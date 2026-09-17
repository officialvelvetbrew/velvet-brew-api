package com.cafe.velvetbrew.service;

import com.cafe.velvetbrew.common.enums.AuthProvider;
import com.cafe.velvetbrew.common.enums.Role;
import com.cafe.velvetbrew.common.exception.EmailAlreadyExistsException;
import com.cafe.velvetbrew.common.exception.InvalidResetTokenException;
import com.cafe.velvetbrew.dto.*;
import com.cafe.velvetbrew.entity.AppRole;
import com.cafe.velvetbrew.entity.PasswordResetToken;
import com.cafe.velvetbrew.entity.RoleFunction;
import com.cafe.velvetbrew.entity.Users;
import com.cafe.velvetbrew.repository.PasswordResetTokenRepository;
import com.cafe.velvetbrew.repository.RoleFunctionRepository;
import com.cafe.velvetbrew.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

	private final UserRepository repository;
	private final RoleFunctionRepository roleFunctionRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final EmailService emailService;

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	@Value("${app.password-reset.token-expiry-minutes}")
	private long resetTokenExpiryMinutes;

	@Value("${app.password-reset.frontend-url}")
	private String resetPasswordFrontendUrl;

	public ApiResponse<Object> register(RegisterRequest request) {

		boolean hasEmail = hasText(request.getEmail());
		boolean hasMobile = hasText(request.getMobile());

		if (!hasEmail && !hasMobile) {
			throw new IllegalArgumentException("Either email or mobile is required.");
		}

		// DTO-level @AssertTrue already rejects neither being present too, but a
		// service-layer guard is what actually protects the uniqueness checks
		// below from running existsByEmail(null)/existsByPhoneNumber(null) -
		// Spring Data turns a null equality parameter into "IS NULL", which
		// would otherwise block registration against any other null-email
		// (phone-only) account already in the table.
		if (hasEmail && repository.existsByEmail(request.getEmail())) {
			log.warn("Registration rejected - email already in use: {}", request.getEmail());
			throw new EmailAlreadyExistsException("Email already registered.");
		}

		if (hasMobile && repository.existsByPhoneNumber(request.getMobile())) {
			log.warn("Registration rejected - mobile already in use: {}", request.getMobile());
			throw new IllegalArgumentException("Mobile number already registered.");
		}

		Users user = Users.builder().fullName(request.getFullName())
				.email(hasEmail ? request.getEmail() : null)
				.phoneNumber(hasMobile ? request.getMobile() : null)
				.password(passwordEncoder.encode(request.getPassword())).role(Role.CUSTOMER)
				.provider(AuthProvider.EMAIL)
				.enabled(true).build();

		Users saved = repository.save(user);

		log.info("Registered new user id={} identifier={} role={}",
				saved.getId(), saved.getPrincipalIdentifier(), saved.getRole());

		return ApiResponse.builder()
		        .success(true)
		        .message("Registration successful")
		        .timestamp(LocalDateTime.now())
		        .build();
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	/**
	 * Always responds with the same generic success message regardless of
	 * whether the email is registered, so this endpoint can't be used to
	 * enumerate which addresses have an account.
	 */
	public ApiResponse<Object> forgotPassword(ForgotPasswordRequest request) {

		repository.findByEmail(request.getEmail()).ifPresent(user -> {

			passwordResetTokenRepository.invalidateActiveTokensForUser(user);

			String token = generateToken();

			PasswordResetToken resetToken = PasswordResetToken.builder()
					.user(user)
					.token(token)
					.expiresAt(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes))
					.used(false)
					.build();

			passwordResetTokenRepository.save(resetToken);

			String resetLink = resetPasswordFrontendUrl + "?token=" + token;

			try {
				emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
				log.info("Password reset token issued for user id={}", user.getId());
			} catch (MailException ex) {
				// Swallowed so a mail-server outage can't be distinguished from an
				// unregistered email by the caller - both return the same generic
				// success response. The token still exists; it's just unreachable
				// until mail delivery is fixed.
				log.error("Failed to send password reset email for user id={}", user.getId(), ex);
			}
		});

		return ApiResponse.builder()
				.success(true)
				.message("If an account exists for that email, a password reset link has been sent.")
				.timestamp(LocalDateTime.now())
				.build();
	}

	public ApiResponse<Object> resetPassword(ResetPasswordRequest request) {

		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
				.orElseThrow(() -> new InvalidResetTokenException("Invalid or expired reset token."));

		if (Boolean.TRUE.equals(resetToken.getUsed()) || resetToken.isExpired()) {
			throw new InvalidResetTokenException("Invalid or expired reset token.");
		}

		Users user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		repository.save(user);

		resetToken.setUsed(true);
		passwordResetTokenRepository.save(resetToken);

		log.info("Password reset completed for user id={}", user.getId());

		return ApiResponse.builder()
				.success(true)
				.message("Password reset successful. You can now log in with your new password.")
				.timestamp(LocalDateTime.now())
				.build();
	}

	private static String generateToken() {

		byte[] randomBytes = new byte[32];
		SECURE_RANDOM.nextBytes(randomBytes);

		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}

	public AuthResponse login(LoginRequest request) {

		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getUsername(),
						request.getPassword()));

		Users user = repository.findByEmail(request.getUsername())
				.or(() -> repository.findByPhoneNumber(request.getUsername()))
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		String token = jwtService.generateToken(user);

		log.info("User id={} logged in via password (role={})", user.getId(), user.getRole());

		return buildAuthResponse(user.getId(), token);
	}

	/**
	 * Re-derives the current user's profile, roles and granted functions
	 * from a valid JWT, without minting a new token - used by GET /me so a
	 * front end can refresh what it's allowed to show after a page reload.
	 */
	@Transactional(readOnly = true)
	public AuthResponse me(String username) {

		Users user = repository.findByEmail(username)
				.or(() -> repository.findByPhoneNumber(username))
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return buildAuthResponse(user.getId(), null);
	}

	/**
	 * Loaded fresh (rather than reusing a possibly-detached entity) so the
	 * lazy roles collection is guaranteed fetchable within this method's own
	 * transaction, regardless of where the caller got the user id from.
	 */
	@Transactional(readOnly = true)
	public AuthResponse buildAuthResponse(Long userId, String token) {

		Users user = repository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		List<AppRole> activeRoles = user.getRoles().stream()
				.filter(role -> Boolean.TRUE.equals(role.getActive()))
				.toList();

		List<String> roleCodes = activeRoles.stream()
				.map(AppRole::getRoleCode)
				.distinct()
				.sorted()
				.toList();

		List<FunctionPermissionResponse> functions = mergeFunctionPermissions(activeRoles);

		return AuthResponse.builder()
				.token(token)
				.type("Bearer")
				.fullName(user.getFullName())
				.email(user.getEmail())
				.phoneNumber(user.getPhoneNumber())
				.role(user.getRole())
				.roles(roleCodes)
				.functions(functions)
				.build();
	}

	/**
	 * A user can hold more than one role, and two roles can disagree on what
	 * a function permits (e.g. one grants read-only, another grants create
	 * too) - flags are merged per function code, most permissive wins.
	 */
	private List<FunctionPermissionResponse> mergeFunctionPermissions(List<AppRole> activeRoles) {

		Set<Long> roleIds = activeRoles.stream().map(AppRole::getId).collect(Collectors.toSet());

		if (roleIds.isEmpty()) {
			return List.of();
		}

		Map<String, boolean[]> merged = new LinkedHashMap<>();

		for (RoleFunction grant : roleFunctionRepository.findByRoleIdIn(roleIds)) {

			if (!Boolean.TRUE.equals(grant.getFunction().getActive())) {
				continue;
			}

			boolean[] flags = merged.computeIfAbsent(
					grant.getFunction().getFunctionCode(), code -> new boolean[4]);

			flags[0] |= Boolean.TRUE.equals(grant.getCanCreate());
			flags[1] |= Boolean.TRUE.equals(grant.getCanRead());
			flags[2] |= Boolean.TRUE.equals(grant.getCanUpdate());
			flags[3] |= Boolean.TRUE.equals(grant.getCanDelete());
		}

		return merged.entrySet().stream()
				.map(entry -> FunctionPermissionResponse.builder()
						.functionCode(entry.getKey())
						.canCreate(entry.getValue()[0])
						.canRead(entry.getValue()[1])
						.canUpdate(entry.getValue()[2])
						.canDelete(entry.getValue()[3])
						.build())
				.sorted((a, b) -> a.getFunctionCode().compareTo(b.getFunctionCode()))
				.toList();
	}

}
