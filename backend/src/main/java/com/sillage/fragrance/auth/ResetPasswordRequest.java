package com.sillage.fragrance.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record ResetPasswordRequest(
		@NotBlank
		@Email
		@Size(max = 254)
		String email,
		@NotBlank
		@Pattern(regexp = "\\d{6}", message = "Reset code must contain 6 digits")
		String code,
		@NotBlank
		@Size(max = 72)
		@Pattern(
				regexp = "^(?=.*[A-Z])(?=.*\\d).{5,}$",
				message = "Password must be at least 5 characters and include an uppercase letter and a number"
		)
		String newPassword
) {
	public ResetPasswordRequest {
		email = email == null ? null : email.trim();
		code = code == null ? null : code.trim();
	}
}
