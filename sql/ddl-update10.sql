alter table word_dictionary_search_log add priority int not null default 1;

alter table word_dictionary_search_missing_words_queue add priority int not null default 1;
