package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.util.Arrays;
import java.util.Comparator;

public enum GenericLogOperationEnum {
	
	START_APP,
	
	START,
	FAVICON_ICON,
	ROBOTS_GENERATE,
	BING_SITE_AUTH,
	SITEMAP_GENERATE,
	
	WORD_DICTIONARY_START,
	WORD_DICTIONARY_AUTOCOMPLETE,
	WORD_DICTIONARY_SEARCH,
	WORD_DICTIONARY_DETAILS,
	
	WORD_DICTIONARY_CATALOG,
	
	WORD_DICTIONARY_NAME_DETAILS,
	WORD_DICTIONARY_NAME_CATALOG,
	
	KANJI_DICTIONARY_START,
	KANJI_DICTIONARY_AUTOCOMPLETE,
	KANJI_DICTIONARY_SEARCH,
	KANJI_DICTIONARY_RADICALS,
	KANJI_DICTIONARY_DETECT,
	KANJI_DICTIONARY_DETAILS,
	
	KANJI_DICTIONARY_CATALOG,
	
	SUGGESTION_START,
	SUGGESTION_SEND,
	
	ANDROID_SEND_MISSING_WORD,
	
	DAILY_REPORT,
	
	INFO,
	
	ADMIN_REQUEST,
	
	REDIRECT,
	
	GENERAL_EXCEPTION,
	PAGE_NO_FOUND_EXCEPTION,
	METHOD_NOT_ALLOWED_EXCEPTION;
	
	public static GenericLogOperationEnum[] getSortedValues() {
		
		GenericLogOperationEnum[] values = values();
		
		Arrays.sort(values, new Comparator<GenericLogOperationEnum>() {

			@Override
			public int compare(GenericLogOperationEnum o1, GenericLogOperationEnum o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		return values;
	}
}
