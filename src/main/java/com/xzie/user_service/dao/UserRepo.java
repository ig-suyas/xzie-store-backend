package com.xzie.user_service.dao;


import com.xzie.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {


	User findByUsername(String username);
	Optional<User> findByEmail(String email);
}
