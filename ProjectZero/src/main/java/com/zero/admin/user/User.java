package com.zero.admin.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.zero.account.Account;
import com.zero.account.Account.Type;
import com.zero.account.AccountDao;
import com.zero.account.JointAccount;
import com.zero.account.SingleAccount;
import com.zero.database.Database;

import lombok.ToString;

@ToString
public abstract class User implements Comparable<User>, Serializable, AccountDao{
	private static final Logger LOGGER =  LogManager.getLogger(User.class);
	protected int id;
	private String username, password, email, firstName, lastName, address, city, state, zip, phone;
	protected Role role;

	
	public static enum Role{
		Employee, Manager, Administrator, Customer;
	}
	
	public static enum Status{
		Active, Inactive, Banned;
	}
	
	public User(int id, String username, String password, String email, String firstName, String lastName, Role role, String address, String city, String state, String zip, String phone) {
		
		if(id < 1)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(username)) {
			throw new IllegalArgumentException();
		}
		
		if(username.length() < 4)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(password)) {
			throw new IllegalArgumentException();
		}
		
		if(password.length() < 3)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(email)) {
			throw new IllegalArgumentException();
		}
		
		if(email.length() < 4)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(firstName)) {
			throw new IllegalArgumentException();
		}
		
		if(firstName.length() < 2)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(lastName)) {
			throw new IllegalArgumentException();
		}
		
		if(lastName.length() < 2)
			throw new IllegalArgumentException();
		
		if(role == null)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(address)) {
			throw new IllegalArgumentException();
		}
		
		if(address.length() < 2)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(city)) {
			throw new IllegalArgumentException();
		}
		
		if(city.length() < 4) {
			throw new IllegalArgumentException();
		}
		
		if(!StringUtils.isNoneBlank(state)) {
			throw new IllegalArgumentException();
		}
		
		if(state.length() < 1)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(zip)) {
			throw new IllegalArgumentException();
		}
		
		if(zip.length() < 4)
			throw new IllegalArgumentException();
		
		if(!StringUtils.isNoneBlank(phone)) {
			throw new IllegalArgumentException();
		}
		
		if(phone.length() < 7)
			throw new IllegalArgumentException();
		
		this.id = id; 
		this.username = username;
		this.password = password;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phone = phone;
		this.role = role;
	} 
	
	@Override
	public int hashCode() { 
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj == null) 
			throw new IllegalArgumentException();
		
		
		if(!(obj instanceof User)) //classic check for instanceOf (capital O?)
			return false;
		
		
		return id == ((User)obj).id;
	}
	
	@Override
	public int compareTo(User o) {
		
		if(o == null) 
			throw new IllegalArgumentException();
		
		
		return id - ((User)o).id;
	}

	public void setPassword(String password) {  //create password from application
		
		if(!StringUtils.isNoneBlank(password)) {
			throw new IllegalArgumentException();
		}
		
		if(password.length() < 3)
			throw new IllegalArgumentException();
		
		//TODO do validation
		this.password = password;
	}
	
	public void changePassword(String password) throws SQLException {
		if(!StringUtils.isNoneBlank(password)) {
			throw new IllegalArgumentException();
		}
		
		if(password.length() < 3)
			throw new IllegalArgumentException();
		setPassword(password);
		commitPassword();
	}
	
	public void commitPassword() throws SQLException {
		LOGGER.info("Changing user's password");
		
		Connection conn = Database.getConnection();
		
		String sql = "update users set password = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, password);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}

	public void setUsername(String username) {  //create username from application
		if(!StringUtils.isNoneBlank(username)) {
			throw new IllegalArgumentException();
		}
		
		if(username.length() < 4)
			throw new IllegalArgumentException();
		
		this.username = username;
	}
	
	public void changeUsername(String username) throws SQLException {
		if(!StringUtils.isNoneBlank(username)) {
			throw new IllegalArgumentException();
		}
		
		if(username.length() < 4)
			throw new IllegalArgumentException();
		
		setUsername(username);
		commitUserName();
	}
	
	public void commitUserName() throws SQLException {
		LOGGER.info("Changing user's username");
		
		Connection conn = Database.getConnection();
		
		String sql = "update users set username = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, username);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
		
	}
	
	public String getUsername() { //self explanatory
		return username;
	}
	public List<Account> getAccountsByUsername(String username) throws FileNotFoundException, ClassNotFoundException, IOException, SQLException{
		if(!StringUtils.isNoneBlank(username)) {
			throw new IllegalArgumentException();
		}
		LOGGER.info("Getting all accounts held by " + username);
		
		ArrayList<Account> resultAccount = new ArrayList<>();
		
		Connection conn = Database.getConnection();
		
		String sql = "select account.id as id, users.id as ownerId, account.label as label, "
				+ "account.amount as amount, account.type as type, account.ownership as ownership, account.status as status " + 
				"from account_owner " + 
				"join users on users.id = account_owner.ownerid " + 
				"join account on account.id = account_owner.id " + 
				"where users.username = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1,  username);
		
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
			resultAccount.add(accounts.get(key));
		}
		
		conn.close();
		
		return resultAccount;
	}
	public void setFirstName(String firstName) { 
		
		if(!StringUtils.isNoneBlank(firstName)) 
			throw new IllegalArgumentException();
		
		if(firstName.length() < 2)
			throw new IllegalArgumentException();
		
		this.firstName = firstName;
	}
	
	public void changeFirstName(String username) throws SQLException {
		if(!StringUtils.isNoneBlank(firstName)) 
			throw new IllegalArgumentException();
		
		if(firstName.length() < 2)
			throw new IllegalArgumentException();
		setFirstName(username);
		commitFirstName();
	}
	
	public void commitFirstName() throws SQLException {
		LOGGER.info("Changing user's first name");
		Connection conn = Database.getConnection();
		
		String sql = "update users set firstname = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, firstName);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}
	public int getId() {
		return this.id;
	}
	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		
		if(!StringUtils.isNoneBlank(lastName)) 
			throw new IllegalArgumentException();
		
		if(lastName.length() < 2)
			throw new IllegalArgumentException();
		
		this.lastName = lastName;
	}
	
	public void changeLastName(String lastName) throws SQLException {
		if(!StringUtils.isNoneBlank(lastName)) 
			throw new IllegalArgumentException();
		
		if(lastName.length() < 2)
			throw new IllegalArgumentException();
		setLastName(lastName);
		commitLastName();
	}
	
	public void commitLastName() throws SQLException {
		LOGGER.info("Changing user's last name");
		
		Connection conn = Database.getConnection();
		
		String sql = "update users set lastname = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, lastName);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}
	
	public String getLastName() {
		return lastName;
	}

	public void setEmail(String email) {
		if(!StringUtils.isNoneBlank(email)) 
			throw new IllegalArgumentException();
		
		if(email.length() < 4)
			throw new IllegalArgumentException();
		
		//TODO do validation
		this.email = email;
	}
	
	public void changeEmail(String email) throws SQLException {
		if(!StringUtils.isNoneBlank(email)) 
			throw new IllegalArgumentException();
		if(email.length() < 4)
			throw new IllegalArgumentException();
		setEmail(email);
		commitEmail();
	}
	
	public void commitEmail() throws SQLException {
		LOGGER.info("Changing user's email");
		
		Connection conn = Database.getConnection();
		
		String sql = "update users set email = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, lastName);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZip() {
		return zip;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		if(!StringUtils.isNoneBlank(address)) 
			throw new IllegalArgumentException();
		if(address.length() < 2)
			throw new IllegalArgumentException();
		this.address = address;
	}
	
	public void changeAddress(String address) throws SQLException {
		if(!StringUtils.isNoneBlank(address)) 
			throw new IllegalArgumentException();
		if(address.length() < 2)
			throw new IllegalArgumentException();
		setAddress(address);
		commitAddress();
	}
	
	public void commitAddress() throws SQLException {
		LOGGER.info("Changing user's home address");

		Connection conn = Database.getConnection();
		
		String sql = "update users set homeaddress = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, address);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}

	public void setCity(String city) {
		if(!StringUtils.isNoneBlank(city)) 
			throw new IllegalArgumentException();
		
		if(city.length() < 4)
			throw new IllegalArgumentException();
		
		this.city = city;
	}
	
	public void changeCity(String city) throws SQLException {
		if(!StringUtils.isNoneBlank(city)) 
			throw new IllegalArgumentException();
		
		if(city.length() < 4)
			throw new IllegalArgumentException();
		setCity(city);
		commitCity();
	}
	
	public void commitCity() throws SQLException {
		LOGGER.info("Changing user's city");
		
		Connection conn = Database.getConnection();
		
		String sql = "update users set city = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, city);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}

	public void setState(String state) {
		if(!StringUtils.isNoneBlank(state)) 
			throw new IllegalArgumentException();
		
		if(state.length() < 1)
			throw new IllegalArgumentException();
		
		this.state = state;
	}
	
	public void changeState(String state) throws SQLException {
		if(!StringUtils.isNoneBlank(state)) 
			throw new IllegalArgumentException();
		if(state.length() < 1)
			throw new IllegalArgumentException();
		setState(state);
		commitState();
	}
	
	public void commitState() throws SQLException {
		LOGGER.info("Changing user's state");

		Connection conn = Database.getConnection();
		
		String sql = "update users set state = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, state);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}

	public void setZip(String zip) {
		if(!StringUtils.isNoneBlank(zip)) 
			throw new IllegalArgumentException();
		
		if(zip.length() < 4)
			throw new IllegalArgumentException();
		this.zip = zip;
	}
	
	public void changeZip(String zip) throws SQLException {
		if(!StringUtils.isNoneBlank(zip)) 
			throw new IllegalArgumentException();
		if(zip.length() < 4)
			throw new IllegalArgumentException();
		
		setZip(zip);
		commitZip();
	}
	
	public void commitZip() throws SQLException {
		LOGGER.info("Changing user's zip code");
		
		Connection conn = Database.getConnection();
		
		String sql = "update users set zipcode = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, zip);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		if(!StringUtils.isNoneBlank(phone)) 
			throw new IllegalArgumentException();
		if(phone.length() < 7)
			throw new IllegalArgumentException();
		this.phone = phone;
	}
	
	public void changePhone(String phone) throws SQLException {
		if(!StringUtils.isNoneBlank(phone)) 
			throw new IllegalArgumentException();
		if(phone.length() < 7)
			throw new IllegalArgumentException();
		setPhone(phone);
		commitPhone();
	}
	
	public void commitPhone() throws SQLException {
		LOGGER.info("Changing user's phone number");
		
		Connection conn = Database.getConnection();
		
		String sql = "update users set phone = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, phone);
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}
	
	public Role getRole() {
		return role;
	}

	public static class UsernameComparator implements Comparator<User>{

		@Override
		public int compare(User o1, User o2) {
			
			if(o1 == null) 
				throw new IllegalArgumentException();
			
			
			if(o2 == null) 
				throw new IllegalArgumentException();
			
			
			return ((User)o1).username.compareTo(((User)o2).username);
		}
		
	}
	
	public static class firstNameComparator implements Comparator<User>{

		@Override
		public int compare(User o1, User o2) {
			
			if(o1 == null) 
				throw new IllegalArgumentException();
			
			
			if(o2 == null) 
				throw new IllegalArgumentException();
			
			
			return ((User)o1).firstName.compareTo(((User)o2).firstName);
		}
		
	}
	
	public static class lastNameComparator implements Comparator<User>{

		@Override
		public int compare(User o1, User o2) {
			
			if(o1 == null) 
				throw new IllegalArgumentException();
			
			
			if(o2 == null) 
				throw new IllegalArgumentException();
			
			
			return ((User)o1).lastName.compareTo(((User)o2).lastName);
		}
		
	}
	public void displayPersonalInformation(){
		System.out.println("Personal Information");
		System.out.println("Username: " + this.username);
		System.out.println("Name:" + this.firstName + " " + this.lastName);
		System.out.println("Home Address: " + this.address + " " + this.city + " " + this.state + " " + this.zip + " ");
		System.out.println("Phone Number: " + phone);
	}
	
	public Account submitApplicationForSingleAccount(String label, double initialAmount, Type type) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		if(label == null)
			throw new IllegalArgumentException();
		if(type == null)
			throw new IllegalArgumentException();
		if(initialAmount < 0)
			throw new IllegalArgumentException();
		
		switch(type) {
			case Checking:
				LOGGER.info("Creating checking account: " + label);
				break;
			case Saving:
				LOGGER.info("Creating saving account: " + label);
				break;
			case MoneyMarket:
				LOGGER.info("Creating money market account: " + label);
				break;
			case IRA:
				LOGGER.info("Creating IRA account: " + label);
				break;
			case Brokerage:
				LOGGER.info("Creating brokerage account: " + label);
				break;
			case CD:
				LOGGER.info("Creating CD account: " + label);
				break;
		}
		
		Database db = Database.getDatabase();
		
		int i = db.getLatestAccountId();
		
		SingleAccount account = new SingleAccount(++i, label, this.id, initialAmount, type);
		
		Connection conn = Database.getConnection();
		
		String sql = "insert into account (id, label, amount, type, ownership, status) values  (?, ?, ?, ?, ?, 'Pending')";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, account.getId());
		st.setString(2, label);
		st.setDouble(3, account.getAmount());
		
		switch(type){
			case Checking:
				st.setString(4, "Checking");
				break;
			case Saving:
				st.setString(4, "Saving");
				break;
			case MoneyMarket:
				st.setString(4, "MoneyMarket");
				break;
			case IRA:
				st.setString(4, "IRA");
				break;
			case Brokerage:
				st.setString(4, "Brokerage");
				break;
			case CD:
				st.setString(4, "CD");
				break;
		}
		
		switch(account.getOwnership()) {
			case Single:
				st.setString(5, "Single");
				break;
			case Joint:
				st.setString(5, "Joint");
				break;
		}
		
		st.executeUpdate();
		
		sql = "insert into account_owner values (?, ?)";
		
		st = conn.prepareStatement(sql);
		
		st.setInt(1, account.getId());
		st.setInt(2, this.id);
		
		st.executeUpdate();
		
		conn.close();
			
		return account;
	}
	
	public Account submitApplicationForJointAccount(String label, int secondAccountHolderId, double initialAmount, Type type) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		if(label == null)
			throw new IllegalArgumentException();
		if(type == null)
			throw new IllegalArgumentException();
		if(initialAmount < 0)
			throw new IllegalArgumentException();
		if(secondAccountHolderId < 1)
			throw new IllegalArgumentException();
		
		switch(type) {
			case Checking:
				LOGGER.info("Creating joint checking account: " + label);
				break;
			case Saving:
				LOGGER.info("Creating joint saving account: " + label);
				break;
			case MoneyMarket:
				LOGGER.info("Creating joint money market account: " + label);
				break;
			case IRA:
				LOGGER.info("Creating joint IRA account: " + label);
				break;
			case Brokerage:
				LOGGER.info("Creating joint brokerage account: " + label);
				break;
			case CD:
				LOGGER.info("Creating joint CD account: " + label);
				break;
		}
		
		Database db = Database.getDatabase();
		
		int i = db.getLatestAccountId();
		
		JointAccount account = new JointAccount(++i, label, this.id, secondAccountHolderId, initialAmount, type);
		
		Connection conn = Database.getConnection();
		
		String sql = "insert into account (id, label, amount, type, ownership, status) values  (?, ?, ?, ?, ?, 'Pending')";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, account.getId());
		st.setString(2, label);
		st.setDouble(3, account.getAmount());
		
		switch(type){
			case Checking:
				st.setString(4, "Checking");
				break;
			case Saving:
				st.setString(4, "Saving");
				break;
			case MoneyMarket:
				st.setString(4, "MoneyMarket");
				break;
			case IRA:
				st.setString(4, "IRA");
				break;
			case Brokerage:
				st.setString(4, "Brokerage");
				break;
			case CD:
				st.setString(4, "CD");
				break;
		}
		
		st.setString(5, "Joint");
		
		st.executeUpdate();
		
		sql = "insert into account_owner values (?, ?)";
		
		st = conn.prepareStatement(sql);
		
		st.setInt(1, account.getId());
		st.setInt(2, this.id);
		
		st.executeUpdate();
		
		st = conn.prepareStatement(sql);
		
		st.setInt(1, account.getId());
		st.setInt(2, secondAccountHolderId);
		
		st.executeUpdate();
		
		conn.close();
			
		return account;
		
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
	
	public void withdrawFunds(int accountId, double amount) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		if(accountId < 1)
			throw new IllegalArgumentException();
		if(amount < 0)
			throw new IllegalArgumentException();
		
		LOGGER.info("Withdrawing funds from " + accountId);
		
		Connection conn = Database.getConnection();
		
		String sql = "select * from account where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, accountId);
		
		ResultSet result = st.executeQuery();
		
		if(result.next()) {
			
			double balance = result.getDouble(3);
			
			double remaining = balance - amount;
			
			conn.close();
			
			if(remaining < 0) {
				LOGGER.error("Balance will drop below zero");
				throw new NegativeBalanceException("Balance will drop below zero");
			}
			
			sql = "update account set amount = " + remaining + " "
					+ "where id = " + accountId;
			
			Database.getDatabase().executeUpdate(sql);
			
		} else {
			LOGGER.error("accountId: " + accountId + " does not exist.");
			throw new AccountDoesNotExistException("accountId: " + accountId + " does not exist." );
		}
		
	}
	
	public void depositFunds(int accountId, double increment) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		if(accountId < 1)
			throw new IllegalArgumentException();
		if(increment < 0)
			throw new IllegalArgumentException();
		
		LOGGER.info("Depositing funds to " + accountId);
		
		Connection conn = Database.getConnection();
		
		String sql = "select * from account where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, accountId);
		
		ResultSet result = st.executeQuery();
		
		if(result.next()) {
			
			double balance = result.getDouble(3);
			
			double remaining = balance + increment;
			
			conn.close();
			
			sql = "update account set amount = " + remaining + " "
					+ "where id = " + accountId;
			
			Database.getDatabase().executeQuery(sql);
			
		} else {
			LOGGER.error("accountId: " + accountId + " does not exist.");
			throw new AccountDoesNotExistException("accountId: " + accountId + " does not exist." );
		}
	}
	
	public void transferFunds(int fromId, int toId, double increment) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		if(fromId < 1)
			throw new IllegalArgumentException();
		if(toId < 1)
			throw new IllegalArgumentException();
		if(increment < 0)
			throw new IllegalArgumentException();
		
		LOGGER.info("transferring funds from account number " + fromId + " to account number " + toId );
		
		Connection conn = Database.getConnection();
		
		String sql = "select * from account where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, fromId);
		
		ResultSet result = st.executeQuery();
		
		if(!result.next()) {
			LOGGER.error("accountId: " + fromId + " does not exist.");
			throw new AccountDoesNotExistException("accountId: " + fromId + " does not exist." );
		}
		
		st = conn.prepareStatement(sql);
		
		st.setInt(1, toId);
		
		result = st.executeQuery();
		
		if(!result.next()) {
			LOGGER.error("accountId: " + toId + " does not exist.");
			throw new AccountDoesNotExistException("accountId: " + toId + " does not exist." );
		}
		
		st = conn.prepareStatement(sql);
		
		st.setInt(1, fromId);
		
		result = st.executeQuery();
		
		result.next();
		
		double balance = result.getDouble(3);
		
		double remaining = balance - increment;
		
		if(remaining < 0) {
			LOGGER.error("Balance will drop below zero");
			throw new NegativeBalanceException("Balance will drop below zero");
		}
		
		sql = "update account set amount = " + remaining + " "
				+ "where id = ?";
		
		st = conn.prepareStatement(sql);
		
		st.setInt(1, fromId);
		
		st.executeUpdate();
		
		sql = "select * from account where id = ?";
		
		st = conn.prepareStatement(sql);
		
		st.setInt(1, toId);
		
		result = st.executeQuery();
		
		result.next();
		
		balance = result.getDouble(3);
		
		remaining = balance + increment;
		
		sql = "update account set amount = " + remaining + " "
				+ "where id = ?";
		
		st = conn.prepareStatement(sql);
		
		st.setInt(1, toId);
		
		conn.close();
	}
	public void disapproveAccount(int accountId) throws SQLException {
		
		Connection conn = Database.getConnection();
		
		String sql = "update account set status = 'Denied' where id = " + accountId;
		
		Statement st = conn.createStatement();
		
		st.executeUpdate(sql);
		
		conn.close();
	}
	
	public void approveAccount(int accountId) throws SQLException {

		Connection conn = Database.getConnection();
		
		String sql = "update account set status = 'Approved' where id = " + accountId;
		
		Statement st = conn.createStatement();
		
		st.executeUpdate(sql);
		
		conn.close();
		
	}
	public List<Account> getPendingAccounts() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		
		List<Account> pendingAccounts = new ArrayList<>();
		
		Connection conn = Database.getConnection();
		
		String sql = "select account.id as id, users.id as ownerId, account.label as label, "
				+ "account.amount as amount, account.type as type, account.ownership as ownership " + 
				"from account_owner " + 
				"join users on users.id = account_owner.ownerid " + 
				"join account on account.id = account_owner.id " + 
				"where account.status = 'Pending'";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
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
				
				if(dbOwnership.equals("Single")) {
					
					account = new SingleAccount(accountId, label, ownerId, amount, type, Account.Status.Pending);
					accounts.put(accountId, account);
					
				} else if(dbOwnership.equals("Joint")) {
					
					if(accounts.containsKey(accountId)) {
						((JointAccount)accounts.get(accountId)).setSecondOwnerId(ownerId);
					} else {
						account = new JointAccount(accountId, label, ownerId, amount, type, Account.Status.Pending);
						accounts.put(accountId, account);
					}
					
				}
				
			} while(result.next());
			
		}
		
		for(Integer key: accounts.keySet()) {
			pendingAccounts.add(accounts.get(key));
		}
		
		conn.close();
		
		return pendingAccounts;
	}
	
	public User getUserById(int userId) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		if(userId < 1)
			throw new IllegalArgumentException();
		
		Connection conn = Database.getConnection();
		
		String sql = "select * from users where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, userId);
		
		ResultSet result = st.executeQuery();
		
		User user = null;
		
		if(result.next()) {
			int dbId = result.getInt(result.findColumn("id"));
			
			String usernameDb = result.getString(result.findColumn("username"));
			String password = result.getString(result.findColumn("password"));
			String email = result.getString(result.findColumn("email"));
			String firstName = result.getString(result.findColumn("firstName"));
			String lastName = result.getString(result.findColumn("lastName"));
			String homeAddress = result.getString(result.findColumn("homeAddress"));
			String city = result.getString(result.findColumn("city"));
			String state = result.getString(result.findColumn("state"));
			String zipcode = result.getString(result.findColumn("zipcode"));
			String phone = result.getString(result.findColumn("phone"));
			String role = result.getString(result.findColumn("role"));
			
			if(role.equals("Employee")) {
				user = new Employee(dbId, usernameDb, password, email, firstName, lastName, homeAddress, city, state, zipcode, phone);
			} else if(role.equals("Manager")) {
				user = new Manager(dbId, usernameDb, password, email, firstName, lastName, homeAddress, city, state, zipcode, phone);
			} else if(role.equals("Administrator")) {
				user = new Administrator(dbId, usernameDb, password, email, firstName, lastName, homeAddress, city, state, zipcode, phone);
			} else if(role.equals("Customer")) {
				user = new Customer(dbId, usernameDb, password, email, firstName, lastName, homeAddress, city, state, zipcode, phone);
			}
		}
		
		
		conn.close();
		
		return user;
	}
}
