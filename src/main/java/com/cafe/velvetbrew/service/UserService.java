package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.Role;
import com.cafe.velvetbrew.entity.Users;
import com.google.firebase.auth.FirebaseToken;

public interface UserService {

    Users findOrCreate(FirebaseToken firebaseToken, String suppliedPhoneNumber);

    /**
     * Updates both the legacy users.role column and the RBAC user_roles
     * grant together, so the two never drift apart the way pre-RBAC
     * accounts did (see migration V32).
     */
    Users updateRole(Long userId, Role role);

}