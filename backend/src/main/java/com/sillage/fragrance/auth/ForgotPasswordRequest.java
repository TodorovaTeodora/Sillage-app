package com.sillage.fragrance.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ForgotPasswordRequest(
		@NotBlank
		@Email
		@Size(max = 254)
		String email
) {
	public ForgotPasswordRequest {
		email = email == null ? null : email.trim();
	}
}
