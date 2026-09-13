package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.Role;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserRoleResponse {

    Long id;
    String identifier;
    Role role;
}
