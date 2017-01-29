package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum GenericLogOperationEnum {
	
	START_APP(true),
	
	START(true),
	FAVICON_ICON(true),
	ROBOTS_GENERATE(true),
	BING_SITE_AUTH(true),
	SITEMAP_GENERATE(true),
	
	WORD_DICTIONARY_START(true),
	WORD_DICTIONARY_AUTOCOMPLETE(false),
	WORD_DICTIONARY_SEARCH(false),
	WORD_DICTIONARY_DETAILS(true),
	
	WORD_DICTIONARY_CATALOG(true),
	
	WORD_DICTIONARY_PDF_DICTIONARY(true),
	
	WORD_DICTIONARY_NAME_DETAILS(true),
	WORD_DICTIONARY_NAME_CATALOG(true),
	
	KANJI_DICTIONARY_START(true),
	KANJI_DICTIONARY_AUTOCOMPLETE(false),
	KANJI_DICTIONARY_SEARCH(false),
	KANJI_DICTIONARY_RADICALS(false),
	KANJI_DICTIONARY_DETECT(false),
	KANJI_DICTIONARY_DETAILS(true),
	
	KANJI_DICTIONARY_CATALOG(true),
	
	SUGGESTION_START(true),
	SUGGESTION_SEND(false),
	
	ANDROID_SEND_MISSING_WORD(false),
	ANDROID_GET_SPELL_CHECKER_SUGGESTION(false),
	
	DAILY_REPORT(false),
	
	INFO(true),
	
	ADMIN_REQUEST(false),
	
	REDIRECT(true),
	
	GENERAL_EXCEPTION(false),
	PAGE_NO_FOUND_EXCEPTION(true),
	SERVICE_UNAVAILABLE_EXCEPTION(true),
	METHOD_NOT_ALLOWED_EXCEPTION(true);
	
	private boolean exportable;
	
	GenericLogOperationEnum(boolean exportable) {
		this.exportable = exportable;
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
}
