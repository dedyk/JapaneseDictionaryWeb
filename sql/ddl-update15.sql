create table word_dictionary_unique_search_log (
    id int not null auto_increment, primary key(id),
    word varchar(70) character set utf8 collate utf8_bin not null, unique(word),
    counter int not null,
    first_appearance_timestamp timestamp not null,
    last_appearance_timestamp timestamp not null
) default character set = utf8 collate = utf8_polish_ci;

-- migracja danych:

insert into word_dictionary_unique_search_log(word, counter, first_appearance_timestamp, last_appearance_timestamp)
select distinct 
    substring(find_word_request_word, 1, 65) COLLATE utf8_bin, 
    (select count(*) from word_dictionary_search_log wdsl2 where substring(wdsl2.find_word_request_word, 1, 65) = substring(wdsl.find_word_request_word, 1, 65)),
    (select timestamp from generic_log gl2, word_dictionary_search_log wdsl2 where gl2.id = wdsl2.generic_log_id and substring(wdsl2.find_word_request_word, 1, 65) = substring(wdsl.find_word_request_word, 1, 65) order by gl2.id limit 1), 
    (select timestamp from generic_log gl2, word_dictionary_search_log wdsl2 where gl2.id = wdsl2.generic_log_id and substring(wdsl2.find_word_request_word, 1, 65) = substring(wdsl.find_word_request_word, 1, 65) order by gl2.id desc limit 1) 
from
    word_dictionary_search_log wdsl, generic_log gl
where
    substring(wdsl.find_word_request_word, 1, 65) not in (select substring(word, 1, 65) COLLATE utf8_bin from word_dictionary_unique_search_log) and gl.id = wdsl.generic_log_id;
