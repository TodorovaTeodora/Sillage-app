package com.sillage.fragrance.auth;

public class CurrentPasswordIncorrectException extends RuntimeException {

	public CurrentPasswordIncorrectException() {
		super("Current password is incorrect");
	}
}
