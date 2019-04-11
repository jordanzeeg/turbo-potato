package com.zero.admin.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.zero.account.Account;
import com.zero.account.Account.Type;
import com.zero.account.JointAccount;
import com.zero.account.SingleAccount;
import com.zero.database.Database;

public class Employee extends User {
	private Customer customerOnHand;
	private Account accountOnHand;
	private List<Account> accountsOnHand;

	public Employee(int id, String username, String password, String email, String firstName, String lastName, String address, String city, String state, String zip, String phone) {
		super(id, username, password, email, firstName, lastName, Role.Employee, address, city, state, zip, phone);
	}
	
	public Customer getCustomerOnHand() {
		return customerOnHand;
	}

	public void setCustomerOnHand(Customer customerOnHand) {
		if(customerOnHand == null)
			throw new IllegalArgumentException();
		this.customerOnHand = customerOnHand;
	}

	public Account getCustomerAccountOnHand() {
		return accountOnHand;
	}
	
	public void setCustomerAccountOnHand(Account account) {
		if(account == null)
			throw new IllegalArgumentException();
		accountOnHand = account;
	}
	
	public List<Account> getAccountsOnHand() {
		return accountsOnHand;
	}

	public void setAccountsOnHand(List<Account> accountsOnHand) {
		if(accountsOnHand == null)
			throw new IllegalArgumentException();
		this.accountsOnHand = accountsOnHand;
	}
	

	
	public Account getAccountById(int id) throws FileNotFoundException, ClassNotFoundException, SQLException, IOException{
		if(id < 1)
			throw new IllegalArgumentException();
		
		Connection conn = Database.getConnection();
		
		String sql = "select account.id as accountId, account.label as label, account.amount as amount, account.type as type, account.ownership as ownership, account.status as status, user.id as userid "
				+ "from account "
				+ "join account_owner on account_owner.id = account.id "
				+ "where account.id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, id);
		
		ResultSet result = st.executeQuery();
		
		Account account = null;
		
		if(result.next()) {
			
			int accountId = result.getInt(1);
			String label = result.getString(2);
			double amount = result.getDouble(3);
			String dbType = result.getString(4);
			String dbOwnership = result.getString(5);
			String dbStatus = result.getString(6);
			int ownerId = result.getInt(7);
			
			Type type = null;
			
			if(dbType.equals("Checking")) {
				type = Type.Checking;
			} else if(dbType.equals("Saving")) {
				type = Type.Saving;
			}  else if(dbType.equals("MoneyMarket")) {
				type = Type.MoneyMarket;
			}  else if(dbType.equals("IRA")) {
				type = Type.IRA;
			}  else if(dbType.equals("Brokerage")) {
				type = Type.Brokerage;
			}  else if(dbType.equals("CD")) {
				type = Type.CD;
			}
			
			Account.Status status = null;
			
			if(dbStatus.equals("Denied")) {
				status = Account.Status.Denied;
			} else if(dbStatus.equals("Approved")) {
				status = Account.Status.Approved;
			} else if(dbStatus.equals("Closed")) {
				status = Account.Status.Closed;
			} else if(dbStatus.equals("Pending")) {
				status = Account.Status.Pending;
			}
			
			if(dbOwnership.equals("Single")) {
				account = new SingleAccount(accountId, label, ownerId, amount, type, status);
			} else if(dbOwnership.equals("Joint")) {
				account = new JointAccount(accountId, label, ownerId, amount, type, status);
				
				result.next();
				
				ownerId = result.getInt(7);
				
				JointAccount joint = (JointAccount)account;
				
				joint.setSecondOwnerId(ownerId);
			}
			
		}
		
		conn.close();
		
		return account;
		
	}

}
