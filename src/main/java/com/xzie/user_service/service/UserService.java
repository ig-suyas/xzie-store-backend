package com.xzie.user_service.service;


import com.xzie.user_service.dao.UserRepo;
import com.xzie.user_service.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {
	
@Autowired
	private UserRepo repo;
private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

	public User register(User user)
	{
		user.setPassword(encoder.encode(user.getPassword()));
		user.setRoles("USER");
		return repo.save(user);
	}

	public Map<String, Object> getMe(Authentication auth)
	{
		User u = repo.findByUsername(auth.getName());
		return Map.of(
				"id", u.getId(),
				"username", u.getUsername(),
				"name", u.getUsername(),
				"email", u.getEmail() == null ? "" : u.getEmail(),
				"role", u.getRoles());
	}
}
