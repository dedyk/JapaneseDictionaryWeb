create table ListEntries_DictionaryEntries_AttributeList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_DictionaryEntries_DictionaryEntryTypeList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_DictionaryEntries_GroupsList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_DictionaryEntries_KanaList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_DictionaryEntries_RomajiList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_DictionaryEntries_TranslateList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_DictionaryEntries_InfoStringList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table DictionaryEntries(
    id int not null, primary key(id),
    prefixKana varchar(10) not null,
    kanji varchar(100) not null, index(kanji),
    prefixRomaji varchar(10) not null,
    info text not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_KanjiEntries_RadicalsList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_KanjiEntries_OnReadingList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_KanjiEntries_KunReadingList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_KanjiEntries_PolishTranslateList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_KanjiEntries_GroupsList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table ListEntries_KanjiEntries_InfoStringList(
    id int not null, primary key(id),
    key_name varchar(20) not null, index(key_name),
    value text not null, fulltext(value),
    special boolean not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;

create table KanjiEntries(
    id int not null, primary key(id),
    kanji varchar(100) not null, unique(kanji),
    strokeCount int not null,
    strokePaths text not null,
    generated text not null) default character set = utf8 collate = utf8_polish_ci ENGINE=MyISAM;
