create table word_dictionary_unique_search_log (
    id int not null auto_increment, primary key(id),
    word varchar(70) character set utf8 collate utf8_bin not null, unique(word),
    counter int not null,
    first_appearance_timestamp timestamp not null,
    last_appearance_timestamp timestamp not null
) default character set = utf8 collate = utf8_polish_ci;

-- migracja danych:

insert into word_dictionary_unique_search_log(word, counter, first_appearance_timestamp, last_appearance_timestamp)
select
     substring(wdsl.find_word_request_word, 1, 65),
     count(*),
     min(gl.timestamp),
     max(gl.timestamp)
from
    word_dictionary_search_log wdsl, generic_log gl
where
    gl.id = wdsl.generic_log_id
group by
    substring(wdsl.find_word_request_word, 1, 65);
