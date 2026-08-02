package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.*;
import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.service.AuthService;
import com.cafe.velvetbrew.service.FirebaseService;
import com.cafe.velvetbrew.service.JwtService;
import com.cafe.velvetbrew.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final FirebaseService firebaseService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/firebase")
    public ResponseEntity<LoginResponse> firebaseLogin(
            @RequestBody FirebaseLoginRequest request) throws Exception {

        FirebaseToken firebaseToken =
                firebaseService.verifyToken(request.getIdToken());

        Users user = userService.findOrCreate(firebaseToken);

        String jwt = jwtService.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(jwt, user));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
}