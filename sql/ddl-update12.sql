create table lock_operation (
    id int not null auto_increment, primary key(id),
    lock_name varchar(30) not null, unique(lock_name),
    lock_timestamp timestamp not null
) default character set = utf8 collate = utf8_polish_ci;
