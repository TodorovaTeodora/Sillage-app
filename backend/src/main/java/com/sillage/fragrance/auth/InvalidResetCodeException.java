package com.sillage.fragrance.auth;

public class InvalidResetCodeException extends RuntimeException {

	public InvalidResetCodeException() {
		super("The reset code is invalid or has expired");
	}
}
