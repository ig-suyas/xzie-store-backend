package com.xzie.user_service.controller;

import com.xzie.user_service.dao.ProductRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
	@Autowired
	ProductRepo repo;
	@GetMapping("/ping")
	public String ping() {
		repo.count();
		return "ok";
	}
}
