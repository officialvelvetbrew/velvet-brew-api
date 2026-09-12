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
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final FirebaseService firebaseService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/firebase")
    public ResponseEntity<AuthResponse> firebaseLogin(
            @RequestBody FirebaseLoginRequest request) {

        FirebaseToken firebaseToken =
                firebaseService.verifyToken(request.getIdToken());

        Users user = userService.findOrCreate(firebaseToken, request.getPhoneNumber());

        String jwt = jwtService.generateToken(user);

        return ResponseEntity.ok(authService.buildAuthResponse(user.getId(), jwt));
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

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(authService.resetPassword(request));
    }

    /**
     * Re-fetches the caller's profile, roles and granted functions from
     * their existing JWT - no new token is issued. Meant for a front end to
     * call on app load / page refresh to know which screens and actions to
     * show without forcing a fresh login.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {

        return ResponseEntity.ok(authService.me(authentication.getName()));
    }
}
