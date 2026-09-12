package com.cafe.velvetbrew.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Runs after JWT authentication. If the caller is authenticated, checks the
 * DB-driven function_urls/role_functions permission model on top of the
 * coarse role rules already enforced by SecurityConfig. Unauthenticated
 * requests are left untouched - public/permitAll routing is unaffected.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacAuthorizationFilter extends OncePerRequestFilter {

    private final RbacAuthorizationService rbacAuthorizationService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed = rbacAuthorizationService.isAllowed(
                authentication.getName(), request.getRequestURI(), request.getMethod());

        if (!allowed) {

            log.warn("RBAC denied {} on {} {}",
                    authentication.getName(), request.getMethod(), request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "success", false,
                    "message", "You do not have permission to perform this action",
                    "timestamp", LocalDateTime.now().toString()
            )));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
