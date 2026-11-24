package com.sillage.fragrance.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class PasswordResetController {

	private static final String GENERIC_MESSAGE =
			"If an account exists for this email, a reset code has been sent.";

	private final PasswordResetService passwordResetService;

	PasswordResetController(PasswordResetService passwordResetService) {
		this.passwordResetService = passwordResetService;
	}

	@PostMapping("/forgot-password")
	ResponseEntity<PasswordResetResponse> forgotPassword(
			@Valid @RequestBody ForgotPasswordRequest request
	) {
		passwordResetService.requestCode(request.email());
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(new PasswordResetResponse(GENERIC_MESSAGE));
	}

	@PostMapping("/reset-password")
	ResponseEntity<Void> resetPassword(
			@Valid @RequestBody ResetPasswordRequest request
	) {
		passwordResetService.resetPassword(
				request.email(),
				request.code(),
				request.newPassword()
		);
		return ResponseEntity.noContent().build();
	}
}
