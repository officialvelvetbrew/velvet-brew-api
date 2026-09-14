package com.cafe.velvetbrew.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final RbacAuthorizationFilter rbacAuthorizationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Without this, Spring Security has no httpBasic()/formLogin()/
                // oauth2ResourceServer() registered and no AuthenticationEntryPoint
                // bean to auto-detect, so it silently falls back to
                // Http403ForbiddenEntryPoint - every missing/invalid/expired JWT
                // then comes back as 403 Forbidden instead of 401 Unauthorized,
                // indistinguishable from RbacAuthorizationFilter's genuine
                // "authenticated but not permitted" 403 below.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                                    "success", false,
                                    "message", "Authentication required. Please log in again.",
                                    "timestamp", LocalDateTime.now().toString()
                            )));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                                    "success", false,
                                    "message", "You do not have permission to perform this action",
                                    "timestamp", LocalDateTime.now().toString()
                            )));
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/firebase",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/customer/menu",
                                "/api/v1/customer/menu/**",
                                "/api/v1/category",
                                "/api/v1/customer/offers",
                                "/api/v1/customer/offers/**"
                        ).permitAll()
                        // Everything else under /api/v1/auth/** (e.g. /me) needs a valid JWT.
                        // Guest checkout: placing an order and checking its status by
                        // order number don't require login, but listing every order or
                        // editing an arbitrary one by number is a staff/admin operation.
                        .requestMatchers(HttpMethod.POST, "/api/v1/customer/orders").permitAll()
                        // Same guest-checkout rule as orders above: initiating a Razorpay
                        // payment has to be reachable without a JWT. Verification is safe
                        // to leave open too - it's authenticated by Razorpay's signature,
                        // not by who's calling it, same as any payment-gateway callback.
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments", "/api/v1/payments/verify").permitAll()
                        // Must be matched before the order-number wildcard below, or
                        // "mine" would be treated as an order number and permitted.
                        .requestMatchers(HttpMethod.GET, "/api/v1/customer/orders/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/customer/orders/*").permitAll()
                        .requestMatchers("/api/v1/customer/orders/**").hasAnyRole("ADMIN", "STAFF")
                        // Matched before the general /api/v1/admin/** ADMIN-only rule below:
                        // updating an order's status, or marking a cash payment paid/unpaid,
                        // is a counter/kitchen operation, so STAFF needs both too, unlike the
                        // rest of the admin surface.
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/admin/orders/*/status",
                                        "/api/v1/admin/orders/*/payment-status")
                                .hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "STAFF")
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rbacAuthorizationFilter,
                        JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
}