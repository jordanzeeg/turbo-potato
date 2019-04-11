package com.zero.admin.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public interface UserDao {
	
	public User getUserByUsername(String username) throws FileNotFoundException, ClassNotFoundException, SQLException, IOException;

}
