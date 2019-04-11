package com.zero;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zero.account.Account;
import com.zero.account.SingleAccount;
import com.zero.database.Database;

public class TestSingleAccount {
	private static Database sDb;
	
	@BeforeClass
	public static void setup() {
		
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSingleAccountConstructor() {
		SingleAccount account = new SingleAccount(-1, "Gifts", 1, 1.0, Account.Type.Checking, Account.Status.Pending);
		account = new SingleAccount(1, null, 1, 1.0, Account.Type.Checking, Account.Status.Pending);
		account = new SingleAccount(1, "Gifts", -1, 1.0, Account.Type.Checking, Account.Status.Pending);
		account = new SingleAccount(1, "Gifts", 1, -1.0, Account.Type.Checking, Account.Status.Pending);
		account = new SingleAccount(1, "Gifts", 1, 1.0, null, Account.Status.Pending);
		account = new SingleAccount(1, "Gifts", 1, 1.0, Account.Type.Checking, null);
	}
	
	@Test
	public void testSingleAccountCompareTo() {
		SingleAccount account = new SingleAccount(1, "Gifts", 1, 1.0, Account.Type.Checking, Account.Status.Pending);
		SingleAccount secondAccount = new SingleAccount(2, "GiftsAndStuff", 1, 1.0, Account.Type.Checking, Account.Status.Pending);
		
		Assert.assertEquals(account.compareTo(secondAccount), -1);
		Assert.assertEquals(secondAccount.compareTo(account), 1);
		Assert.assertEquals(secondAccount.compareTo(secondAccount), 0);
	}
	
	@AfterClass
	public static void breakDown() {
		
	}

}
