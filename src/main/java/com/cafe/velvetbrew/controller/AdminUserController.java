package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.UpdateUserRoleRequest;
import com.cafe.velvetbrew.dto.UserRoleResponse;
import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final UserService userService;

    /**
     * Promotes/demotes a user, keeping the legacy users.role column and the
     * RBAC user_roles grant in sync in one call - see UserServiceImpl and
     * migration V32 for why letting these drift apart is the bug this
     * replaces (a promoted admin/staff account otherwise still gets 403s
     * from RbacAuthorizationFilter despite passing SecurityConfig's role
     * check).
     */
    @PatchMapping("/{id}/role")
    public ApiResponse<UserRoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        Users updated = userService.updateRole(id, request.getRole());

        return ApiResponse.success(
                "User role updated successfully",
                UserRoleResponse.builder()
                        .id(updated.getId())
                        .identifier(updated.getPrincipalIdentifier())
                        .role(updated.getRole())
                        .build());
    }
}
