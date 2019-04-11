package com.zero.admin.user;

public class NegativeBalanceException extends RuntimeException {
	
	public NegativeBalanceException() {
		super();
	}
	
	public NegativeBalanceException(String message) {
		super(message);
	}
	
}
