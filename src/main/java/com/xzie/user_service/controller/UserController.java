package com.xzie.user_service.controller;


import com.xzie.user_service.model.User;
import com.xzie.user_service.service.JwtService;
import com.xzie.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class UserController {

	@Autowired
	private UserService service;
	@Autowired
	AuthenticationManager authenticationManager;


	@Autowired
    JwtService jwtService;


	@PostMapping("/register")
	public User register(@RequestBody User user) {
		return service.register(user);
	}


	@PostMapping("/login")
	public String login(@RequestBody User user)
	{
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
		if(authentication.isAuthenticated())
			return jwtService.generateToken(user.getUsername());
		else
			return "login failed";
	}


	@GetMapping("/api/me")
	public Map<String, Object> me(Authentication auth)
	{
		return service.getMe(auth);
	}

}
