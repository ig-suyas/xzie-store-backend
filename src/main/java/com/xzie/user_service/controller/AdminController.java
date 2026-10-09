package com.xzie.user_service.controller;

import com.xzie.user_service.dto.UserDto;
import com.xzie.user_service.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService service;

    @GetMapping
    public List<UserDto> list() {
        return service.allUsers();
    }

    @PutMapping("/{id}/role")
    public void role(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        service.changeRole(id, body.get("role"), auth.getName());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth.getName());
    }
}
