package com.xzie.user_service.service;

import com.xzie.user_service.dao.CartRepo;
import com.xzie.user_service.dao.UserRepo;
import com.xzie.user_service.dto.UserDto;
import com.xzie.user_service.model.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private static final List<String> ROLES = List.of("USER", "SELLER", "ADMIN");

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CartRepo cartRepo;

    public List<UserDto> allUsers() {
        return userRepo.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getRoles()))
                .toList();
    }

    public void changeRole(Long id, String role, String currentUsername) {
        if (role == null || !ROLES.contains(role)) throw new RuntimeException("Invalid role");
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (u.getUsername().equals(currentUsername))
            throw new RuntimeException("You can't change your own role");
        u.setRoles(role);
        userRepo.save(u);
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (u.getUsername().equals(currentUsername))
            throw new RuntimeException("You can't delete your own account");
        cartRepo.deleteAll(cartRepo.findByUsername(u.getUsername()));   // clean up their cart
        userRepo.delete(u);
    }
}
