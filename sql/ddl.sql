create table generic_log (
    id int not null auto_increment, primary key(id),
    timestamp timestamp not null, index(timestamp),
    session_id varchar(50) null,
    user_agent text null,
    remote_ip varchar(80) null,
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

create table kanji_dictionary_autocomplete_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    term text null,
    found_elements int null
) default character set = utf8 collate = utf8_polish_ci;

create table kanji_dictionary_search_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    find_kanji_request_word text null,
    find_kanji_request_word_place varchar(20) null,
    find_kanji_request_stroke_count_from int null,
    find_kanji_request_stroke_count_to int null,
    find_kanji_result_result_size int null
) default character set = utf8 collate = utf8_polish_ci;

create table kanji_dictionary_radicals_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    radicals text null,
    found_elements int null
) default character set = utf8 collate = utf8_polish_ci;

create table kanji_dictionary_detect_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    strokes text null,
    detect_kanji_result text
) default character set = utf8 collate = utf8_polish_ci;

create table kanji_dictionary_details_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    kanji_entry_id int not null,
    kanji_entry_kanji text not null,
    kanji_entry_translateList text null,
    kanji_entry_info text null
) default character set = utf8 collate = utf8_polish_ci;

create table suggestion_send_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    title text null,
    sender text null,
    body text null
) default character set = utf8 collate = utf8_polish_ci;

create table daily_log_processed_ids (
    id int not null, primary key(id)
)  default character set = utf8 collate = utf8_polish_ci;

create table daily_report_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    title text not null,
    report text not null
) default character set = utf8 collate = utf8_polish_ci;

create table general_exception_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    request_uri text null,
    status_code int null,
    exception text null
) default character set = utf8 collate = utf8_polish_ci;