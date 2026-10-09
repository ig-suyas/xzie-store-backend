package com.xzie.user_service.controller;

import com.xzie.user_service.dto.OrderDto;
import com.xzie.user_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping("/api/orders")
    @PreAuthorize("hasRole('USER')")
    public OrderDto place(@RequestBody Map<String, String> body, Authentication auth) {
        return service.place(auth.getName(), body.get("shippingAddress"));
    }

    @GetMapping("/api/orders")                 // any logged-in user sees their own orders
    public List<OrderDto> mine(Authentication auth) {
        return service.mine(auth.getName());
    }

    @GetMapping("/api/seller/orders")
    @PreAuthorize("hasRole('SELLER')")
    public List<OrderDto> seller(Authentication auth) {
        return service.forSeller(auth.getName());
    }

    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderDto> admin() {
        return service.all();
    }

    @PutMapping("/api/orders/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public void status(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        service.updateStatus(id, body.get("status"), auth.getName(), isAdmin);
    }
}
