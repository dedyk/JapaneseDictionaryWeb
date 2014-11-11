create table android_send_missing_word_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    word text null,
    word_place_search varchar(20) null
) default character set = utf8 collate = utf8_polish_ci;
