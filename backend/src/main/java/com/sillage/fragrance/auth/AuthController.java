package com.sillage.fragrance.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final AuthSession authSession;

	public AuthController(AuthService authService, AuthSession authSession) {
		this.authService = authService;
		this.authSession = authSession;
	}

	@PostMapping("/register")
	ResponseEntity<AuthResponse> register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletRequest servletRequest
	) {
		UserAccount account = authService.register(request.email(), request.password());
		authSession.signIn(servletRequest, account);
		return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(account));
	}

	@PostMapping("/login")
	AuthResponse login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest servletRequest
	) {
		UserAccount account = authService.authenticate(request.email(), request.password());
		authSession.signIn(servletRequest, account);
		return AuthResponse.from(account);
	}

	@GetMapping("/me")
	ResponseEntity<AuthResponse> currentUser(HttpServletRequest request) {
		return authSession.userId(request)
				.flatMap(authService::findById)
				.map(account -> ResponseEntity.ok(AuthResponse.from(account)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	@PostMapping("/logout")
	ResponseEntity<Void> logout(HttpServletRequest request) {
		authSession.signOut(request);
		return ResponseEntity.noContent().build();
	}
}
