create table android_queue_event_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    user_id varchar(40) not null,
    operation varchar(50) not null,
    create_date timestamp not null,
    params text null
) default character set = utf8 collate = utf8_polish_ci;
