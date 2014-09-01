create table admin_request_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    type varchar(50) not null,
    result varchar(50) not null,
    params text null
) default character set = utf8 collate = utf8_polish_ci;
