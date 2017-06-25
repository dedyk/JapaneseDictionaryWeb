package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum GenericLogOperationEnum {
	
	START_APP(90),
	
	START(90),
	FAVICON_ICON(90),
	ROBOTS_GENERATE(90),
	BING_SITE_AUTH(90),
	SITEMAP_GENERATE(90),
	
	WORD_DICTIONARY_START(90),
	WORD_DICTIONARY_AUTOCOMPLETE(),
	WORD_DICTIONARY_SEARCH(),
	WORD_DICTIONARY_DETAILS(90),
	
	WORD_DICTIONARY_CATALOG(90),
	
	WORD_DICTIONARY_PDF_DICTIONARY(365),
	
	WORD_DICTIONARY_NAME_DETAILS(90),
	WORD_DICTIONARY_NAME_CATALOG(90),
	
	KANJI_DICTIONARY_START(90),
	KANJI_DICTIONARY_AUTOCOMPLETE(),
	KANJI_DICTIONARY_SEARCH(),
	KANJI_DICTIONARY_RADICALS(),
	KANJI_DICTIONARY_DETECT(),
	KANJI_DICTIONARY_DETAILS(90),
	
	KANJI_DICTIONARY_CATALOG(90),
	
	SUGGESTION_START(90),
	SUGGESTION_SEND(),
	
	ANDROID_SEND_MISSING_WORD(),
	ANDROID_GET_SPELL_CHECKER_SUGGESTION(),
	
	DAILY_REPORT(),
	
	INFO(90),
	
	ADMIN_REQUEST(),
	
	REDIRECT(90),
	
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
