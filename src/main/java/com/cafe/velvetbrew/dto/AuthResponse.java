package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.Role;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;

    private String type;

    private String fullName;

    private String email;

    private String phoneNumber;

    /**
     * Legacy single role - still drives the coarse hasRole()/hasAnyRole()
     * checks in SecurityConfig.
     */
    private Role role;

    /**
     * Dynamic role codes assigned via user_roles (roles table). Empty for a
     * plain CUSTOMER account that only has the legacy role above.
     */
    private List<String> roles;

    /**
     * Effective create/read/update/delete capability per RBAC function,
     * merged across all of the user's roles - see RbacAuthorizationService
     * for how these are enforced server-side. The front end should use this
     * to decide which screens/actions to show, e.g. only render a
     * "Delete supplier" button if the SUPPLIERS entry has canDelete=true.
     */
    private List<FunctionPermissionResponse> functions;
}
