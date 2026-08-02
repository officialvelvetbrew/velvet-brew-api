package com.cafe.velvetbrew.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    public FirebaseToken verifyToken(String idToken) throws Exception {

        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }
}