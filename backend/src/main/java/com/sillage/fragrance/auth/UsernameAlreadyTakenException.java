package com.sillage.fragrance.auth;

public class UsernameAlreadyTakenException extends RuntimeException {

	public UsernameAlreadyTakenException() {
		super("This username is already in use");
	}
}
