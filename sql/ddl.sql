create table generic_log (
    id int not null auto_increment, primary key(id),
    timestamp timestamp not null, index(timestamp),
    session_id varchar(50) null,
    remote_ip varchar(80) not null,
    remote_host varchar(255) null,
    operation varchar(40) not null
) default character set = utf8 collate = utf8_polish_ci;

create table word_dictionary_autocomplete_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    term text null,
    found_elements int null
) default character set = utf8 collate = utf8_polish_ci;

create table word_dictionary_search_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    find_word_request_word text null,
    find_word_request_search_kanji boolean null,
    find_word_request_search_kana boolean null,
    find_word_request_search_romaji boolean null,
    find_word_request_search_translate boolean null,
    find_word_request_search_info boolean null,
    find_word_request_word_place varchar(20) null,
    find_word_request_dictionary_entry_type_list text null,
    find_word_result_result_size int null
) default character set = utf8 collate = utf8_polish_ci;

create table word_dictionary_details_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    dictionary_entry_id int not null,
    dictionary_entry_kanji text null,
    dictionary_entry_kanaList text null,
    dictionary_entry_romajiList text null,
    dictionary_entry_translateList text null,
    dictionary_entry_info text null
) default character set = utf8 collate = utf8_polish_ci;
