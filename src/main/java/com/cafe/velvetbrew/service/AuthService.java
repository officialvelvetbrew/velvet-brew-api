package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.Role;
import com.cafe.velvetbrew.common.exception.EmailAlreadyExistsException;
import com.cafe.velvetbrew.dto.*;
import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public ApiResponse<Object> register(RegisterRequest request) {

		if (repository.existsByEmail(request.getEmail())) {
			throw new EmailAlreadyExistsException("Email already registered.");
		}

		Users user = Users.builder().fullName(request.getFullName()).email(request.getEmail())
				.phoneNumber(request.getMobile()).password(passwordEncoder.encode(request.getPassword())).role(Role.CUSTOMER)
				.enabled(true).build();

		repository.save(user);

		return ApiResponse.builder()
		        .success(true)
		        .message("Registration successful")
		        .timestamp(LocalDateTime.now())
		        .build();
	}

	public AuthResponse login(LoginRequest request) {

		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getUsername(),
						request.getPassword()));

		Users user = repository.findByEmail(request.getUsername())
				.or(() -> repository.findByPhoneNumber(request.getUsername()))
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		String token = jwtService.generateToken(user);

		return AuthResponse.builder()
				.token(token)
				.type("Bearer")
				.fullName(user.getFullName())
				.email(user.getEmail())
				.role(user.getRole())
				.build();
	}

}
