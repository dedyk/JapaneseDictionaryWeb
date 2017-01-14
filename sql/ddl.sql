create table generic_log (
    id int not null auto_increment, primary key(id),
    timestamp timestamp not null, index(timestamp),
    session_id varchar(50) null,
    user_agent text null,
    request_url text null,
    referer_url text null,
    remote_ip varchar(512) null,
    remote_host varchar(512) null,
    operation varchar(40) not null
) default character set = utf8 collate = utf8_polish_ci;

create index generic_log_operation_timestamp on generic_log(operation, timestamp);

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
    find_word_request_search_only_common_words boolean null,
    find_word_request_word_place varchar(20) null,
    find_word_request_dictionary_entry_type_list text null,
    find_word_result_result_size int null,
    priority int not null default 1
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
    strokes mediumtext null,
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
    body mediumtext null
) default character set = utf8 collate = utf8_polish_ci;

create table daily_log_processed_ids (
    id int not null, primary key(id)
)  default character set = utf8 collate = utf8_polish_ci;

create table daily_report_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    title text not null,
    report mediumtext not null
) default character set = utf8 collate = utf8_polish_ci;

create table general_exception_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    status_code int null,
    exception mediumtext null
) default character set = utf8 collate = utf8_polish_ci;

create table queue (
	id int not null auto_increment, primary key(id),
	name varchar(30) not null, 
	status varchar(20) not null,
	host_name varchar(30) not null,
	send_timestamp timestamp not null,
	delivery_count int not null,
	next_attempt timestamp not null,
	object longblob not null,
	index(name, status)
) default character set = utf8 collate = utf8_polish_ci;

create table word_dictionary_catalog_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    page_no int not null
) default character set = utf8 collate = utf8_polish_ci;

create table word_dictionary_name_catalog_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    page_no int not null
) default character set = utf8 collate = utf8_polish_ci;

create table kanji_dictionary_catalog_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    page_no int not null
) default character set = utf8 collate = utf8_polish_ci;

create table admin_request_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    type varchar(50) not null,
    result varchar(50) not null,
    params text null
) default character set = utf8 collate = utf8_polish_ci;

create table android_send_missing_word_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    word text null,
    word_place_search varchar(20) null
) default character set = utf8 collate = utf8_polish_ci;

create table word_dictionary_search_missing_words_queue (
    id int not null auto_increment, primary key(id),
    missing_word varchar(200) character set utf8 collate utf8_bin not null, unique(missing_word),
    counter int not null,
    first_appearance_timestamp timestamp not null,
    last_appearance_timestamp timestamp not null,
    lock_timestamp timestamp null,
    priority int not null default 1
) default character set = utf8 collate = utf8_polish_ci;

create table lock_operation (
    id int not null auto_increment, primary key(id),
    lock_name varchar(30) not null, unique(lock_name),
    lock_timestamp timestamp not null
) default character set = utf8 collate = utf8_polish_ci;

create table android_get_spell_checker_suggestion_log (
    id int not null auto_increment, primary key(id),
    generic_log_id int not null, index(generic_log_id),
    word text null,
    type varchar(20) not null,
    spell_checker_suggestion_list text null
) default character set = utf8 collate = utf8_polish_ci;

