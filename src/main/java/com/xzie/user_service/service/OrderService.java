package com.xzie.user_service.service;

import com.xzie.user_service.dao.CartRepo;
import com.xzie.user_service.dao.OrderRepo;
import com.xzie.user_service.dao.ProductRepo;
import com.xzie.user_service.dto.OrderDto;
import com.xzie.user_service.dto.OrderItemDto;
import com.xzie.user_service.model.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;
    @Autowired private CartRepo cartRepo;
    @Autowired private ProductRepo productRepo;

    @Transactional
    public OrderDto place(String username, String address) {
        List<CartItem> cart = cartRepo.findByUsername(username);
        if (cart.isEmpty()) throw new RuntimeException("Your cart is empty");
        if (address == null || address.isBlank()) throw new RuntimeException("Shipping address is required");

        Order order = new Order();
        order.setUsername(username);
        order.setShippingAddress(address);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cart) {
            Product p = productRepo.findById(ci.getProductId())
                    .orElseThrow(() -> new RuntimeException("A product in your cart no longer exists"));
            if (p.getStock() < ci.getQuantity())
                throw new RuntimeException("Not enough stock for " + p.getName());
            p.setStock(p.getStock() - ci.getQuantity());
            productRepo.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setSellerName(p.getSellerName());
            order.getItems().add(oi);
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotal(total);
        Order saved = orderRepo.save(order);
        cartRepo.deleteAll(cart);          // empty the cart
        return toDto(saved, null);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> mine(String username) {
        return orderRepo.findByUsernameOrderByIdDesc(username).stream().map(o -> toDto(o, null)).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> forSeller(String seller) {
        return orderRepo.findForSeller(seller).stream().map(o -> toDto(o, seller)).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> all() {
        return orderRepo.findAllByOrderByIdDesc().stream().map(o -> toDto(o, null)).toList();
    }

    @Transactional
    public void updateStatus(Long id, String status, String username, boolean isAdmin) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        boolean theirs = o.getItems().stream().anyMatch(i -> username.equals(i.getSellerName()));
        if (!isAdmin && !theirs) throw new AccessDeniedException("Not your order");
        try { o.setStatus(OrderStatus.valueOf(status)); }
        catch (Exception e) { throw new RuntimeException("Invalid status"); }
        orderRepo.save(o);
    }

    // sellerFilter: if set, show only that seller's items and their subtotal
    private OrderDto toDto(Order o, String sellerFilter) {
        List<OrderItem> items = o.getItems().stream()
                .filter(i -> sellerFilter == null || sellerFilter.equals(i.getSellerName())).toList();
        BigDecimal total = sellerFilter == null ? o.getTotal()
                : items.stream().map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderDto(o.getId(), o.getStatus().name(), total, o.getCreatedAt(),
                o.getShippingAddress(), o.getUsername(),
                items.stream().map(i -> new OrderItemDto(i.getProductName(), i.getQuantity(), i.getPrice())).toList());
    }
}
