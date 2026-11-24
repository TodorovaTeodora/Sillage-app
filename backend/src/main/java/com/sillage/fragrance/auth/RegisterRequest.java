package com.sillage.fragrance.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank
		@Email
		@Size(max = 254)
		String email,
		@NotBlank
		@Size(max = 72)
		@Pattern(
				regexp = "^(?=.*[A-Z])(?=.*\\d).{5,}$",
				message = "Password must be at least 5 characters and include an uppercase letter and a number"
		)
		String password
) {
	public RegisterRequest {
		email = email == null ? null : email.trim();
	}
}
