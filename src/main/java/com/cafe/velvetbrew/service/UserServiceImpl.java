package com.cafe.velvetbrew.service;


import com.cafe.velvetbrew.common.enums.AuthProvider;
import com.cafe.velvetbrew.common.enums.Role;
import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Users findOrCreate(FirebaseToken token) {

        Optional<Users> existing =
                userRepository.findByFirebaseUid(token.getUid());

        if (existing.isPresent()) {
            return existing.get();
        }

        Users user = userRepository.findByEmail(token.getEmail())
                .orElse(new Users());

        user.setFirebaseUid(token.getUid());
        user.setEmail(token.getEmail());
        user.setFullName(token.getName());
        user.setEnabled(true);
        user.setEmailVerified(token.isEmailVerified());
        user.setRole(Role.CUSTOMER);

        String provider = (String) token.getClaims().get("firebase");

        if (provider != null && provider.contains("google")) {
            user.setProvider(AuthProvider.GOOGLE);
        } else {
            user.setProvider(AuthProvider.EMAIL);
        }

        return userRepository.save(user);
    }
}