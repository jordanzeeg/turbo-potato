package com.zero.admin.user;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.zero.database.Database;

public class Administrator extends User{

	public Administrator(int id, String username, String password, String email, String firstName, String lastName, String address, String city, String state, String zip, String phone) {
		super(id, username, password, email, firstName, lastName, Role.Administrator, address, city, state, zip, phone);
	}
	
	public void changeRole(int id, Role role) throws SQLException {
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
	
	public Employee createEmployee(String username, String password, String email, String firstName, String lastName, String address, String city, String state, String zip, String phone) throws SQLException, IOException, ClassNotFoundException, IOException  {
		//initialize fields into employee; 
		//factory method
		//call constructor from employee class
		
		Database db = Database.getDatabase();
		
		int i = db.getLatestUserId();
		
		Employee employee = new Employee(++i, username, password, email, firstName, lastName, address, city, state, zip, phone);
		
		String sql = "insert into users values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'United States', 'Employee')";
		
		Connection conn = DriverManager.getConnection(db.getDbUrl(), db.getDbUsername(), db.getDbPassword());
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, i); //id
		st.setString(2, username); //username
		st.setString(3, password); //password
		st.setString(4, email); //email
		st.setString(5, firstName); //firstName
		st.setString(6, lastName); //lastName
		st.setString(7, address); //address
		st.setString(8, city); //city
		st.setString(9, state); //state
		st.setString(10, zip); //zip
		st.setString(11, phone); //phone
		
		st.executeUpdate();
		
		return employee;
	}
	
	public Manager createManager(String username, String password, String email, String firstName, String lastName, String address, String city, String state, String zip, String phone) throws SQLException, IOException, ClassNotFoundException, IOException  {
		//initialize fields into Manager; 
		//factory method
		//call constructor from manager class
		
		Database db = Database.getDatabase();
		
		int i = db.getLatestUserId();
		
		Manager manager = new Manager(++i, username, password, email, firstName, lastName, address, city, state, zip, phone);
		
		String sql = "insert into users values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'United States', 'Manager')";
		
		Connection conn = DriverManager.getConnection(db.getDbUrl(), db.getDbUsername(), db.getDbPassword());
		
		PreparedStatement st = conn.prepareStatement(sql);
		
		st.setInt(1, i); //id
		st.setString(2, username); //username
		st.setString(3, password); //password
		st.setString(4, email); //email
		st.setString(5, firstName); //firstName
		st.setString(6, lastName); //lastName
		st.setString(7, address); //address
		st.setString(8, city); //city
		st.setString(9, state); //state
		st.setString(10, zip); //zip
		st.setString(11, phone); //phone
		
		st.executeUpdate();
		
		return manager;
		
	}
		

}
