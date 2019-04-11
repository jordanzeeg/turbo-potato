create table users (
id number NOT NULL,
username varchar2(30) NOT NULL,
password varchar2(30) NOT NULL,
email varchar2(30) NOT NULL,
firstName varchar2(30) NOT NULL,
lastname varchar2(30) NOT NULL,
homeAddress varchar2(50) NOT NULL,
city varchar2(20) NOT NULL,
state varchar2(20) NOT NULL,
zipcode varchar2(10) NOT NULL,
phone varchar2(15) NOT NULL,
country varchar2(30) NOT NULL,
role varchar2(20) NOT NULL,
PRIMARY KEY (id),
check(role in ('Employee', 'Manager', 'Administrator', 'Customer')),
constraint unique_username UNIQUE (username),
constraint unique_email UNIQUE (email)
)

create table account (
id number NOT NULL,
label varchar2(20) NOT NULL,
amount number(15,2) NOT NULL,
type varchar2(15) NOT NULL,
ownership varchar2(15) NOT NULL,
status varchar2(15) NOT NULL,
PRIMARY KEY (id),
check(type in ('Checking', 'Savings', 'MoneyMarket', 'IRA', 'Brokerage', 'CD')),
check(ownership in ('Single', 'Joint')),
check(status in ('Denied', 'Approved', 'Closed', 'Pending'))
)

create table account_owner(
id number NOT NULL,
ownerId number NOT NULL,
constraint unique_account_owner UNIQUE (id, ownerId),
FOREIGN KEY (id) references account(id),
FOREIGN KEY (ownerId) references users(id)
)