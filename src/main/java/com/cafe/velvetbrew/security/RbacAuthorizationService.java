package com.cafe.velvetbrew.security;

import com.cafe.velvetbrew.entity.AppRole;
import com.cafe.velvetbrew.entity.FunctionUrl;
import com.cafe.velvetbrew.entity.RoleFunction;
import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.repository.FunctionUrlRepository;
import com.cafe.velvetbrew.repository.RoleFunctionRepository;
import com.cafe.velvetbrew.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fine-grained, DB-driven authorization check layered on top of the coarse
 * role-based rules in SecurityConfig. A request is only restricted here if an
 * active row in function_urls matches its path + method; otherwise this check
 * defers entirely to the existing role-based rules (it never widens access).
 *
 * Each function_urls row is flagged with the ONE CRUD operation it represents
 * (e.g. "DELETE /inventory/items/{id}" has can_delete=true and the other
 * three false). A caller is allowed through only if one of their roles holds
 * a role_functions grant on that same function with the matching flag also
 * true - this is what lets STAFF, say, create/read/update inventory items
 * but not delete them, while ADMIN can do all four.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RbacAuthorizationService {

    private final FunctionUrlRepository functionUrlRepository;
    private final UserRepository userRepository;
    private final RoleFunctionRepository roleFunctionRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean isAllowed(String username, String path, String method) {

        List<FunctionUrl> candidates = functionUrlRepository.findByActiveTrue()
                .stream()
                .filter(fu -> Boolean.TRUE.equals(fu.getFunction().getActive()))
                .filter(fu -> matchesMethod(fu.getHttpMethod(), method))
                .filter(fu -> pathMatcher.match(fu.getUrl(), path))
                .toList();

        if (candidates.isEmpty()) {
            return true;
        }

        Users user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhoneNumber(username))
                .orElse(null);

        if (user == null) {
            return true;
        }

        Set<Long> roleIds = user.getRoles().stream()
                .filter(role -> Boolean.TRUE.equals(role.getActive()))
                .map(AppRole::getId)
                .collect(Collectors.toSet());

        if (roleIds.isEmpty()) {
            return false;
        }

        Set<Long> functionIds = candidates.stream()
                .map(fu -> fu.getFunction().getId())
                .collect(Collectors.toSet());

        List<RoleFunction> grants = roleFunctionRepository
                .findByRoleIdInAndFunctionIdIn(roleIds, functionIds);

        for (FunctionUrl fu : candidates) {
            for (RoleFunction grant : grants) {

                if (!grant.getFunction().getId().equals(fu.getFunction().getId())) {
                    continue;
                }

                if (Boolean.TRUE.equals(fu.getCanCreate()) && Boolean.TRUE.equals(grant.getCanCreate())) return true;
                if (Boolean.TRUE.equals(fu.getCanRead())   && Boolean.TRUE.equals(grant.getCanRead()))   return true;
                if (Boolean.TRUE.equals(fu.getCanUpdate()) && Boolean.TRUE.equals(grant.getCanUpdate())) return true;
                if (Boolean.TRUE.equals(fu.getCanDelete()) && Boolean.TRUE.equals(grant.getCanDelete())) return true;
            }
        }

        return false;
    }

    private boolean matchesMethod(String configured, String actual) {
        return "*".equals(configured) || configured.equalsIgnoreCase(actual);
    }
}
