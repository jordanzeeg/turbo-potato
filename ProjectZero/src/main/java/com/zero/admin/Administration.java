package com.zero.admin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.zero.admin.user.Administrator;
import com.zero.admin.user.Customer;
import com.zero.admin.user.Employee;
import com.zero.admin.user.Manager;
import com.zero.admin.user.User;
import com.zero.admin.user.User.Role;
import com.zero.admin.user.UserDao;
import com.zero.database.Database;

public class Administration implements UserDao{ //God has arrived
	private static Administration sAdmin;
	private static final Logger LOGGER =  LogManager.getLogger(Administration.class);
	
	private Administration() {}
	
	public static Administration getAdministration() { //there is only one god and there are no gods before him
		
		if(sAdmin != null) {
			return sAdmin;
		}
		
		sAdmin = new Administration();
		
		return sAdmin;
		
	}
	//create functions should have more inputs to match constructor of user
	//create functions should have a field for role
	public Customer createCustomer(String username, String password, String email, String firstName, String lastName, String address, String city, String state, String zip, String phone) throws SQLException, IOException, ClassNotFoundException, IOException {
		
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
		
		Database db = Database.getDatabase();
		
		int i = db.getLatestUserId();
		
		Customer customer = new Customer(++i, username, password, email, firstName, lastName, address, city, state, zip, phone);
		
		String call = "{call auto_increment_users(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
		
		Connection conn = DriverManager.getConnection(db.getDbUrl(), db.getDbUsername(), db.getDbPassword());
		
		CallableStatement cstmt = conn.prepareCall(call);
		
		cstmt.setString(1, username); //username
		cstmt.setString(2, password); //password
		cstmt.setString(3, email); //email
		cstmt.setString(4, firstName); //firstName
		cstmt.setString(5, lastName); //lastName
		cstmt.setString(6, address); //address
		cstmt.setString(7, city); //city
		cstmt.setString(8, state); //state
		cstmt.setString(9, zip); //zip
		cstmt.setString(10, phone); //phone
		cstmt.setString(11, "Customer");
		
		cstmt.executeUpdate();
		
		String str = "select * from users where id = ?";
		
		cstmt = conn.prepareCall(str);
		
		int latest = db.getLatestUserId();
		
		cstmt.setInt(1, latest);
		
		ResultSet query = cstmt.executeQuery();
		
		query.next();
		
		customer = new Customer(query.getInt(1), query.getString(2), query.getString(3), query.getString(4), query.getString(5), query.getString(6), query.getString(7), query.getString(8), query.getString(9), query.getString(10), query.getString(11));
		
		cstmt.close();
		
		LOGGER.info("new customer created: " + customer.getUsername());
		
		return customer;
	}yo
	
	public boolean checkUserName(String username) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		if(username == null)
			throw new IllegalArgumentException();
		
		Connection conn = Database.getConnection();
		
		String sql = "select username from users where username = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1,  username);
		
		//executeQuery to search for usernames
		ResultSet resultSet = st.executeQuery();
		
		boolean result = resultSet.next();
		
		conn.close();
		
		return result;
	}
	
	public boolean checkPassword(String username, String password) throws SQLException {
		if(username == null)
			throw new IllegalArgumentException();
		if(password == null)
			throw new IllegalArgumentException();
		//executeQuery to search for password
		
		Connection conn = Database.getConnection();
		
		String sql = "select password from users where username = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, username);
		
		ResultSet result = st.executeQuery();
		
		String dbPassword = null;
		
		if(result.next()) {
			dbPassword = result.getString(1);
		} else {
			return false;
		}

		return password.equals(dbPassword);
	}
	
	public User getUserByUsername(String username) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		if(username == null)
			throw new IllegalArgumentException();
		
		Connection conn = Database.getConnection();
		
		String sql = "select * from users where username LIKE ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setString(1, username + "%");
		
		ResultSet result = st.executeQuery();
		
		User user = null;
		
		if(result.next()) {
			int id = result.getInt(result.findColumn("id"));
			
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
				user = new Employee(id, usernameDb, password, email, firstName, lastName, homeAddress, city, state, zipcode, phone);
			} else if(role.equals("Manager")) {
				user = new Manager(id, usernameDb, password, email, firstName, lastName, homeAddress, city, state, zipcode, phone);
			} else if(role.equals("Administrator")) {
				user = new Administrator(id, usernameDb, password, email, firstName, lastName, homeAddress, city, state, zipcode, phone);
			} else if(role.equals("Customer")) {
				user = new Customer(id, usernameDb, password, email, firstName, lastName, homeAddress, city, state, zipcode, phone);
			}
		}
		
		
		conn.close();
		
		return user;
	}
	
	public static void changeRole(int id, Role role) throws SQLException {
		if(id < 1)
			throw new IllegalArgumentException();
		if(role == null)
			throw new IllegalArgumentException();
		
		Connection conn = Database.getConnection();
		
		String sql = "update users set role = ? where id = ?";
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		
		if(role.equals(Role.Employee)) {
			st.setString(1, "Employee");
			st.setInt(2, id);
		} else if(role.equals(Role.Customer)) {
			st.setString(1, "Customer");
			st.setInt(2, id);
		} else if(role.equals(Role.Manager)) {
			st.setString(1, "Manager");
			st.setInt(2, id);
		} else if(role.equals(Role.Administrator)) {
			st.setString(1, "Administrator");
			st.setInt(2, id);
		} 
		
		st.setInt(2, id);
		
		st.executeUpdate();
		
		conn.close();
	}

	//I think there are some functions missing from here

}
