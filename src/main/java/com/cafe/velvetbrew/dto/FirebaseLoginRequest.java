package com.cafe.velvetbrew.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class FirebaseLoginRequest {

    @JsonAlias("token")
    private String idToken;

    // Only used when the Firebase token itself carries no phone_number
    // claim (e.g. Google/email sign-in) but the client already collected
    // one - the frontend has been sending this under several different
    // key names, hence the aliases.
    @JsonAlias({"mobile", "phone", "phone_number"})
    private String phoneNumber;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}