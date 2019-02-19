package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum GenericLogOperationEnum {
	
	START_APP(30),
	
	START(30),
	FAVICON_ICON(30),
	ROBOTS_GENERATE(30),
	BING_SITE_AUTH(30),
	SITEMAP_GENERATE(30),
	
	WORD_DICTIONARY_START(30),
	WORD_DICTIONARY_AUTOCOMPLETE(30),
	WORD_DICTIONARY_SEARCH(90),
	WORD_DICTIONARY_DETAILS(30),
	
	WORD_DICTIONARY_CATALOG(30),
	
	WORD_DICTIONARY_PDF_DICTIONARY(90),
	
	WORD_DICTIONARY_NAME_DETAILS(30),
	WORD_DICTIONARY_NAME_CATALOG(30),
	
	WORD_DICTIONARY_GET_TATOEBA_SENTENCES(30),
	WORD_DICTIONARY_GET_GROUP_DICT_ENTRIES(30),
	
	WORD_DICTIONARY_GET_DICT_ENT_SIZE(30),
	WORD_DICTIONARY_GET_DICT_ENT_NAME_SIZE(30),
	WORD_DICTIONARY_GET_DICT_ENT_GROUP_TYPES(30),
	WORD_DICTIONARY_GET_TRANS_INTRANS_PAIR(30),
	WORD_DICTIONARY_GET_WORD_POWER_LIST(30),
	
	KANJI_DICTIONARY_START(30),
	KANJI_DICTIONARY_AUTOCOMPLETE(30),
	KANJI_DICTIONARY_SEARCH(90),
	KANJI_DICTIONARY_SEARCH_STROKE_COUNT(30),
	KANJI_DICTIONARY_GET_ALL_KANJIS(30),
	KANJI_DICTIONARY_RADICALS(),
	KANJI_DICTIONARY_DETECT(),
	KANJI_DICTIONARY_DETAILS(30),
	
	KANJI_DICTIONARY_CATALOG(30),
	
	SUGGESTION_START(30),
	SUGGESTION_SEND(),
	
	ANDROID_SEND_MISSING_WORD(),
	ANDROID_GET_SPELL_CHECKER_SUGGESTION(),
	
	DAILY_REPORT(60),
	
	INFO(30),
	
	ADMIN_REQUEST(),
	
	REDIRECT(30),
	
	GENERAL_EXCEPTION(),
	PAGE_NO_FOUND_EXCEPTION(90),
	SERVICE_UNAVAILABLE_EXCEPTION(90),
	METHOD_NOT_ALLOWED_EXCEPTION(90);
	
	private boolean exportable;
	
	private int dayOlderThan = Integer.MAX_VALUE;
	
	GenericLogOperationEnum() {
		this.exportable = false;
	}

	GenericLogOperationEnum(int dayOlderThan) {
		this.exportable = true;
		this.dayOlderThan = dayOlderThan;
	}

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
	
	public static List<GenericLogOperationEnum> getAllExportableOperationList() {
		
		GenericLogOperationEnum[] values = values();
		
		List<GenericLogOperationEnum> result = new ArrayList<GenericLogOperationEnum>();
		
		for (GenericLogOperationEnum genericLogOperationEnum : values) {
			
			if (genericLogOperationEnum.isExportable() == true) {
				result.add(genericLogOperationEnum);
			}
		}
		
		return result;
	}

	public boolean isExportable() {
		return exportable;
	}

	public int getDayOlderThan() {
		return dayOlderThan;
	}
}
