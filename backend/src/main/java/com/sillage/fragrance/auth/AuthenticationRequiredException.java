package com.sillage.fragrance.auth;

public class AuthenticationRequiredException extends RuntimeException {

	public AuthenticationRequiredException() {
		super("Please log in to manage your collection");
	}
}
