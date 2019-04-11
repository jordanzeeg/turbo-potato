create sequence users_sequence
    minvalue 1
    start with 1
    increment by 1
	
create or replace procedure auto_increment_users(username in users.username%type, password in users.password%type, email in users.email%type, firstname in users.firstname%type, lastname in users.lastname%type, homeaddress in users.homeaddress%type, city in users.city%type, state in users.state%type, zipcode in users.zipcode%type, phone in users.phone%type, role in users.role%type)
is
begin
    insert into users values (users_sequence.nextval, username, password, email, firstname, lastname, homeaddress, city, state, zipcode, phone, 'United States', role);
    
    commit;
end;
/

exec auto_increment_users('supersanta', 'super123', 'supersanta@gmail.com', 'Super', 'Santa', '123 north 123 street', 'north', 'north', '11111', '1234567890', 'Customer');

call auto_increment_users('supersanta1', 'super1231', 'supersanta@gmail.com1', 'Super', 'Santa', '123 north 123 street', 'north', 'north', '11111', '1234567890', 'Customer');
