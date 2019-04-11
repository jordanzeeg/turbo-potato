package com.zero;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.zero.account.Account;
import com.zero.account.Account.Ownership;
import com.zero.account.Account.Status;
import com.zero.account.Account.Type;
import com.zero.account.JointAccount;
import com.zero.account.SingleAccount;
import com.zero.admin.Administration;
import com.zero.admin.user.Employee;
import com.zero.admin.user.Manager;
import com.zero.admin.user.User;
import com.zero.admin.user.User.Role;
import com.zero.database.Database;

public class Application {
	private static final Logger LOGGER = LogManager.getLogger(Application.class);
	private static Application sApp = null;
	public static boolean sDebug = false;

	private Application() {
	}

	private static final int OPTION_REGISTER = 1;
	private static final int OPTION_LOGIN = 2;
	private static final int OPTION_QUIT = 3;
	private static boolean stayLoggedIn;
	private static boolean stayInTransactionMenu = true;
	private static boolean online = false;
	private static Scanner sc;
	private static Administration god = Administration.getAdministration(); // TODO change object name
	private static User currentUser;
	private static User otherUser;
	private static Employee emp;
	private static Manager manager;
	private static List<Account> accounts;
	private static List<Account> accountsSubSet;
	private static Account currentAccount;

	public static void main(String[] args) {

		sApp = new Application();

		try {
			Database.getDatabase();
		} catch (SQLException e) {
			LOGGER.error("Failed to initialize database : " + e.toString());
		} catch (IOException e) {
			LOGGER.error("Failed to initialize database : " + e.toString());
		} catch (ClassNotFoundException e) {
			LOGGER.error("Failed to initialize database : " + e.toString());
		}

		online = true;
		LOGGER.info("Application online"); // initialization is fun

		while (online) {

			sc = new Scanner(System.in); // initialize the scanner object

			sayHello(); // prints out initial options

			String command = sc.nextLine(); // reads the initial option choice

			if (StringUtils.isNumeric(command)) { // verifies its an integer

				int option = Integer.valueOf(command); // gets the value of the integer

				if (option > 0 && option < 4) {

					switch (option) {
					case OPTION_REGISTER:
						startRegistration(); // begins registration of user // line 85
						break;
					case OPTION_LOGIN:

						try {
							currentUser = promptLogin(); // begins login process

							while (stayLoggedIn) {
								sayMenu(currentUser);
								menuResponse();
							}
						} catch (SQLException e) {
							LOGGER.error("Failed to prompt login. Something is wrong with the SQL statement: "
									+ e.toString());
						} catch (FileNotFoundException e) {
							LOGGER.error(
									"Failed to prompt login. Could not find a configuration file: " + e.toString());
						} catch (ClassNotFoundException e) {
							LOGGER.error("Failed to prompt login. The library for accessing the database is missing "
									+ e.toString());
						} catch (IOException e) {
							LOGGER.error(
									"Failed to prompt login. Could not read the configuration file. " + e.toString());
						}

						break;
					case OPTION_QUIT:
						sayGoodbye(); // ends application //line 79
						break;
					}
				} else {
					System.out.println("Invalid input.\n");
				}

			} else {
				System.out.println("Invalid input.\n");
			}

		}

	}

	private static void menuResponse() throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {
		boolean validChoice = false;
		stayLoggedIn = true;
		do {
			String command = sc.nextLine(); // reads the initial option choice

			if (StringUtils.isNumeric(command)) { // verifies its an integer

				int option = Integer.valueOf(command); // gets the value of the integer

				if (option > 0 && option < 9) {
					validChoice = true;
					switch (currentUser.getRole()) { // switch based on User type
					case Customer:
						switch (option) {
						case 1: // apply for account
							applyAccount();
							break;

						case 2: // transaction menu
							stayInTransactionMenu = true;
							while(stayInTransactionMenu) {
							sayTransactionMenu();
							transactionMenuResponse();
							// currentUser.displayAccountInformation(); // displays all accounts for user
							}
							break;

						case 3: // view/edit personal info
								// displays menu to edit name/address/phone
							System.out.println(""); // TODO
							boolean personalInformationcomplete = false;
							do {
								updatePersonalInfoMenu(currentUser); // allows customer to choose one of these, and
																		// write into it
								personalInformationcomplete = updatePersonalInfoResponse();
							} while (!personalInformationcomplete);

							// currentUser.store(); //store user into database

							break;

						case 4: // become admin
							adminCode();
							break;

						case 5: // exit
							stayLoggedIn = false;
							break;
						default:
							System.out.println("Please enter a valid input");
							break;
						}
						break;
					case Employee:
						switch (option) {
						case 1: // apply
							applyAccount();
							break;

						case 2: // view/edit account
							while(stayInTransactionMenu) {
							sayTransactionMenu();
							transactionMenuResponse();
							}
							break;

						case 3: // view/edit personal info
							// displays menu to edit name/address/phone
							System.out.println(""); // TODO
							boolean personalInformationcomplete = false;
							do {
								updatePersonalInfoMenu(currentUser); // allows customer to choose one of these, and
																		// write into it
								personalInformationcomplete = updatePersonalInfoResponse();
							} while (!personalInformationcomplete);

							break;

						case 4: // become admin
							adminCode();
							break;
						case 5: // view customer account
							System.out.println("Whose account would you like to view?");
							// TODO get from sysin username here
							String otherUsername = sysinUsername();
							accounts = currentUser.getAccountsByUsername(otherUsername);
							accounts.forEach(x -> System.out.println("Label:" + x.getLabel() + "\n" + "Type: "
									+ x.getType() + "\nAmount: " + x.getAmount() + "\nStatus: " + x.getStatus()));
							// getAccount(otherUser); //retrieve account information for display
							// TODO sysout(otherUser accountInfo)
							break;

						case 6: // view/edit customer personal info
							System.out.println("Whose account would you like to view?");
							String otherUsername2 = sysinUsername();
							otherUser = god.getUserByUsername(otherUsername2);
							otherUser.displayPersonalInformation();
							System.out.println("otherUser.displayPersonalInformation();"); // TODO
							// otherUser.displayPersonalInformation();

							break;
						case 7: // System.out.println("7. Approve/Deny application");
							int accountId = 0;
							accounts = currentUser.getPendingAccounts();
							for(int i = 0; i < accounts.size(); i++) {
								Ownership ownership = accounts.get(i).getOwnership();
								if(ownership == Ownership.Single) {
									SingleAccount singleAccount = ((SingleAccount)accounts.get(i));
									User x = currentUser.getUserById(singleAccount.getOwnerId());
									System.out.println("Owner: "+ x.getUsername()  +" \nLabel: " + singleAccount.getLabel() + "\n" + "Type: "
											+ singleAccount.getType() + "\nAmount: " + singleAccount.getAmount() + "\nStatus: " + singleAccount.getStatus());
								}else {JointAccount jointAccount = ((JointAccount)accounts.get(i));
								User x = currentUser.getUserById(jointAccount.getOwnerId());
								User y = currentUser.getUserById(jointAccount.getSecondOwnerId());
								System.out.println("Owner: " + x.getUsername() + "\nSecond Owner: "+ y.getUsername() +" \nLabel: " + jointAccount.getLabel() + "\n" + "Type: "
										+ jointAccount.getType() + "\nAmount: " + jointAccount.getAmount() + "\nStatus: " + jointAccount.getStatus());
									
								}
							}
							System.out.println("Which account would you like to approve/deny? Press E for exit");
							for (int i = 0; i < accounts.size(); i++) {
								System.out.println(i + ". " + accounts.get(i).getLabel());
							}
							String choiceString = sc.nextLine();
							validChoice = false;
							if (choiceString != "E") {
								
								
							do {
								if (StringUtils.isNumeric(choiceString)) {
									int choice = Integer.valueOf(choiceString);
									accountId = accounts.get(choice).getId();
								
								}else
									System.out.println("Invalid Input.");
							}while (validChoice);
							System.out.println("Would you like to approve or deny this account?");
							System.out.println("1. Approve");
							System.out.println("2. Deny");
							choiceString = sc.nextLine();
							validChoice = false;
							do {
								if (StringUtils.isNumeric(choiceString)) {
									int choice = Integer.valueOf(choiceString);
									if (choice == 1) {
									currentUser.approveAccount(accountId);
									System.out.println("account has been approved");
									validChoice = true;
									}
									else if (choice == 2) {
										currentUser.disapproveAccount(accountId);
										System.out.println("account has been denied");
										validChoice = true;
									}
									else System.out.println("invalid input");
								} else
									System.out.println("Invalid Input.");
							} while (!validChoice);
							} else {break;}
							break;
							
						case 8:
							stayLoggedIn = false;
							break;
						default:
							System.out.println("Please enter a valid input");
							break;
						}
						break;
					case Manager:
						switch (option) {
						case 1: // apply
							applyAccount();

							// applyForAccount(currentUser);//begin application process
							break;

						case 2: // view/edit account
							stayInTransactionMenu = true;
							while(stayInTransactionMenu) {
							sayTransactionMenu();
							transactionMenuResponse();
							}
							break;

						case 3: // view/edit personal info
							// displays menu to edit name/address/phone
							System.out.println(""); // TODO
							boolean personalInformationcomplete = false;
							do {
								updatePersonalInfoMenu(currentUser); // allows customer to choose one of these, and
																		// write into it
								personalInformationcomplete = updatePersonalInfoResponse();
							} while (!personalInformationcomplete);

							break;

						case 4: // become admin
							adminCode();
							break;
						case 5: // view/edit other customer account
							System.out.println("Whose account would you like to view?");
							// TODO get from sysin username here
							String otherUsername = sysinUsername();
							accounts = currentUser.getAccountsByUsername(otherUsername);
							System.out.println("Which account would you like to cancel?");
							for (int i = 0; i < accounts.size(); i++) {
								System.out.println(i + ". " + accounts.get(i).getLabel());
							}	
							String choiceString = sc.nextLine();
							if (StringUtils.isNumeric(choiceString)) {
								int choice = Integer.valueOf(choiceString);
								((Manager)currentUser).cancelAccount(accounts.get(choice).getId());
							}
							
							
							// getAccount(otherUser); //retrieve account information for display
							// TODO sysout(otherUser accountInfo)
							break;
						case 6: // view customer personal info
							System.out.println("Whose account would you like to view/edit?");
							String username2 = sysinUsername();
							otherUser = god.getUserByUsername(username2);
							System.out.println(""); // TODO
							 personalInformationcomplete = false;
							do {
								updatePersonalInfoMenu(otherUser); // allows customer to choose one of these, and
																		// write into it
								personalInformationcomplete = updatePersonalInfoResponse();
							} while (!personalInformationcomplete);
							break;
						case 7: // approve/deny accounts
							int accountId = 0;
							accounts = currentUser.getPendingAccounts();
							for(int i = 0; i < accounts.size(); i++) {
								Ownership ownership = accounts.get(i).getOwnership();
								if(ownership == Ownership.Single) {
									SingleAccount singleAccount = ((SingleAccount)accounts.get(i));
									User x = currentUser.getUserById(singleAccount.getOwnerId());
									System.out.println("Owner: "+ x.getUsername()  +" \nLabel: " + singleAccount.getLabel() + "\n" + "Type: "
											+ singleAccount.getType() + "\nAmount: " + singleAccount.getAmount() + "\nStatus: " + singleAccount.getStatus());
								}else {JointAccount jointAccount = ((JointAccount)accounts.get(i));
								User x = currentUser.getUserById(jointAccount.getOwnerId());
								User y = currentUser.getUserById(jointAccount.getSecondOwnerId());
								System.out.println("Owner: " + x.getUsername() + "\nSecond Owner: "+ y.getUsername() +" \nLabel: " + jointAccount.getLabel() + "\n" + "Type: "
										+ jointAccount.getType() + "\nAmount: " + jointAccount.getAmount() + "\nStatus: " + jointAccount.getStatus());
									
								}
							}
							System.out.println("Which account would you like to approve/deny? Press E for exit");
							for (int i = 0; i < accounts.size(); i++) {
								System.out.println(i + ". " + accounts.get(i).getLabel());
							}
							String choiceString2 = sc.nextLine();
							validChoice = false;
							if (choiceString2 != "E") {
								
								
							do {
								if (StringUtils.isNumeric(choiceString2)) {
									int choice = Integer.valueOf(choiceString2);
									accountId = accounts.get(choice).getId();
								
								}else
									System.out.println("Invalid Input.");
							}while (validChoice);
							System.out.println("Would you like to approve or deny this account?");
							System.out.println("1. Approve");
							System.out.println("2. Deny");
							System.out.println("3. Cancel");
							choiceString2 = sc.nextLine();
							validChoice = false;
							do {
								if (StringUtils.isNumeric(choiceString2)) {
									int choice = Integer.valueOf(choiceString2);
									if (choice == 1) {
									currentUser.approveAccount(accountId);
									System.out.println("account has been approved");
									validChoice = true;
									}
									else if (choice == 2) {
										currentUser.disapproveAccount(accountId);
										System.out.println("account has been denied");
										validChoice = true;
								}else if (choice == 3) {
										((Manager)(currentUser)).cancelAccount(accountId);
										System.out.println("account has been cancelled");
										validChoice = true;
								}
									else System.out.println("invalid input");
								} else
									System.out.println("Invalid Input.");
							} while (!validChoice);
							} else {break;}
							break;
						case 8:
							stayLoggedIn = false;
							break;
						default:
							System.out.println("Please enter a valid input");
							break;
						}

						break;
					case Administrator:
						switch (option) {
						case 1: // apply

							applyAccount();

							// applyForAccount(currentUser);//begin application process
							break;

						case 2: // view/edit account
							stayInTransactionMenu = true;
							while(stayInTransactionMenu) {
							sayTransactionMenu();
							transactionMenuResponse();
							}
							break;

						case 3: // view/edit personal info
							currentUser.displayPersonalInformation();

							updatePersonalInfoMenu(currentUser); // displays menu to edit name/address/phone

							System.out.println("currentUser.updatePersonalInfo();"); // TODO
							// currentUser.updatePersonalInfo(); //allows customer to choose one of these,
							// and write into it
							System.out.println("currentUser.store();"); // TODO
							// currentUser.store(); //store user into database

							break;

						case 4: // upgrade current user
							// TODO function that changes role of currentUser
							promoteMenu();
							promoteResponse(currentUser);
							break;
						case 5: // promote others
							System.out.println("Whose account would you like to upgrade?");
							String username2 = "";
							username2 = sysinUsername();
							User otherUser;
							otherUser = god.getUserByUsername(username2);
							System.out.println("god.getUserFromUsername(username2);"); // TODO
							promoteMenu();
							promoteResponse(otherUser);
							break;
						case 6:
							stayLoggedIn = false;
							break;
						default:
							System.out.println("Please enter a valid input");
							break;
						}
						break;
					}
				}
			}
		} while (!validChoice);
	}

	private static void transactionMenuResponse()
			throws FileNotFoundException, ClassNotFoundException, SQLException, IOException {
		// TODO Auto-generated method stub
		Account account;
		boolean validChoice = false;
		// keeps you in this function
		String command = sc.nextLine(); // reads the initial option choice

		if (StringUtils.isNumeric(command)) { // verifies its an integer

			int option = Integer.valueOf(command); // gets the value of the integer

			if (option > 0 && option < 9) {

				switch (currentUser.getRole()) { // switch based on User type
				case Customer:

					boolean validOption;
					switch (option) {

					case 1: // menu System.out.println("1. View account information");
						accounts = currentUser.getAccountsByUsername(currentUser.getUsername());
						accounts.forEach(x -> System.out.println("Label:" + x.getLabel() + "\n" + "Type: " + x.getType()
								+ "\nAmount: " + x.getAmount() + "\nStatus: " + x.getStatus()));
						break;

					case 2: // System.out.println("2. Withdraw money from account"); //decrement

						accounts = currentUser.getAccountsByUsername(currentUser.getUsername());
						System.out.println("Which account would you like to withdraw from?");
						for (int i = 0; i < accounts.size(); i++) {
							System.out.println(i + ". " + accounts.get(i).getLabel());
						}
						validOption = false;

						String choiceString = sc.nextLine();
						if (StringUtils.isNumeric(choiceString)) {
							int choice = Integer.valueOf(choiceString);
							account = accounts.get(choice);
							// verify account is approved
							if (account.getStatus() == Status.Approved) {
								System.out.println("How much would you like to withdraw?");
								double increment = sysinMoney();
								currentUser.withdrawFunds(account.getId(), increment);
								validOption = true;
							} else {
								System.out.println("This account cannot be accessed at this time");

							}
						} else {
							System.out.println("Please enter a number corresponding to an account");
						}

						break;

					case 3: // System.out.println("3. Deposit money into account") //increment
						accounts = currentUser.getAccountsByUsername(currentUser.getUsername());
						System.out.println("Which account would you like to deposit into?");
						for (int i = 0; i < accounts.size(); i++) {
							System.out.println(i + ". " + accounts.get(i).getLabel());
						}
						validOption = false;

						choiceString = sc.nextLine();
						if (StringUtils.isNumeric(choiceString)) {
							int choice = Integer.valueOf(choiceString);
							account = accounts.get(choice);
							if (account.getStatus() == Status.Approved) {
								System.out.println("How much would you like to deposit?");
								double increment = sysinMoney();
								currentUser.depositFunds(account.getId(), increment);
								validOption = true;
							} else {
								System.out.println("This account cannot be accessed at this time");
							}
						} else {
							System.out.println("Please enter a number corresponding to an account");
						}

						validChoice = true;
						break;

					case 4: // System.out.println("4. Transfer money between accounts"); //both must be
							// owned by customer
						validOption = false;
						Account account2;

						// get user to transfer from
						String username = currentUser.getUsername();

						// get user account to transfer from
						System.out.println("Which account would you like to transfer from?");
						for (int i = 0; i < accounts.size(); i++) {
							System.out.println(i + ". " + accounts.get(i).getLabel());
						}
						choiceString = sc.nextLine();
						if (StringUtils.isNumeric(choiceString)) {
							int choice = Integer.valueOf(choiceString);
							account = accounts.get(choice);
							if (account.getStatus() == Status.Approved) {
								System.out.println("Which account would you like to transfer to?");
								for (int i = 0; i < accounts.size(); i++) {
									System.out.println(i + ". " + accounts.get(i).getLabel());
								}
								String choiceString2 = sc.nextLine();
								if (StringUtils.isNumeric(choiceString2)) {
									int choice2 = Integer.valueOf(choiceString2);
									account2 = accounts.get(choice2);
									if (account2.getStatus() == Status.Approved) {
										System.out.println("How much would you like to transfer?");
										double increment = sysinMoney();
										// execute transfer
										currentUser.transferFunds(account.getId(), account2.getId(), increment);
										validOption = true;

										validChoice = true;

									} else
										System.out.println("this account cannot be accessed at this time");
								} else
									System.out.println("Invalid input");

							} else
								System.out.println("this account cannot be accessed at this time");
						} else
							System.out.println("Invalid input");

						break;

					case 5: // exit // change this
						stayInTransactionMenu = false;
						break;

					}
					break;

				case Employee:
					switch (option) {

					case 1: // System.out.println("1. View account information");
						accounts = currentUser.getAccountsByUsername(currentUser.getUsername());
						accounts.forEach(x -> System.out.println("Label:" + x.getLabel() + "\n" + "Type: " + x.getType()
								+ "\nAmount: " + x.getAmount() + "\nStatus: " + x.getStatus()));
						// nullpointer exception
						break;

					case 2: // System.out.println("2. Withdraw money from account");
						moneyWithdraw();
						validChoice = false;
						break;

					case 3: // System.out.println("3. Deposit money into account");
						moneyDeposit();
						validChoice = false;
						break;

					case 4: // System.out.println("4. Transfer money between two accounts");
						transferBetweenTwo();
						validChoice = false;

						break;
					case 5: // System.out.println("5. Approve/Deny an Apllication"); //TODO check this for
							// bugs
						accounts = manager.getPendingAccounts();
						accounts.forEach(x -> System.out.println("Label:" + x.getLabel() + "\n" + "Type: " + x.getType()
								+ "Amount: " + x.getAmount() + "Status: " + x.getStatus()));
						System.out.println("Which account would you like to approve/deny?");
						for (int i = 0; i < accounts.size(); i++) {
							System.out.println(i + ". " + accounts.get(i).getLabel());
						}
						String choiceString = sc.nextLine();
						validChoice = false;
						do {
							if (StringUtils.isNumeric(choiceString)) {
								int choice = Integer.valueOf(choiceString);
								int accountId = accounts.get(choice).getId();
							} else
								System.out.println("Invalid Input.");
						} while (validChoice);
						System.out.println("Would you like to approve or deny this account?");
						System.out.println("1. Approve");
						System.out.println("2. Deny");
						choiceString = sc.nextLine();
						validChoice = false;
						do {
							if (StringUtils.isNumeric(choiceString)) {
								int choice = Integer.valueOf(choiceString);
								int accountId = accounts.get(choice).getId();
								validChoice = true;
							} else
								System.out.println("Invalid Input.");
						} while (validChoice);

						break;

					case 6: // System.out.println("6. Exit");
						stayInTransactionMenu = false;
						break;

					}
					break;

				case Manager: // TODO make ability to cancel accounts
					switch (option) {

					case 1: // System.out.println("1. View account information");
						accounts = currentUser.getAccountsByUsername(currentUser.getUsername());
						accounts.forEach(x -> System.out.println("Label:" + x.getLabel() + "\n" + "Type: " + x.getType()
								+ "\nAmount: " + x.getAmount() + "\nStatus: " + x.getStatus()));
						break;

					case 2: // System.out.println("2. Withdraw money from account");
						moneyWithdraw();
						break;

					case 3: // System.out.println("3. Deposit money into account");
						moneyDeposit();
						break;

					case 4: // System.out.println("4. Transfer money between two accounts");
						transferBetweenTwo();
						break;
					case 5: // System.out.println("5. Approve/Deny an Apllication");
						String choiceString = "";
						int accountId = 0;
						accounts = manager.getPendingAccounts();
						accounts.forEach(x -> System.out.println("Label:" + x.getLabel() + "\n" + "Type: " + x.getType()
								+ "Amount: " + x.getAmount() + "Status: " + x.getStatus()));
						System.out.println("Which account would you like to withdraw from?");
						for (int i = 0; i < accounts.size(); i++) {
							System.out.println(i + ". " + accounts.get(i).getLabel());
						}
						choiceString = sc.nextLine();
						validChoice = false;
						do {
							if (StringUtils.isNumeric(choiceString)) {
								int choice = Integer.valueOf(choiceString);
								accountId = accounts.get(choice).getId();
								validChoice = true;
							} else
								System.out.println("Invalid Input.");
						} while (!validChoice);
						System.out.println("Would you like to approve or deny this account?");
						System.out.println("1. Approve");
						System.out.println("2. Deny");
						System.out.println("3. Destroy");
						do {
							choiceString = sc.nextLine();
							validChoice = false;

							if (StringUtils.isNumeric(choiceString)) {
								int choice = Integer.valueOf(choiceString);
								switch (choice) {
								case 1:
									manager.approveAccount(accountId);
									validChoice = true;
									break;
								case 2:
									manager.disapproveAccount(accountId);
									validChoice = true;
									break;
								case 3:
									manager.cancelAccount(accountId);
									validChoice = true;
									break;
								default:
									validChoice = false;
									System.out.println("Invalid Input.");

								}

							} else
								System.out.println("Invalid Input.");
						} while (validChoice);
						break;
					case 7: // System.out.println("7. Exit");
						stayInTransactionMenu = false; // change this
						break;
					}
					break;
				case Administrator: // copy customer verbatim
					switch (option) {
					case 1: // menu System.out.println("1. View account information");
						accounts = currentUser.getAccountsByUsername(currentUser.getUsername());
						accounts.forEach(x -> System.out.println("Label:" + x.getLabel() + "\n" + "Type: " + x.getType()
								+ "Amount: " + x.getAmount() + "Status: " + x.getStatus() + "admin"));
						break;

					case 2: // System.out.println("2. Withdraw money from account"); //decrement
						moneyWithdraw();
						break;

					case 3: // System.out.println("3. Deposit money into account") //increment
						moneyDeposit();
						break;

					case 4: // System.out.println("4. Transfer money between accounts"); //both must be
							// owned by customer
						customerTransfer();
						break;

					case 5: // exit // change this
						stayInTransactionMenu = false;
						break;

					}
					break;
				}
			}

		}

	}

	private static void customerTransfer()
			throws FileNotFoundException, ClassNotFoundException, SQLException, IOException {
		// TODO Auto-generated method stub
		boolean validOption = false;
		int accountId, accountId2;
		accountId = accountId2 = 0;
		do {
			// get user to transfer from
			String username = currentUser.getUsername();

			// get user account to transfer from
			System.out.println("Which account would you like to transfer from?");
			for (int i = 0; i < accounts.size(); i++) {
				System.out.println(i + ". " + accounts.get(i).getLabel());
			}
			String choiceString = sc.nextLine();
			if (StringUtils.isNumeric(choiceString)) {
				int choice = Integer.valueOf(choiceString);
				accountId = accounts.get(choice).getId();
			} else
				System.out.println("Invalid input");

			// get account to transfer to
			System.out.println("Which account would you like to transfer to?");
			for (int i = 0; i < accounts.size(); i++) {
				System.out.println(i + ". " + accounts.get(i).getLabel());
			}
			String choiceString2 = sc.nextLine();
			if (StringUtils.isNumeric(choiceString2)) {
				int choice2 = Integer.valueOf(choiceString2);
				accountId2 = accounts.get(choice2).getId();
			} else
				System.out.println("Invalid input");

			// get amount
			System.out.println("How much would you like to transfer?");
			double increment = sysinMoney();
			// execute transfer
			currentUser.transferFunds(accountId, accountId2, increment);
			System.out.println("Transfer successful!");
			validOption = true;

		} while (!validOption);
	}

	private static void moneyDeposit() {
		// TODO Auto-generated method stub
		Account account;
		boolean validOption = false;
		do {
			System.out.println("Which username is this deposit for?");
			String username = sysinUsername();
			try {
				accounts = currentUser.getAccountsByUsername(username);
			} catch (FileNotFoundException e) {
				LOGGER.error("file not found while searching for database. " + e.toString());
			} catch (ClassNotFoundException e) {
				LOGGER.error("Database not found. Unable to read from Database" + e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());
				e.printStackTrace();
			} catch (SQLException e) {
				LOGGER.error("Data tables not communicating with java program. " + e.toString());
				e.printStackTrace();
			}
			System.out.println("Which account would you like to deposit to?");
			for (int i = 0; i < accounts.size(); i++) {
				System.out.println(i + ". " + accounts.get(i).getLabel());
			}
			String choiceString = sc.nextLine();
			if (StringUtils.isNumeric(choiceString)) {
				int choice = Integer.valueOf(choiceString);
				account = accounts.get(choice);
				if (account.getStatus() == Status.Approved) {
					System.out.println("How much would you like to deposit?");
					double increment = sysinMoney();
					try {
						currentUser.depositFunds(account.getId(), increment);
					} catch (FileNotFoundException e) {
						LOGGER.error("file not found while searching for database. " + e.toString());
					} catch (ClassNotFoundException e) {
						LOGGER.error("Database not found. Unable to read from Database" + e.toString());

					} catch (IOException e) {
						LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

					} catch (SQLException e) {
						LOGGER.error("Data tables not communicating with java program. " + e.toString());

					}
					validOption = true;
				} else {
					System.out.println("this account is not available at this time");
				}
			}
		} while (!validOption);
	}

	private static void moneyWithdraw()
			throws FileNotFoundException, ClassNotFoundException, SQLException, IOException {
		// TODO Auto-generated method stub
		Account account;
		boolean validOption = false;
		do {
			System.out.println("Which username is this withdrawl from?");
			String username = sysinUsername();
			accounts = currentUser.getAccountsByUsername(username);
			System.out.println("Which account would you like to withdraw from?");
			for (int i = 0; i < accounts.size(); i++) {
				System.out.println(i + ". " + accounts.get(i).getLabel());
			}
			String choiceString = sc.nextLine();
			if (StringUtils.isNumeric(choiceString)) {
				int choice = Integer.valueOf(choiceString);
				account = accounts.get(choice);
				if (account.getStatus() == Status.Approved) {
					System.out.println("How much would you like to withdraw?");
					double decrement = sysinMoney();
					currentUser.withdrawFunds(account.getId(), decrement);
					validOption = true;
				} else {
					System.out.println("this account is not available at this time");
				}
			}
		} while (!validOption);
	}

	private static void transferBetweenTwo() {

		// TODO Auto-generated method stub
		boolean validOption;
		validOption = false;
		boolean validChoice = false;
		Account account, account2;
		do {
			// get user to transfer from
			System.out.println("Which username is this transfer from?");
			String username = sysinUsername();
			try {
				accounts = currentUser.getAccountsByUsername(username);
			} catch (FileNotFoundException e) {
				LOGGER.error("file not found while searching for database. " + e.toString());
			} catch (ClassNotFoundException e) {
				LOGGER.error("Database not found. Unable to read from Database" + e.toString());

			} catch (IOException e) {
				LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

			} catch (SQLException e) {
				LOGGER.error("Data tables not communicating with java program. " + e.toString());

			}

			// get user account to transfer from
			System.out.println("Which account would you like to transfer from?");
			for (int i = 0; i < accounts.size(); i++) {
				System.out.println(i + ". " + accounts.get(i).getLabel());
			}
			do {
				String choiceString = sc.nextLine();
				if (StringUtils.isNumeric(choiceString)) {
					int choice = Integer.valueOf(choiceString);
					account = accounts.get(choice);
					if (account.getStatus() == Status.Approved) {
						validChoice = true;
						System.out.println("Which username is this transfer to?");
						String otherUsername = sysinUsername();
						try {
							accounts = currentUser.getAccountsByUsername(otherUsername);
						} catch (FileNotFoundException e) {
							LOGGER.error("file not found while searching for database. " + e.toString());
						} catch (ClassNotFoundException e) {
							LOGGER.error("Database not found. Unable to read from Database" + e.toString());

						} catch (IOException e) {
							LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

						} catch (SQLException e) {
							LOGGER.error("Data tables not communicating with java program. " + e.toString());

						}

						// get account to transfer to
						System.out.println("Which account would you like to transfer to?");
						for (int i = 0; i < accounts.size(); i++) {
							System.out.println(i + ". " + accounts.get(i).getLabel());
						}
						String choiceString2 = sc.nextLine();
						do {
							if (StringUtils.isNumeric(choiceString2)) {
								int choice2 = Integer.valueOf(choiceString2);
								account2 = accounts.get(choice2);
								if (account2.getStatus() == Status.Approved) {
									validChoice = false;

									// get amount
									System.out.println("How much would you like to transfer?");
									double increment = sysinMoney();
									// execute transfer
									try {
										currentUser.transferFunds(account.getId(), account2.getId(), increment);
									} catch (FileNotFoundException e) {
										LOGGER.error("file not found while searching for database. " + e.toString());
									} catch (ClassNotFoundException e) {
										LOGGER.error("Database not found. Unable to read from Database" + e.toString());

									} catch (IOException e) {
										LOGGER.error("Cannot read property file to begin connecting to database "
												+ e.toString());

									} catch (SQLException e) {
										LOGGER.error(
												"Data tables not communicating with java program. " + e.toString());

									}
									System.out.println("Transaction Successful! " + account.getLabel() + " now has "
											+ account.getAmount() + " in the Account \n" + account2.getLabel()
											+ " now has " + account2.getAmount() + " in the Account.");
									validChoice = true;

								} else {
									System.out.println("This account is not accessible at this time");
								}
							} else
								System.out.println("Invalid input");

						} while (!validChoice);
					}
				}
			} while (!validChoice);
		} while (!validChoice);
	}

	private static void sayTransactionMenu() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		System.out.println("What transaction can we help you with today?");
		switch (currentUser.getRole()) {
		case Customer: {
			System.out.println("1. View account information");
			System.out.println("2. Withdraw money from account");
			System.out.println("3. Deposit money into account");
			System.out.println("4. Transfer money between accounts");
			System.out.println("5. Exit");
			break;
		}
		case Employee: {

			System.out.println("1. View account information");
			System.out.println("2. Withdraw money from account");
			System.out.println("3. Deposit money into account");
			System.out.println("4. Transfer money between two accounts");
			System.out.println("6. Exit");
			break;
		}
		case Manager: {
			System.out.println("1. View account information");
			System.out.println("2. Withdraw money from account");
			System.out.println("3. Deposit money into account");
			System.out.println("4. Transfer money between two accounts");
			System.out.println("5. Exit");
			break;
		}
		case Administrator: {
			System.out.println("1. View account information");
			System.out.println("2. Withdraw money from account");
			System.out.println("3. Deposit money into account");
			System.out.println("4. Transfer money between accounts");
			System.out.println("5. Exit");
			break;
		}
		}
	}

	private static void applyAccount() {
		// TODO Auto-generated method stub

		boolean validChoice = false;
		double initialAmount = 0;
		boolean validOption = false;
		do {
			System.out.println("If this is for a joint account press 1, otherwise press 2");
			// TODO copy option switch

			Type accountType = Type.Checking;

			String command = sc.nextLine(); // reads the initial option choice
			if (StringUtils.isNumeric(command)) { // verifies its an integer

				int option = Integer.valueOf(command); // gets the value of the integer
				if (option == 1) {
					String otherUsername = "";
					do {
						System.out.println("What is the username of the person applying with you?");
						otherUsername = sysinUsername();
					} while (otherUsername == currentUser.getUsername());

					try {
						otherUser = god.getUserByUsername(otherUsername);
					} catch (FileNotFoundException e) {
						LOGGER.error("file not found while searching for database. " + e.toString());
					} catch (ClassNotFoundException e) {
						LOGGER.error("Database not found. Unable to read from Database" + e.toString());

					} catch (IOException e) {
						LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

					} catch (SQLException e) {
						LOGGER.error("Data tables not communicating with java program. " + e.toString());

					}
					System.out.println("What would you like the name of the account to be?");

					String accountName = sysinAccountName();
					System.out.println("What kind of account would you like to apply for?");
					accountTypeMenu();
					accountType = accountTypeResponse();
					System.out.println("How much money would you like to start the account with?");
					initialAmount = sysinMoney();
					String input;

					do {
						System.out.println("Sharing Account with: " + otherUsername);
						System.out.println("Name of Account: " + accountName);
						System.out.println("Type of Account: " + accountType);
						System.out.println("Initial amount: " + initialAmount);
						System.out.println("\n");
						System.out.println("Is this information correct? Please type (Y)es/(N)o.");

						input = sc.nextLine();

						if (input.length() == 1 && (input.equals("Y")) || (input.equals("y")) || (input.equals("N"))
								|| (input.equals("n")) || (input.equals("Yes")) || (input.equals("No"))
								|| (input.equals("yes")) || (input.equals("no"))) {
							validOption = true;
						} else if (!(input.equals("Y")) || (input.equals("y")) || (input.equals("N"))
								|| (input.equals("n"))) {
							System.out.println("Invalid input. Please type (Y)es/(N)o.");
						} else {
							System.out.println("Invalid input.\n");
						}

					} while (!validOption);
					validOption = false;
					if (input.equals("Y") || input.equals("y") || input.equals("Yes") || input.equals("yes")) {
						// call constructor/factory method that saves data into database and text file
						try {
							currentUser.submitApplicationForJointAccount(accountName, otherUser.getId(), initialAmount,
									accountType);
						} catch (FileNotFoundException e) {
							LOGGER.error("file not found while searching for database. " + e.toString());
						} catch (ClassNotFoundException e) {
							LOGGER.error("Database not found. Unable to read from Database" + e.toString());

						} catch (IOException e) {
							LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

						} catch (SQLException e) {
							LOGGER.error("Data tables not communicating with java program. " + e.toString());

						} // apply for account using both currentUser
						System.out.println("Congratulations! You have successfully applied for an account.");
						System.out
								.println("a bank manager will look over your application and get back to you shortly");
						validChoice = true;
					} else {
						applyAccount(); // if information is incorrect, we're back to the beginning of creation
						validChoice = true;
					}
				} else if (option == 2) {
					// TODO applyForAccount(currentUser) //may or may not be the same function as
					// above
					System.out.println("What would you like the name of the account to be?");
					String accountName = sysinAccountName();
					System.out.println("What kind of account would you like to apply for?");
					accountTypeMenu();
					accountType = accountTypeResponse();
					System.out.println("How much money would you like to start the account with?");
					initialAmount = sysinMoney();
					String input;

					do {
						System.out.println("Name of Account: " + accountName);
						System.out.println("Type of Account: " + accountType);
						System.out.println("Initial amount: " + initialAmount);
						System.out.println("\n");
						System.out.println("Is this information correct? Please type (Y)es/(N)o.");

						input = sc.nextLine();

						if (input.length() == 1 && (input.equals("Y")) || (input.equals("y")) || (input.equals("N"))
								|| (input.equals("n")) || (input.equals("Yes")) || (input.equals("No"))
								|| (input.equals("yes")) || (input.equals("no"))) {
							validOption = true;
						} else if (!(input.equals("Y")) || (input.equals("y")) || (input.equals("N"))
								|| (input.equals("n"))) {
							System.out.println("Invalid input. Please type (Y)es/(N)o.");
						} else {
							System.out.println("Invalid input.\n");
						}

					} while (!validOption);
					validChoice = false;
					if (input.equals("Y") || input.equals("y") || input.equals("Yes") || input.equals("yes")) {
						// call constructor/factory method that saves data into database and text file
						try {
							currentUser.submitApplicationForSingleAccount(accountName, initialAmount, accountType);
						} catch (FileNotFoundException e) {
							LOGGER.error("file not found while searching for database. " + e.toString());
						} catch (ClassNotFoundException e) {
							LOGGER.error("Database not found. Unable to read from Database" + e.toString());

						} catch (IOException e) {
							LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

						} catch (SQLException e) {
							LOGGER.error("Data tables not communicating with java program. " + e.toString());

						}
						// for
						// account
						// using
						// both
						// currentUser
						System.out.println("Congratulations! You have successfully applied for an account.");
						System.out
								.println("a bank manager will look over your application and get back to you shortly");
						validChoice = true;
					} else {
						applyAccount(); // if information is incorrect, we're back to the beginning of creation
						validChoice = true;
					}
				} else {
					System.out.println("Invalid input");
				}
			}

		} while (!validChoice);
	}

	private static Type accountTypeResponse() {
		// TODO Auto-generated method stub
		Type accountType = Type.Checking;

		boolean validChoice = false;
		do {
			String command = sc.nextLine(); // reads the initial option choice
			if (StringUtils.isNumeric(command)) { // verifies its an integer

				int option = Integer.valueOf(command); // gets the value of the integer
				if (option > 0 && option < 7) {

					switch (option) {
					case 1: // change to checking

						accountType = Type.Checking;
						validChoice = true;
						break;

					case 2: // change to Saving
						accountType = Type.Saving;
						validChoice = true;
						break;

					case 3: // change to MoneyMarket
						accountType = Type.MoneyMarket;
						validChoice = true;
						break;

					case 4: // IRA
						accountType = Type.IRA;
						validChoice = true;
						break;
					case 5: // Brokerage
						accountType = Type.Brokerage;
						validChoice = true;
						break;
					case 6: // CD
						accountType = Type.CD;
						validChoice = true;
						break;

					}
				} else {
					System.out.println("Invalid type of account, please type a number betweeen 1 and 6");
				}
			}

			else {
				System.out.println("Invalid input");
				validChoice = false;
			}
		} while (!validChoice);
		return accountType;
	}

	private static boolean updatePersonalInfoResponse() {
		// TODO Auto-generated method stub
		boolean returnboolean = false;
		boolean validChoice = false;
		String username2, password2, homeAddress2, city2, state2, zip2, phone2, firstName2, lastName2;
		username2 = password2 = homeAddress2 = city2 = state2 = zip2 = phone2 = firstName2 = lastName2 = "";
		do {
			String command = sc.nextLine(); // reads the initial option choice

			if (StringUtils.isNumeric(command)) { // verifies its an integer

				int option = Integer.valueOf(command); // gets the value of the integer

				if (option > 0 && option < 7) {
					validChoice = true;

					switch (option) {
					case 1: // change username
						System.out.println("Please type in the username you would like to use"); // TODO
						username2 = sysinUsername();
						try {
							currentUser.changeUsername(username2);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						break;

					case 2: // change password
						System.out.println("Please type in the password you would like to use"); // TODO
						password2 = sysinPassword();
						try {
							currentUser.changePassword(password2);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}

						break;

					case 3: // change name
						System.out.println("Please type in the first name you would like to use"); // TODO
						firstName2 = sysinFirstName();
						try {
							currentUser.changeFirstName(firstName2);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						System.out.println("Please type in the last name you would like to use"); // TODO
						lastName2 = sysinLastName();
						try {
							currentUser.changeLastName(lastName2);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}

						break;

					case 4: // change address
						System.out.println("Please type in the street address you would like to use"); // TODO
						homeAddress2 = sysinAddress();
						try {
							currentUser.changeAddress(homeAddress2);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						System.out.println("Please type in the city you would like to use");
						city2 = sysinCity();
						try {
							currentUser.changeCity(city2);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						System.out.println("Please type in the state you would like to use");
						state2 = sysinState();
						try {
							currentUser.changeState(state2);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						System.out.println("Please type in the zip you would like to use");
						zip2 = sysinzip();
						try {
							currentUser.changeZip(zip2);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						break;
					case 5: // phone number
						String phone = "";
						boolean validOption = false;
						do {

							System.out.println("Please enter your phone number");

							String input = sc.nextLine();

							if (input.length() > 7 && input.length() < 20 && StringUtils.isNumeric(input)) {
								phone = input;
								validOption = true;
							} else if (!StringUtils.isNumeric(input)) {
								System.out.println("Only input numbers please.");
							} else if (input.length() < 4) {
								System.out.println("Must be more than 3 characters");
							} else if (input.length() > 19) {
								System.out.println("Must be less than 20 characters");
							} else {
								System.out.println("Invalid input.\n");
							}
							try {
								currentUser.changePhone(phone);
							} catch (SQLException e) {
								LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
							}
						} while (!validOption);
						break;
					case 6: // exit
						returnboolean = true;
						break; // TODO change this
					}
				}
			}
		} while (!validChoice);
		return returnboolean;
	}

	public static void updatePersonalInfoMenu(User user) {
		// TODO Auto-generated method stub
		System.out.println("What field would you like to update?");
		System.out.println("1. Username: currently: " + user.getUsername());
		System.out.println("2. Password: ");
		System.out.println("3. Name: currently: " + user.getFirstName() + " " + user.getLastName());
		System.out.println("4. Home Address: currently: " + user.getAddress() + " " + user.getCity() + " "
				+ user.getState() + " " + user.getZip());
		System.out.println("5. Phone Number: " + user.getPhone());
		System.out.println("6. All information is accurate");

	}

	private static void sayMenu(User user) {
		// TODO Auto-generated method stub
		System.out.println("What would you like to do today?");
		switch (user.getRole()) {
		case Customer: {
			System.out.println("1. Apply for an account");
			System.out.println("2. perform a bank transaction");
			System.out.println("3. View/edit personal information");
			System.out.println("4. Enter Administration mode");
			System.out.println("5. Exit");
			break;
		}
		case Employee: {

			System.out.println("1. Apply for an account");
			System.out.println("2. perform a bank transaction");
			System.out.println("3. View/edit my personal information");
			System.out.println("4. Enter Administration mode");
			System.out.println("5. View customer's banking account");
			System.out.println("6. View customer's personal information");
			System.out.println("7. Approve/Deny application");
			System.out.println("8. Exit");
			break;
		}
		case Manager: {
			System.out.println("1. Apply for an account");
			System.out.println("2. perform a bank transaction");
			System.out.println("3. View/edit my personal information");
			System.out.println("4. Enter Administration mode");
			System.out.println("5. View customer's banking account");
			System.out.println("6. View/edit personal information");
			System.out.println("7. Approve/Deny application");
			System.out.println("8. Exit");
			break;
		}
		case Administrator: {
			System.out.println("1. Apply for an account");
			System.out.println("2. perform a bank transaction");
			System.out.println("3. View/edit personal information");
			System.out.println("4. Upgrade current user");
			System.out.println("5. Upgrade other user");
			System.out.println("6. Exit");
			break;
		}
		}

	}

	private static final void promoteMenu() {
		System.out.println("What Type of User are you upgrading into?");
		System.out.println("Please select an option:");
		System.out.println("1.Customer");
		System.out.println("2.Employee");
		System.out.println("3.Manager");
	}

	private static void accountTypeMenu() {
		// TODO Auto-generated method stub
		System.out.println("What Type of Account are you applying for?");
		System.out.println("1.Checking");
		System.out.println("2.Saving");
		System.out.println("3.Money Market");
		System.out.println("4.IRA");
		System.out.println("5.Brokerage");
		System.out.println("6.CD");
	}

	// referenced on line 39
	private static final void sayHello() {
		System.out.println("Welcome to Revature Banking.");
		System.out.println("Please select an option:");
		System.out.println("1.Register.");
		System.out.println("2.Login.");
		System.out.println("3.Quit.");
	}

	private static final void sayGoodbye() { // do we need to backup/commit here?
		System.out.println("Goodbye");
		sc.close();
		online = false;
	}

	private static final String sysinUsername() { // takes in username from sysin
		boolean validOption = false;
		String username = "";
		do {

			String input = sc.nextLine(); // reads in the next line of code

			if (StringUtils.isAlphanumeric(input) && input.length() > 3 // makes sure it's longer than 3 characters //
																		// TODO maxlength?
					&& input.length() < 30 // 30 from schema
					&& !firstCharacterIsNumber(input)) { // cannot start with a number
				username = input; // needs to check for existing username
				validOption = true;

			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 29) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {

				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n"); // the other wacky input catches
			}
		} while (!validOption);
		return username;
	}

	private static String sysinPassword() { // takes in password from sysin
		boolean validPassword = false;
		boolean validOption = false;
		String password, confirmPassword;
		password = confirmPassword = "";
		do {

			validOption = false;

			do {

				System.out.println("Please enter your password");

				String input = sc.nextLine(); // reads in attempt at a password

				if (StringUtils.isAlphanumeric(input) && input.length() > 2 && input.length() < 30 // checks length //
																									// TODO maxlength?
						&& !firstCharacterIsNumber(input)) { // verifies first character
					password = input; // if all checks are valid, set password
					validOption = true;
				} else if (input.length() < 3) {
					System.out.println("Must be more than 2 characters");
				} else if (input.length() > 29) {
					System.out.println("Must be less than 30 characters");
				} else if (firstCharacterIsNumber(input)) {
					System.out.println("First character can not be a number.");
				} else {
					System.out.println("Invalid input.\n");
				}

			} while (!validOption);

			validOption = false;

			do {

				System.out.println("Please re-enter the password your entered to confirm.");

				String input = sc.nextLine(); // read in second attempt at password

				if (StringUtils.isAlphanumeric(input) && input.length() > 2 && input.length() < 30
						&& !firstCharacterIsNumber(input)) {
					confirmPassword = input; // verify attempts match
					validOption = true;
				} else if (input.length() < 3) {
					System.out.println("Must be more than 2 characters");
				} else if (input.length() > 29) {
					System.out.println("Must be less than 30 characters");
				} else if (firstCharacterIsNumber(input)) {
					System.out.println("First character can not be a number.");
				} else {
					System.out.println("Invalid input.\n");
				}

			} while (!validOption);

			if (!password.equals(confirmPassword)) {
				System.out.println("Error! Passwords do not match.");
			} else {
				validPassword = true;
			}

		} while (!validPassword);
		return password;
	}

	private static String sysinFirstName() {
		// TODO Auto-generated method stub
		boolean validOption = false;
		String firstName = "";
		do {

			System.out.println("Please enter your first name");

			String input = sc.nextLine(); // reads in the next line of code

			if (StringUtils.isAlphanumeric(input) && input.length() > 3 // makes sure it's longer than 3 characters //
																		// TODO maxlength?
					&& input.length() < 30 // 30 from schema
					&& !firstCharacterIsNumber(input)) { // cannot start with a number
				firstName = input; // needs to check for existing username
				validOption = true;

			} else if (input.length() < 3) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 29) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {

				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n"); // the other wacky input catches
			}
		} while (!validOption);
		return firstName;

	}

	private static String sysinLastName() {
		// TODO Auto-generated method stub
		boolean validOption = false;
		String lastName = "";
		do {

			String input = sc.nextLine(); // reads in the next line of code

			if (StringUtils.isAlphanumeric(input) && input.length() > 3 // makes sure it's longer than 3 characters //
																		// TODO maxlength?
					&& input.length() < 30 // 30 from schema
					&& !firstCharacterIsNumber(input)) { // cannot start with a number
				lastName = input; // needs to check for existing username
				validOption = true;

			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 29) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {

				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n"); // the other wacky input catches
			}
		} while (!validOption);
		return lastName;

	}

	private static String sysinAccountName() {
		// TODO Auto-generated method stub

		boolean validOption = false;
		String accountName = "";
		do {

			String input = sc.nextLine(); // reads in the next line of code

			if (StringUtils.isAlphanumeric(input) && input.length() > 3 // makes sure it's longer than 3 characters //
																		// TODO maxlength?
					&& input.length() < 30 // 30 from schema
					&& !firstCharacterIsNumber(input)) { // cannot start with a number
				accountName = input; // needs to check for existing username
				validOption = true;

			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 19) {
				System.out.println("Must be less than 20 characters");
			} else if (firstCharacterIsNumber(input)) {

				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n"); // the other wacky input catches
			}
		} while (!validOption);
		return accountName;

	}

	private static String sysinAddress() {
		// TODO Auto-generated method stub
		boolean validOption = false;
		String homeAddress = "";
		do {

			String input = sc.nextLine(); // reads in the next line of code

			if (StringUtils.isAlphanumericSpace(input) && input.length() > 3 // makes sure it's longer than 3 characters
																				// // TODO maxlength?
					&& input.length() < 50 // 50 from schema
			) { // cannot start with a number
				homeAddress = input; // needs to check for existing username
				validOption = true;

			} else if (!StringUtils.isAlphanumericSpace(input)) {
				System.out.println("Only input numbers and letters");
			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 49) {
				System.out.println("Must be less than 50 characters");

			} else {
				System.out.println("Invalid input.\n"); // the other wacky input catches
			}
		} while (!validOption);
		return homeAddress;
	}

	private static String sysinCity() {
		boolean validOption = false;
		String city = "";
		do {

			String input = sc.nextLine(); // reads in the next line of code

			if (StringUtils.isAlphanumericSpace(input) && input.length() > 3 // makes sure it's longer than 3 characters
																				// // TODO maxlength?
					&& input.length() < 20 // 50 from schema
					&& !firstCharacterIsNumber(input)) { // cannot start with a number
				city = input; // needs to check for existing username
				validOption = true;

			} else if (!StringUtils.isAlphanumericSpace(input)) {
				System.out.println("Only input numbers and letters");
			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 19) {
				System.out.println("Must be less than 20 characters");
			} else if (firstCharacterIsNumber(input)) {

				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n"); // the other wacky input catches
			}
		} while (!validOption);
		return city;
	}

	private static String sysinState() {
		boolean validOption = false;
		String state = "";
		do {

			String input = sc.nextLine(); // reads in the next line of code

			if (StringUtils.isAlphanumericSpace(input) && input.length() >= 2 // makes sure it's longer than 3
																				// characters //
																				// TODO maxlength?
					&& input.length() < 20 // 50 from schema
					&& !firstCharacterIsNumber(input)) { // cannot start with a number
				state = input; // needs to check for existing username
				validOption = true;

			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 19) {
				System.out.println("Must be less than 20 characters");
			} else if (firstCharacterIsNumber(input)) {

				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n"); // the other wacky input catches
			}
		} while (!validOption);
		return state;
	}

	private static String sysinzip() {
		// TODO Auto-generated method stub
		boolean validOption = false;
		String zip = "";

		do {

			String input = sc.nextLine();

			if (input.length() > 3 && input.length() < 10 && StringUtils.isNumeric(input)) {
				zip = input;
				validOption = true;
			} else if (!StringUtils.isNumeric(input)) {
				System.out.println("Only input numbers please.");
			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 9) {
				System.out.println("Must be less than 10 characters");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		return zip;
	}

	private static double sysinMoney() {
		// TODO Auto-generated method stub
		boolean validOption = false;
		String moneyString = "";
		double moneyDouble = 0;

		do {
			String input = sc.nextLine();

			if (input.length() > 2 && input.length() < 15) {
				moneyString = input;
				try {
					moneyDouble = Double.parseDouble(moneyString);
					moneyDouble = round(moneyDouble);
					validOption = true;

				} catch (NumberFormatException e) {
					System.out.println("Please enter a proper amount");
					validOption = false;
				}
			} else if (!StringUtils.isNumeric(input)) {
				System.out.println("Only input numbers please.");
			} else if (input.length() < 2) {
				System.out.println("Must be more than 2 characters");
			} else if (input.length() > 15) {
				System.out.println("Must be less than 15 characters");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);
		return moneyDouble;
	}

	private static double round(double moneyDouble) {
		BigDecimal bd = new BigDecimal(moneyDouble);
		bd = bd.setScale(2, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}

	private static void promoteResponse(User user) {
		// TODO Auto-generated method stub
		boolean validChoice = false;
		do {
			String command = sc.nextLine(); // reads the initial option choice

			if (StringUtils.isNumeric(command)) { // verifies its an integer

				int option = Integer.valueOf(command); // gets the value of the integer

				if (option > 0 && option < 4) {
					validChoice = true;

					switch (option) {
					case 1: // change to customer

						System.out.println("Successfully changed to Customer"); // TODO//TODO
						try {
							Administration.changeRole(user.getId(), Role.Customer);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						System.out.println("Restart the program to allow changes to take effect");
						break;

					case 2: // change to employee
						System.out.println("Successfully changed to Employee"); // TODO //TODO
						try {
							Administration.changeRole(user.getId(), Role.Employee);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						System.out.println("Restart the program to allow changes to take effect");
						break;

					case 3: // change to manager

						try {
							Administration.changeRole(user.getId(), Role.Manager);
						} catch (SQLException e) {
							LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
						}
						System.out.println("Successfully changed to Manager"); // TODO
						System.out.println("Restart the program to allow changes to take effect");
						break;

					case 4: // exit
						stayLoggedIn = false;
						break;

					}
				}
			}
		} while (!validChoice);

	}

	private static void adminCode() {
		// takes in a code to verify that user can in fact become an admin, and changes
		// their role if they can.
		int passCode = 8520;

		boolean validOption = false;

		do {

			System.out.println("Please enter the code to gain access to Admin functions");

			String input = sc.nextLine();
			int testCode = 0;
			if (input.length() == 4 && StringUtils.isNumeric(input)) {
				testCode = Integer.parseInt(input);
				if (testCode == passCode) {
					System.out.println("You are now an Admin");
					try {
						Administration.changeRole(currentUser.getId(), Role.Administrator);
					} catch (SQLException e) {
						LOGGER.error("Cannot communicate properly with SQL database. " + e.toString());
					}
					System.out.println("Log in to the program to allow changes to take effect");
					// prompt for user to restart program for admin effect
				}
				validOption = true;
			} else if (!StringUtils.isNumeric(input)) {
				System.out.println("Only input numbers please.");
			} else if (input.length() != 4) {
				System.out.println("Must be more than 3 characters");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);
	}

	private static final void startRegistration() { // registers the user

		boolean validOption = false;
		String username, password, confirmPassword, email, firstName, lastName, homeAddress, city, state, zipcode,
				country, phone;
		username = password = confirmPassword = email = firstName = lastName = homeAddress = city = state = zipcode = country = phone = "";
		// Role role, String address, String city, String state, String zip, String
		// phone;s

		do {

			System.out.println("Please enter your username");

			String input = sc.nextLine(); // reads in the next line of code
			try {
				if (StringUtils.isAlphanumeric(input) && input.length() > 3 // makes sure it's longer than 3 characters
																			// //
																			// TODO maxlength?
						&& input.length() < 30 // 30 from schema
						&& !firstCharacterIsNumber(input) && !god.checkUserName(input)) { // cannot start with a number
					username = input; // needs to check for existing username

					validOption = true;
				} else if (input.length() < 4) {
					System.out.println("Must be more than 3 characters");
				} else if (input.length() > 29) {
					System.out.println("Must be less than 30 characters");
				} else if (firstCharacterIsNumber(input)) {

					System.out.println("First character can not be a number.");
				} else
					try {
						if (god.checkUserName(input)) {
							System.out.println("Username is already taken. Choose a new Username");
						} else {
							System.out.println("Invalid input.\n"); // the other wacky input catches
						}
					} catch (FileNotFoundException e) {
						LOGGER.error("file not found while searching for database. " + e.toString());
					} catch (ClassNotFoundException e) {
						LOGGER.error("Database not found. Unable to read from Database" + e.toString());

					} catch (IOException e) {
						LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

					} catch (SQLException e) {
						LOGGER.error("Data tables not communicating with java program. " + e.toString());

					}
			} catch (FileNotFoundException e) {
				LOGGER.error("file not found while searching for database. " + e.toString());
			} catch (ClassNotFoundException e) {
				LOGGER.error("Database not found. Unable to read from Database" + e.toString());

			} catch (IOException e) {
				LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

			} catch (SQLException e) {
				LOGGER.error("Data tables not communicating with java program. " + e.toString());

			}

		} while (!validOption);

		boolean validPassword = false;

		do {

			validOption = false;

			do {

				System.out.println("Please enter your password");

				String input = sc.nextLine(); // reads in attempt at a password

				if (StringUtils.isAlphanumeric(input) && input.length() > 2 && input.length() < 30 // checks length //
																									// TODO maxlength?
						&& !firstCharacterIsNumber(input)) { // verifies first character
					password = input; // if all checks are valid, set password
					validOption = true;
				} else if (input.length() < 3) {
					System.out.println("Must be more than 2 characters");
				} else if (input.length() > 29) {
					System.out.println("Must be less than 30 characters");
				} else if (firstCharacterIsNumber(input)) {
					System.out.println("First character can not be a number.");
				} else {
					System.out.println("Invalid input.\n");
				}

			} while (!validOption);

			validOption = false;

			do {

				System.out.println("Please re-enter the password your entered to confirm.");

				String input = sc.nextLine(); // read in second attempt at password

				if (StringUtils.isAlphanumeric(input) && input.length() > 2 && input.length() < 30
						&& !firstCharacterIsNumber(input)) {
					confirmPassword = input; // verify attempts match
					validOption = true;
				} else if (input.length() < 3) {
					System.out.println("Must be more than 2 characters");
				} else if (input.length() > 29) {
					System.out.println("Must be less than 30 characters");
				} else if (firstCharacterIsNumber(input)) {
					System.out.println("First character can not be a number.");
				} else {
					System.out.println("Invalid input.\n");
				}

			} while (!validOption);

			if (!password.equals(confirmPassword)) {
				System.out.println("Error! Passwords do not match.");
			} else {
				validPassword = true;
			}

		} while (!validPassword);

		validOption = false;

		do {

			System.out.println("Please enter your first name");

			String input = sc.nextLine();

			if (StringUtils.isAlphanumeric(input) && input.length() > 3 // checks length
					&& input.length() < 30 && !firstCharacterIsNumber(input)) {
				firstName = input;
				validOption = true;
			} else if (input.length() < 2) {
				System.out.println("Must be more than 1 characters");
			} else if (input.length() > 29) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {
				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		validOption = false;

		do {

			System.out.println("Please enter your last name");

			String input = sc.nextLine();

			if (StringUtils.isAlphanumeric(input) && input.length() > 3 // checks length //TODO maxlength??
					&& input.length() < 30 && !firstCharacterIsNumber(input)) {
				lastName = input;
				validOption = true;
			} else if (input.length() < 1) {
				System.out.println("Must be more than 1 characters");
			} else if (input.length() > 29) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {
				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		validOption = false;

		do {

			System.out.println("Please enter your email address");

			String input = sc.nextLine();

			if (input.length() > 3 && input.length() < 30 && !firstCharacterIsNumber(input)) { // check for format?
				email = input;
				validOption = true;
			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 29) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {
				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		validOption = false;

		do {

			System.out.println("Please enter your street address");

			String input = sc.nextLine();

			if (input.length() > 3 && input.length() < 50 && StringUtils.isAlphanumericSpace(input)) {
				homeAddress = input;
				validOption = true;
			} else if (!StringUtils.isAlphanumeric(input)) {
				System.out.println("Only input numbers and letters");
			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		validOption = false;

		do {

			System.out.println("Please enter your city");

			String input = sc.nextLine();

			if (input.length() < 20 && StringUtils.isAlphanumericSpace(input)) {
				city = input;
				validOption = true;
			} else if (!StringUtils.isAlphanumericSpace(input)) {
				System.out.println("Only input letters please.");
			} else if (input.length() > 19) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {
				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		validOption = false;

		do {

			System.out.println("Please enter your state");

			String input = sc.nextLine();

			if (input.length() >= 2 && input.length() < 20 && StringUtils.isAlphanumericSpace(input)) {
				state = input;
				validOption = true;
			} else if (!StringUtils.isAlphanumericSpace(input)) {
				System.out.println("Only input letters please.");
			} else if (input.length() > 19) {
				System.out.println("Must be less than 20 characters");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		validOption = false;

		do {

			System.out.println("Please enter your zip code");

			String input = sc.nextLine();

			if (input.length() > 3 && input.length() < 10 && StringUtils.isNumeric(input)) {
				zipcode = input;
				validOption = true;
			} else if (!StringUtils.isNumeric(input)) {
				System.out.println("Only input numbers please.");
			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 9) {
				System.out.println("Must be less than 10 characters");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		validOption = false;

		do {

			System.out.println("Please enter your phone number");

			String input = sc.nextLine();

			if (input.length() > 6 && input.length() < 20 && StringUtils.isNumeric(input)) {
				phone = input;
				validOption = true;
			} else if (!StringUtils.isNumeric(input)) {
				System.out.println("Only input numbers please.");
			} else if (input.length() < 6) {
				System.out.println("Must be more than 6 characters");
			} else if (input.length() > 19) {
				System.out.println("Must be less than 20 characters");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);
		validOption = false;
		String input;

		do {

			System.out.println("Username: " + username);
			System.out.println("Password: " + giveMeStarsInsteadOfCharacters(password)); // protects pw
			System.out.println("Email: " + email);
			System.out.println("First Name: " + firstName);
			System.out.println("Last Name: " + lastName);
			System.out.println("Home Address: " + homeAddress);
			System.out.println("City: " + city);
			System.out.println("State: " + state);
			System.out.println("Zip Code: " + zipcode);
			System.out.println("Phone Number: " + phone);
			System.out.println("\n");
			System.out.println("Is this information correct? Please type (Y)es/(N)o.");

			input = sc.nextLine();

			if (input.length() == 1 && (input.equals("Y")) || (input.equals("y")) || (input.equals("N"))
					|| (input.equals("n")) || (input.equals("Yes")) || (input.equals("No")) || (input.equals("yes"))
					|| (input.equals("no"))) {
				validOption = true;
			} else if (!(input.equals("Y")) || (input.equals("y")) || (input.equals("N")) || (input.equals("n"))) {
				System.out.println("Invalid input. Please type (Y)es/(N)o.");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);
		validOption = false;
		if (input.equals("Y") || input.equals("y") || input.equals("Yes") || input.equals("yes")) {
			// call constructor/factory method that saves data into database and text file
			try {
				god.createCustomer(username, password, email, firstName, lastName, homeAddress, city, state, zipcode,
						phone);
				System.out.println("Congratulations! You have successfully been registered in our database");
			} catch (SQLException e) {
				System.out.println("The developers that wrote this suck");
				System.out.println("SQL Exception: " + e.toString());
			} catch (FileNotFoundException e) {
				System.out.println("The configuration files needed to run this app do not exist");
			} catch (IOException e) {
				System.out.println("There was an unexpected error that occured while running this app");
			} catch (ClassNotFoundException e) {
				System.out.println("The database component is not in the app");
			}

		} else {
			startRegistration(); // if information is incorrect, we're back to the beginning of creation
		}

	}

	private static final User promptLogin() { // already created user,
												// now it's time to
												// login

		boolean validOption = false;
		String username, password;
		username = password = "";
		stayLoggedIn = true;
		do {

			System.out.println("Please enter your username");

			String input = sc.nextLine();

			if (StringUtils.isAlphanumeric(input) // check username against database
					&& input.length() > 3 && input.length() < 30 && !firstCharacterIsNumber(input)) {

				try {

					if (god.checkUserName(input)) {
						username = input;
						validOption = true;
					} else {
						System.out.println("Username is not found. checkUsername");
					}

				} catch (SQLException e) {
					System.out.println("The developers who wrote this suck.");
				} catch (FileNotFoundException e) {
					System.out.println("Missing configuration files to run this app.");
				} catch (IOException e) {
					System.out.println("There was an unexpected error while running this app.");
				} catch (ClassNotFoundException e) {
					System.out.println("This app is missing the database component needed to run this app.");
				}

			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 29) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {
				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n");
			}

		} while (!validOption);

		validOption = false;

		do {

			System.out.println("Please enter your password");

			String input = sc.nextLine();

			if (StringUtils.isAlphanumeric(input) // check password against database
					&& input.length() > 3 && input.length() < 30 && !firstCharacterIsNumber(input)) {
				try {

					if (god.checkPassword(username, input)) {
						password = input;
						validOption = true;
					} else {
						System.out.println("password incorrect");
					}

				} catch (SQLException e) {
					System.out.println("The developers who wrote this suck.");
				}

			} else if (input.length() < 4) {
				System.out.println("Must be more than 3 characters");
			} else if (input.length() > 29) {
				System.out.println("Must be less than 30 characters");
			} else if (firstCharacterIsNumber(input)) {
				System.out.println("First character can not be a number.");
			} else {
				System.out.println("Invalid input.\n");
			}
			// god.checkPassword(username);

		} while (!validOption);
		// TODO i've logged in. what's next
		// TODO get user data from database
		try {
			currentUser = god.getUserByUsername(username);
		} catch (FileNotFoundException e) {
			LOGGER.error("file not found while searching for database. " + e.toString());
		} catch (ClassNotFoundException e) {
			LOGGER.error("Database not found. Unable to read from Database" + e.toString());

		} catch (IOException e) {
			LOGGER.error("Cannot read property file to begin connecting to database " + e.toString());

		} catch (SQLException e) {
			LOGGER.error("Data tables not communicating with java program. " + e.toString());

		}
		return currentUser;
	}

	// checks if input starts with a number
	private static final boolean firstCharacterIsNumber(String value) {

		if (StringUtils.isEmpty(value)) {
			return false;
		}

		String firstCharacter = value.substring(0, 1);

		return StringUtils.isNumeric(firstCharacter);

	}

	// protects user's password data
	private static final String giveMeStarsInsteadOfCharacters(String password) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < password.length(); i++) { // keeps number of stars equal to length
			builder.append("*");
		}

		return builder.toString();
	}

	public static Application getApplication() {
		return sApp;
	}

	public static void backupAllData() { // transfer all data into the database
		// TODO
		// create sql code
		// call executeUpdate
		throw new UnsupportedOperationException(); // throw the right exceptions
	}

}
