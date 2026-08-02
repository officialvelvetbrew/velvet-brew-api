package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;

    private Users user;
}