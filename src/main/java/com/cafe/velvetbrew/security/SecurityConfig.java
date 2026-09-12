package com.cafe.velvetbrew.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final RbacAuthorizationFilter rbacAuthorizationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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