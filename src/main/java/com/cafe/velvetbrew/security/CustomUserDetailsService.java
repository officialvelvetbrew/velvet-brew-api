package com.cafe.velvetbrew.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Users user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhoneNumber(username))
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())   // principal
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .build();
    }
}