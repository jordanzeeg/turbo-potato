package com.zero.admin.user;

public class AccountDoesNotExistException extends RuntimeException{
	
	public AccountDoesNotExistException() {
		super();
	}
	
	public AccountDoesNotExistException(String message) {
		super(message);
	}

}
