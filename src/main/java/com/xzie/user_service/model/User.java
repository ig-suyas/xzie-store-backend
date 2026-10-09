package com.xzie.user_service.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.Bean;


@Data
@Table(name = "users")
@Entity
public class User {
	
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String roles;
	private String username;
	private String password;
	private String email;
	
}
