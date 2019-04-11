package com.zero.admin.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zero.account.Account;
import com.zero.account.Account.Type;
import com.zero.account.JointAccount;
import com.zero.account.SingleAccount;
import com.zero.database.Database;

public class Customer extends User{
	private List<Account> mAccounts = new ArrayList<>();
	private Account currentAccountOnHand;

	public Customer(int id, String username, String password, String email, String firstName, String lastName, String address, String city, String state, String zip, String phone) throws FileNotFoundException, ClassNotFoundException, IOException, SQLException {
		super(id, username, password, email, firstName, lastName, Role.Customer, address, city, state, zip, phone);
	}
	
	public Account getCurrentAccountOnHand() {
		return currentAccountOnHand;
	}
	
	public void setCurrentAccountOnHand(Account account) {
		if(account == null)
			throw new IllegalArgumentException();
		
		currentAccountOnHand = account;
	}
	
	public List<Account> getAllAccounts(){
		return mAccounts;
	}
	
	public List<Account> pullAllAccounts() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException{
		
		mAccounts = new ArrayList<>();
		
		Database db = Database.getDatabase();
		
		String sql = "select account.id as id, users.id as ownerId, account.label as label, "
				+ "account.amount as amount, account.type as type, account.ownership as ownership, account.status as status\r\n" + 
				"from account_owner\r\n" + 
				"join users users.id = account_owner.id\r\n" + 
				"join account account.id = account_owner.ownerid\r\n" + 
				"where account_owner.ownerid = " + this.id;
		
		ResultSet result = db.executeQuery(sql);
		
		Map<Integer, Account> accounts = new HashMap<>();
		
		if(result.next()) {
			
			do {
				
				int accountId = result.getInt(1);
				int ownerId = result.getInt(2);
				String label = result.getString(3);
				double amount = result.getDouble(4);
				String dbType = result.getString(5);
				String dbOwnership = result.getString(6);
				String dbStatus = result.getString(7);
				
				Account account = null;
				
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
					accounts.put(accountId, account);
					
				} else if(dbOwnership.equals("Joint")) {
					
					if(accounts.containsKey(accountId)) {
						((JointAccount)accounts.get(accountId)).setSecondOwnerId(ownerId);
					} else {
						account = new JointAccount(accountId, label, ownerId, amount, type, status);
						accounts.put(accountId, account);
					}
					
				}
				
			} while(result.next());
			
		}
		
		for(Integer key: accounts.keySet()) {
			mAccounts.add(accounts.get(key));
		}
		
		return mAccounts;
	}
	

	public List<Account> getAllAccountsExceptOne(int accountIdToLeaveOut) throws FileNotFoundException, ClassNotFoundException, IOException, SQLException{
		if(accountIdToLeaveOut < 1)
			throw new IllegalArgumentException();
		
		List<Account> resultAccounts = new ArrayList<>();
		
		Connection conn = Database.getConnection();
		
		String sql = "select account.id as id, users.id as ownerId, account.label as label, "
				+ "account.amount as amount, account.type as type, account.ownership as ownership, account.status as status\r\n" + 
				"from account_owner\r\n" + 
				"join users users.id = account_owner.id\r\n" + 
				"join account account.id = account_owner.ownerid\r\n" + 
				"where account_owner.ownerid = ? AND " + 
				"NOT account.id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, this.id);
		st.setInt(2, accountIdToLeaveOut);
		
		ResultSet result = st.executeQuery();
		
		Map<Integer, Account> accounts = new HashMap<>();
		
		if(result.next()) {
			
			do {
				
				int accountId = result.getInt(1);
				int ownerId = result.getInt(2);
				String label = result.getString(3);
				double amount = result.getDouble(4);
				String dbType = result.getString(5);
				String dbOwnership = result.getString(6);
				String dbStatus = result.getString(7);
				
				Account account = null;
				
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
					accounts.put(accountId, account);
					
				} else if(dbOwnership.equals("Joint")) {
					
					if(accounts.containsKey(accountId)) {
						((JointAccount)accounts.get(accountId)).setSecondOwnerId(ownerId);
					} else {
						account = new JointAccount(accountId, label, ownerId, amount, type, status);
						accounts.put(accountId, account);
					}
					
				}
				
			} while(result.next());
			
		}
		
		for(Integer key: accounts.keySet()) {
			resultAccounts.add(accounts.get(key));
		}
		
		return resultAccounts;
	}

}
