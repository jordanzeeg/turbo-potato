package com.zero.account;

public class JointAccount extends Account{
	private int ownerId;
	private int secondOwnerId;
	
	public JointAccount(int id, String label, int ownerId, double amount, Type type, Status status) {
		super(id, label, amount, type, Ownership.Joint, status);
		if(ownerId < 1) {
			throw new IllegalArgumentException();
		}
		this.ownerId = ownerId;
	}
	
	public JointAccount(int id, String label, int ownerId, int secondOwnerId, double initialAmount, Type type) {
		super(id, label, initialAmount, type, Ownership.Joint, Status.Pending);
		if(ownerId < 1) {
			throw new IllegalArgumentException();
		}
		
		if(secondOwnerId < 1) {
			throw new IllegalArgumentException();
		}
		this.ownerId = ownerId;
		this.secondOwnerId = secondOwnerId;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public int getSecondOwnerId() {
		return secondOwnerId;
	}
	
	public void setSecondOwnerId(int secondOwnerId) {
		if(secondOwnerId < 1) {
			throw new IllegalArgumentException();
		}
		this.secondOwnerId = secondOwnerId;
	}
}
