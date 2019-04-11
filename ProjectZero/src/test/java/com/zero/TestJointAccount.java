package com.zero;

import org.junit.Assert;
import org.junit.Test;

import com.zero.account.Account;
import com.zero.account.Account.Status;
import com.zero.account.JointAccount;

public class TestJointAccount {
	
	@Test(expected = IllegalArgumentException.class)
	public void testJointAccountConstructor() {
		JointAccount account = new JointAccount(-1, "Gifts", 1, 1.0, Account.Type.Checking, Status.Pending);
		account = new JointAccount(1, null, 1, 1.0, Account.Type.Checking, Status.Pending);
		account = new JointAccount(1, "Gifts", -1, 1.0, Account.Type.Checking, Status.Pending);
		account = new JointAccount(1, "Gifts", 1, -1.0, Account.Type.Checking, Status.Pending);
		account = new JointAccount(1, "Gifts", 1, 1.0, null, Status.Pending);
		account = new JointAccount(1, "Gifts", 1, 1.0, Account.Type.Checking, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSetSecondOwnerId() {
		JointAccount account = new JointAccount(1, "Gifts", 1, 1.0, Account.Type.Checking, Status.Pending);
		account.setSecondOwnerId(-1);
	}
	
	@Test
	public void testSingleAccountCompareTo() {
		JointAccount account = new JointAccount(1, "Gifts", 1, 1.0, Account.Type.Checking, Account.Status.Pending);
		JointAccount secondAccount = new JointAccount(2, "GiftsAndStuff", 1, 1.0, Account.Type.Checking, Account.Status.Pending);
		
		Assert.assertEquals(account.compareTo(secondAccount), -1);
		Assert.assertEquals(secondAccount.compareTo(account), 1);
		Assert.assertEquals(secondAccount.compareTo(secondAccount), 0);
	}

}
