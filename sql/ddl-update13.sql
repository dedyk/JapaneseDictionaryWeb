create table android_get_spell_checker_suggestion_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    word text null,
    type varchar(20) not null,
    spell_checker_suggestion_list text null
) default character set = utf8 collate = utf8_polish_ci;
