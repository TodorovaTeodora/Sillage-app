package com.sillage.fragrance.auth;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
		@Size(max = 40) String username,
		@Size(max = 100) String country,
		@Size(max = 100) String city
) {
	public UpdateProfileRequest {
		username = trim(username);
		country = trim(country);
		city = trim(city);
	}

	private static String trim(String value) {
		return value == null ? "" : value.trim();
	}
}
