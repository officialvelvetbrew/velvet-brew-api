package com.cafe.velvetbrew.service;


import com.cafe.velvetbrew.common.enums.AuthProvider;
import com.cafe.velvetbrew.common.enums.Role;
import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Users findOrCreate(FirebaseToken token, String suppliedPhoneNumber) {

        Optional<Users> existingByUid =
                userRepository.findByFirebaseUid(token.getUid());

        String email = token.getEmail();
        String tokenPhoneNumber = extractPhoneNumber(token);

        // The verified-by-OTP phone number from the token, if any, always
        // wins; a client-supplied one is only a fallback for sign-in
        // methods (Google/email) whose token carries no phone_number claim
        // at all, e.g. a number the user typed into a profile form.
        String suppliedPhoneNumberOrNull = blankToNull(suppliedPhoneNumber);
        String phoneNumber = tokenPhoneNumber != null ? tokenPhoneNumber : suppliedPhoneNumberOrNull;

        if (email == null && phoneNumber == null) {
            log.warn("Firebase token for uid={} carries neither an email nor a phone number", token.getUid());
        }

        // A user already linked to this firebaseUid still needs the fields
        // below synced on every login - e.g. a phone_number claim that
        // wasn't present on an earlier login (before the user verified a
        // phone number) would otherwise never make it into the DB, since
        // this lookup would keep finding the same row and returning early.
        Users user = existingByUid.orElseGet(() -> findByEmailOrPhone(email, phoneNumber).orElse(new Users()));

        boolean isNewAccount = user.getId() == null;

        user.setFirebaseUid(token.getUid());

        // Only overwrite an identifier the token actually carries - a
        // phone-auth token has no email claim, and setting it to null here
        // would wipe out an email a pre-existing account already had.
        if (email != null) {
            user.setEmail(email);
        }

        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }

        String tokenName = token.getName();
        if (tokenName != null) {
            user.setFullName(tokenName);
        } else if (user.getFullName() == null) {
            // full_name is NOT NULL in the DB, but Firebase only populates a
            // display name for Google sign-in - email/password and phone
            // sign-in tokens carry no name claim at all.
            user.setFullName(deriveFallbackName(email, phoneNumber));
        }
        user.setEnabled(true);
        user.setEmailVerified(token.isEmailVerified());
        user.setRole(Role.CUSTOMER);
        user.setProvider(resolveProvider(token, tokenPhoneNumber));

        Users saved = userRepository.save(user);

        if (isNewAccount) {
            log.info("Created new user id={} via Firebase (uid={}, identifier={})",
                    saved.getId(), token.getUid(), saved.getPrincipalIdentifier());
        } else {
            log.info("Linked Firebase uid={} to existing user id={}", token.getUid(), saved.getId());
        }

        return saved;
    }

    /**
     * Never calls findByEmail(null)/findByPhoneNumber(null) - Spring Data
     * turns a null equality parameter into "IS NULL", which would otherwise
     * match a completely unrelated account that also has a null email or
     * null phone number (exactly the bug class fixed elsewhere this
     * session for password login/registration).
     */
    private Optional<Users> findByEmailOrPhone(String email, String phoneNumber) {

        if (email != null) {
            Optional<Users> byEmail = userRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                return byEmail;
            }
        }

        return phoneNumber != null ? userRepository.findByPhoneNumber(phoneNumber) : Optional.empty();
    }

    private String deriveFallbackName(String email, String phoneNumber) {

        if (email != null) {
            return email.substring(0, email.indexOf('@'));
        }

        return phoneNumber != null ? phoneNumber : "Customer";
    }

    private String blankToNull(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }

    private String extractPhoneNumber(FirebaseToken token) {

        Object claim = token.getClaims().get("phone_number");
        return claim instanceof String phone ? phone : null;
    }

    /**
     * The "firebase" claim is a nested object ({"sign_in_provider": "...",
     * "identities": {...}}), not a string - casting it directly to String,
     * as this method used to, throws ClassCastException the moment Firebase
     * login is actually exercised.
     */
    @SuppressWarnings("unchecked")
    private AuthProvider resolveProvider(FirebaseToken token, String phoneNumber) {

        if (phoneNumber != null) {
            return AuthProvider.PHONE;
        }

        Object firebaseClaim = token.getClaims().get("firebase");
        String signInProvider = null;

        if (firebaseClaim instanceof Map) {
            Object value = ((Map<String, Object>) firebaseClaim).get("sign_in_provider");
            signInProvider = value instanceof String ? (String) value : null;
        }

        return signInProvider != null && signInProvider.contains("google")
                ? AuthProvider.GOOGLE
                : AuthProvider.EMAIL;
    }
}
