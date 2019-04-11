package com.zero.admin.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zero.account.Account;
import com.zero.account.Account.Type;
import com.zero.account.AccountDao;
import com.zero.account.JointAccount;
import com.zero.account.SingleAccount;
import com.zero.admin.Administration;
import com.zero.database.Database;

public class Manager extends User implements UserDao, AccountDao{
	private Customer customerOnHand;
	private Account accountOnHand;

	public Manager(int id, String username, String password, String email, String firstName, String lastName, String address, String city, String state, String zip, String phone) {
		super(id, username, password, email, firstName, lastName, Role.Manager, address, city, state, zip, phone);
	}
	
	public Account getAccountOnHand() {
		return accountOnHand;
	}

	public void setAccountOnHand(Account accountOnHand) {
		if(accountOnHand == null)
			throw new IllegalArgumentException();
		this.accountOnHand = accountOnHand;
	}

	public Customer getCustomerOnHand() {
		return customerOnHand;
	}

	public void setCustomerOnHand(Customer customerOnHand) {
		if(customerOnHand == null)
			throw new IllegalArgumentException();
		this.customerOnHand = customerOnHand;
	}

	public void cancelAccount(int accountId) throws SQLException {
		if(accountId < 1) 
			throw new IllegalArgumentException();
		
		Connection conn = Database.getConnection();
		
		String sql = "update account set status = 'Closed' where id = " + accountId;
		
		Statement st = conn.createStatement();
		
		st.executeUpdate(sql);
		
		conn.close();
	}
	

	
	/*
	 * public List<Account> getPendingAccounts() throws SQLException,
	 * FileNotFoundException, ClassNotFoundException, IOException {
	 * 
	 * List<Account> pendingAccounts = new ArrayList<>();
	 * 
	 * Database db = Database.getDatabase();
	 * 
	 * String sql =
	 * "select account.id as id, users.id as ownerId, account.label as label, " +
	 * "account.amount as amount, account.type as type, account.ownership as ownership\r\n"
	 * + "from account_owner\r\n" + "join users users.id = account_owner.id\r\n" +
	 * "join account account.id = account_owner.ownerid\r\n" +
	 * "where account.status = 'Pending'";
	 * 
	 * ResultSet result = db.executeQuery(sql);
	 * 
	 * Map<Integer, Account> accounts = new HashMap<>();
	 * 
	 * if(result.next()) {
	 * 
	 * do {
	 * 
	 * int accountId = result.getInt(1); int ownerId = result.getInt(2); String
	 * label = result.getString(3); double amount = result.getDouble(4); String
	 * dbType = result.getString(5); String dbOwnership = result.getString(6);
	 * 
	 * Account account = null;
	 * 
	 * Type type = null;
	 * 
	 * if(dbType.equals("Checking")) { type = Type.Checking; } else
	 * if(dbType.equals("Saving")) { type = Type.Saving; } else
	 * if(dbType.equals("MoneyMarket")) { type = Type.MoneyMarket; } else
	 * if(dbType.equals("IRA")) { type = Type.IRA; } else
	 * if(dbType.equals("Brokerage")) { type = Type.Brokerage; } else
	 * if(dbType.equals("CD")) { type = Type.CD; }
	 * 
	 * if(dbOwnership.equals("Single")) {
	 * 
	 * account = new SingleAccount(accountId, label, ownerId, amount, type,
	 * Account.Status.Pending); accounts.put(accountId, account);
	 * 
	 * } else if(dbOwnership.equals("Joint")) {
	 * 
	 * if(accounts.containsKey(accountId)) {
	 * ((JointAccount)accounts.get(accountId)).setSecondOwnerId(ownerId); } else {
	 * account = new JointAccount(accountId, label, ownerId, amount, type,
	 * Account.Status.Pending); accounts.put(accountId, account); }
	 * 
	 * }
	 * 
	 * } while(result.next());
	 * 
	 * }
	 * 
	 * for(Integer key: accounts.keySet()) { pendingAccounts.add(accounts.get(key));
	 * }
	 * 
	 * return pendingAccounts; }
	 */
	
	
	
	public User getUserByUsername(String username) throws FileNotFoundException, ClassNotFoundException, SQLException, IOException {
		if(username == null)
			throw new IllegalArgumentException();
		return Administration.getAdministration().getUserByUsername(username);
	}
}
	
