create table word_dictionary_search_missing_words_queue (
    id int not null auto_increment, primary key(id),
    missing_word varchar(200) not null, unique(missing_word),
    counter int not null,
    first_appearance_timestamp timestamp not null,
    last_appearance_timestamp timestamp not null,
    lock_timestamp timestamp null
) default character set = utf8 collate = utf8_polish_ci;
