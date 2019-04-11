package com.zero.account;

import java.io.Serializable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class Account implements Serializable, Comparable<Account>{
	private static final Logger LOGGER =  LogManager.getLogger(Account.class);
	private int id;
	private String label;
	private double amount;
	private Type type;
	private Ownership ownership;
	private Status status;
	
	public enum Type{
		Checking, Saving, MoneyMarket, IRA, Brokerage, CD
	}
	
	public enum Ownership{
		Single, Joint
	}
	
	public enum Status{
		Denied, Approved, Closed, Pending
	}
	
	public Account(int id, String label, double amount, Type type, Ownership ownership, Status status) {
		if(id < 1) {
			throw new IllegalArgumentException();
		}
		
		if(label == null) {
			throw new IllegalArgumentException();
		}
		
		if(amount < 1) {
			throw new IllegalArgumentException();
		}
		
		if(type == null) {
			throw new IllegalArgumentException();
		}
		
		if(ownership == null) {
			throw new IllegalArgumentException();
		}
		
		if(status == null) {
			throw new IllegalArgumentException();
		}
		
		if(!(type instanceof Type)) {
			throw new IllegalArgumentException();
		}
		
		if(!(ownership instanceof Ownership)) {
			throw new IllegalArgumentException();
		}
		
		if(!(status instanceof Status)) {
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.label = label;
		this.amount = amount;
		this.type = type;
		this.ownership = ownership;
		this.status = status;
	}
	
	public Account(int id, String label, double amount, Type type, Ownership ownership) {
		if(id < 1) {
			throw new IllegalArgumentException();
		}
		
		if(label == null) {
			throw new IllegalArgumentException();
		}
		
		if(amount < 1) {
			throw new IllegalArgumentException();
		}
		
		if(type == null) {
			throw new IllegalArgumentException();
		}
		
		if(ownership == null) {
			throw new IllegalArgumentException();
		}
		
		if(!(type instanceof Type)) {
			throw new IllegalArgumentException();
		}
		
		if(!(ownership instanceof Ownership)) {
			throw new IllegalArgumentException();
		}
		
		this.id = id;
		this.label = label;
		this.amount = amount;
		this.type = type;
		this.ownership = ownership;
		this.status = Status.Pending;
	}
	
	public int getId() { //account id retrieval
		return id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public Type getType() {
		return type;
	}
	
	public Ownership getOwnership() {
		return ownership;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void backup() {  //transition data into the database
		//call to execute update
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(Account arg0) { //compare accounts
		
		if(arg0 == null) 
			throw new IllegalArgumentException();
		
		if(!(arg0 instanceof Account))
			throw new IllegalArgumentException();
		
		Account arg = (Account)arg0;
		
		return this.id - arg.id;
	}
	
}
