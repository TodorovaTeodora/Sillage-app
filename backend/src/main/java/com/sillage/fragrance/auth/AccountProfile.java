package com.sillage.fragrance.auth;

public record AccountProfile(
		String email,
		String username,
		String country,
		String city
) {
	static AccountProfile from(UserAccount account) {
		return new AccountProfile(
				account.getEmail(),
				account.getUsername(),
				account.getCountry(),
				account.getCity()
		);
	}
}
