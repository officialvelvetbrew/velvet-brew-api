package com.cafe.velvetbrew.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Users user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhoneNumber(username))
                .orElseThrow(() -> {
                    log.warn("Authentication lookup failed - no user for: {}", username);
                    return new UsernameNotFoundException("User not found");
                });

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            log.warn("Authentication lookup for disabled account: {}", username);
        }

        Set<String> authorities = new HashSet<>();
        authorities.add("ROLE_" + user.getRole().name());

        user.getRoles().forEach(role -> {
            if (Boolean.TRUE.equals(role.getActive())) {
                authorities.add("ROLE_" + role.getRoleCode().toUpperCase());
            }
        });

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPrincipalIdentifier())   // principal
                .password(user.getPassword())
                .authorities(authorities.toArray(new String[0]))
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .build();
    }
}
