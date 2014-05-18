create table generic_log (
    id int not null auto_increment, primary key(id),
    timestamp timestamp not null, index(timestamp),
    session_id varchar(50) null,
    remote_ip varchar(80) not null,
    remote_host varchar(255) null,
    operation varchar(40) not null
) default character set = utf8 collate = utf8_polish_ci;

create table word_dictionary_autocomplete (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    term varchar(50) null,
    found_elements int null
) default character set = utf8 collate = utf8_polish_ci;
