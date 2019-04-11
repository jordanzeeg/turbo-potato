package com.zero.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Database {
	public static final Logger LOGGER =  LogManager.getLogger(Database.class);
	
	private static Database sDatabase = null;
	
	//access information for database
	private String username; 
	private String password;
	private String url;
	
	private Database() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		
		Properties properties = new Properties();
		
		properties.load(new FileReader("src/main/resources/config.properties"));
		
		url = properties.getProperty("url");
		username = properties.getProperty("username");
		password = properties.getProperty("password");
		
		if(!doesDatabaseExists()) { //checks if database exists and creates it;
			LOGGER.info("database does not exist! creating");
			createDatabase();
		}
		
	}
	//singleton because you only want one database
	public static Database getDatabase() throws FileNotFoundException, ClassNotFoundException, IOException, SQLException {
		
		if(sDatabase != null) {//check for database
			return sDatabase;  //return ye olde object if exists
		}
		
		sDatabase = new Database();
		
		return sDatabase;
		
	}
	
	public ResultSet executeQuery(String sql) throws SQLException { //get data from database
		if(!StringUtils.isNoneBlank(sql)){
			throw new IllegalArgumentException(); //catch this
		}
		
		Connection connection = DriverManager.getConnection(url, username, password);
		Statement st = connection.createStatement(); //sql statement
		
		ResultSet result = st.executeQuery(sql); //execute sql that was passed through
		
		connection.close();
		
		return result;
	}
	
	public int executeUpdate(String sql) throws SQLException { //update data using sql string passed in
		if(!StringUtils.isNoneBlank(sql)){
			throw new IllegalArgumentException(); //catch this
		}
		
		Connection connection = DriverManager.getConnection(url, username, password);
		Statement st = connection.createStatement(); //sql statement
		
		int result = st.executeUpdate(sql); //execute sql
		
		connection.close(); 
		
		return result;
	}
	
	public void createDatabase() throws FileNotFoundException, IOException, SQLException { //still needs to actually create database
		LOGGER.info("Creating database....");
		FileReader reader = new FileReader(new File("src/main/resources", "schema.sql"));
		
		int i = reader.read();
		
		StringBuilder builder = new StringBuilder();
		
		while(i != -1) {
			builder.append((char)i);
			i = reader.read();
		}
		
		reader.close();
		
		String sql = builder.toString();
		
		Connection connection = DriverManager.getConnection(url, username, password);
		Statement st = connection.createStatement(); //sql statement
		
		String[] sqls = sql.split("\r\n\r\n");
		
		for(int x = 0; x < sqls.length; x++) {
			st.addBatch(sqls[x]);
		}
		
		st.executeBatch(); //execute sql
		
		connection.close();
		
	}
	
	public boolean doesDatabaseExists() throws SQLException { //called on line 36
		Connection connection = DriverManager.getConnection(url, username, password);
		Statement st = connection.createStatement();
		
		String sql = "select table_name from user_tables"; //sql to return all data from DB
		
		ResultSet result = st.executeQuery(sql); //execute sql string
		
		boolean exists = result.next();
		
		connection.close();
		
		return exists;
	}
	
	public void recreateDatabase() throws SQLException, FileNotFoundException, IOException { //presumably called to create database after destroydatabase
		LOGGER.info("Recreating database....");
		
		if(doesDatabaseExists()) {
			destroyDatabase();
		}
		
		createDatabase();
	}
	
	public void destroyDatabase() throws SQLException { //an important part of any doomsday device
		LOGGER.warn("Destroying database...");
		
		Connection conn = Database.getConnection();
		
		String sql = "drop table account purge";
		
		Statement st = conn.createStatement();
		
		st.executeUpdate(sql);
		
		sql = "drop table users purge";
		
		st.executeUpdate(sql);
	}
	
	public int getLatestUserId() throws SQLException {
		String sql = "select id from users order by id DESC fetch first 1 rows only";
		
		Connection connection = DriverManager.getConnection(url, username, password);
		Statement st = connection.createStatement();
		
		ResultSet result = st.executeQuery(sql);
		
		int x = -1;
		
		if(result.next()) {
			x = result.getInt(1);
		} else {
			x = 0;
		}
		
		connection.close();
		
		return x;
	}
	
	public int getLatestAccountId() throws SQLException {
		String sql = "select id from account order by id DESC fetch first 1 rows only";
		
		Connection connection = DriverManager.getConnection(url, username, password);
		Statement st = connection.createStatement();
		
		ResultSet result = st.executeQuery(sql);
		
		int x = -1;
		
		if(result.next()) {
			x = result.getInt(1);
		} else {
			x = 0;
		}
		
		connection.close();
		
		return x;
	}
	
	public String getDbUsername() {
		return username;
	}
	public String getDbPassword() {
		return password;
	}
	public String getDbUrl() {
		return url;
	}
	
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(sDatabase.getDbUrl(), sDatabase.getDbUsername(), sDatabase.getDbPassword());
	}
	
	

}
