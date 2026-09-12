package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.exception.InvalidFirebaseTokenException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirebaseService {

    public FirebaseToken verifyToken(String idToken) {

        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            log.info("Firebase token verified for uid={}", token.getUid());
            return token;

        } catch (FirebaseAuthException ex) {
            // A rejected/expired/malformed token is a client error, not a
            // server fault - translated into a clean 401 by
            // GlobalExceptionHandler instead of falling through to the
            // generic 500 handler.
            log.warn("Firebase token verification rejected: {}", ex.getMessage());
            throw new InvalidFirebaseTokenException("Invalid or expired authentication token.");
        }
    }
}