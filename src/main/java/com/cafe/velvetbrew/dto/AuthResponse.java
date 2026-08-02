package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;

    private String type = "Bearer";

    private String fullName;

    private String email;

    private Role role;
}