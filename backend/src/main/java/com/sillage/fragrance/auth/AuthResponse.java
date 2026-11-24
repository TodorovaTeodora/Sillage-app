package com.sillage.fragrance.auth;

public record AuthResponse(String email, String username) {

	static AuthResponse from(UserAccount account) {
		return new AuthResponse(account.getEmail(), account.getUsername());
	}
}
