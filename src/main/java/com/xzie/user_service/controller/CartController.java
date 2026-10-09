package com.xzie.user_service.controller;

import com.xzie.user_service.dao.CartRepo;
import com.xzie.user_service.dao.ProductRepo;
import com.xzie.user_service.dto.CartItemDto;
import com.xzie.user_service.model.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('USER')")
public class CartController {

    @Autowired
    private CartRepo cartRepo;
    @Autowired private ProductRepo productRepo;

    @GetMapping
    public List<CartItemDto> get(Authentication auth) {
        return cartRepo.findByUsername(auth.getName()).stream()
                .map(ci -> productRepo.findById(ci.getProductId())
                        .map(p -> new CartItemDto(ci.getId(), p.getId(), p.getName(),
                                p.getPrice(), p.hasImage(), ci.getQuantity()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @PostMapping
    public void add(@RequestBody Map<String, Integer> body, Authentication auth) {
        Long productId = body.get("productId").longValue();
        int qty = body.get("quantity");
        productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        CartItem item = cartRepo.findByUsernameAndProductId(auth.getName(), productId)
                .orElseGet(() -> {
                    CartItem c = new CartItem();
                    c.setUsername(auth.getName());
                    c.setProductId(productId);
                    c.setQuantity(0);
                    return c;
                });
        item.setQuantity(item.getQuantity() + qty);
        cartRepo.save(item);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody Map<String, Integer> body, Authentication auth) {
        CartItem item = cartRepo.findById(id).orElseThrow();
        if (!item.getUsername().equals(auth.getName())) throw new AccessDeniedException("Not your cart");
        item.setQuantity(body.get("quantity"));
        cartRepo.save(item);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id, Authentication auth) {
        CartItem item = cartRepo.findById(id).orElseThrow();
        if (!item.getUsername().equals(auth.getName())) throw new AccessDeniedException("Not your cart");
        cartRepo.delete(item);
    }
}