package com.zero.account;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface AccountDao {
	
	public List<Account> getAccountsByUsername(String username) throws FileNotFoundException, ClassNotFoundException, SQLException, IOException;
	
	public Account getAccountById(int id) throws FileNotFoundException, ClassNotFoundException, SQLException, IOException;

}
