create table word_dictionary_name_details_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    dictionary_entry_id int not null,
    dictionary_entry_kanji text null,
    dictionary_entry_kanaList text null,
    dictionary_entry_romajiList text null,
    dictionary_entry_translateList text null,
    dictionary_entry_info text null
) default character set = utf8 collate = utf8_polish_ci;

create table word_dictionary_name_catalog_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    page_no int not null
) default character set = utf8 collate = utf8_polish_ci;
