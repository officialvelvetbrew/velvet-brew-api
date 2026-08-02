package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.entity.Users;
import com.google.firebase.auth.FirebaseToken;

public interface UserService {

    Users findOrCreate(FirebaseToken firebaseToken);

}