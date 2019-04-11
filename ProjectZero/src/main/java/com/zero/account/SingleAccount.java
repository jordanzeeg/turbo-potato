package com.zero.account;

public class SingleAccount extends Account{
	private int ownerId;
	
	public SingleAccount(int id, String label, int ownerId, double amount, Type type, Status status) {
		super(id, label, amount, type, Ownership.Single, status);
		if(ownerId < 1) {
			throw new IllegalArgumentException();
		}
		this.ownerId = ownerId;
	}
	
	public SingleAccount(int id, String label, int ownerId, double initialAmount, Type type) {
		super(id, label, initialAmount, type, Ownership.Single, Status.Pending);
		if(ownerId < 1) {
			throw new IllegalArgumentException();
		}
		this.ownerId = ownerId;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
}
