package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required.")
    private Role role;
}
