package com.sillage.fragrance.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

	private final AuthService authService;
	private final AuthSession authSession;

	public AccountController(AuthService authService, AuthSession authSession) {
		this.authService = authService;
		this.authSession = authSession;
	}

	@GetMapping
	AccountProfile profile(HttpServletRequest request) {
		return authService.findById(authSession.requireUserId(request))
				.map(AccountProfile::from)
				.orElseThrow(AuthenticationRequiredException::new);
	}

	@PutMapping
	AccountProfile updateProfile(
			@Valid @RequestBody UpdateProfileRequest profile,
			HttpServletRequest request
	) {
		return AccountProfile.from(
				authService.updateProfile(authSession.requireUserId(request), profile)
		);
	}

	@PutMapping("/password")
	ResponseEntity<Void> updatePassword(
			@Valid @RequestBody UpdatePasswordRequest password,
			HttpServletRequest request
	) {
		authService.updatePassword(authSession.requireUserId(request), password);
		return ResponseEntity.noContent().build();
	}
}
