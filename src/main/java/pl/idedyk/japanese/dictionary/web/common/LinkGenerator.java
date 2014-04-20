package pl.idedyk.japanese.dictionary.web.common;

import java.util.List;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;

public class LinkGenerator {

	public static String generateDictionaryEntryDetailsLink(String contextPath, DictionaryEntry dictionaryEntry, 
			DictionaryEntryType forceDictionaryEntryType) {
		
		String kanji = dictionaryEntry.getKanji();
		List<String> kanaList = dictionaryEntry.getKanaList();
		
		if (forceDictionaryEntryType == null) {
			
			String linkTemplate = contextPath + "/wordDictionaryDetails/%ID%/%KANJI%/%KANA%";
			
            return linkTemplate.replaceAll("%ID%", String.valueOf(dictionaryEntry.getId())).
            		replaceAll("%KANJI%", kanji != null ? kanji : "-").
            		replaceAll("%KANA%", kanaList != null && kanaList.size() > 0 ? kanaList.get(0) : "-");
			
		} else {
			
			String linkTemplate = contextPath + "/wordDictionaryDetails/%ID%/%KANJI%/%KANA%?forceDictionaryEntryType=%FORCEDICTIONARYENTRYTYPE%";
			
            return linkTemplate.replaceAll("%ID%", String.valueOf(dictionaryEntry.getId())).
            		replaceAll("%KANJI%", kanji != null ? kanji : "-").
            		replaceAll("%KANA%", kanaList != null && kanaList.size() > 0 ? kanaList.get(0) : "-").
            		replaceAll("%FORCEDICTIONARYENTRYTYPE%", forceDictionaryEntryType.toString());			
		}
	}
	
	public static String generateKanjiDetailsLink(String contextPath, KanjiEntry kanjiEntry) {
		
		String linkTemplate = contextPath + "/kanjiDictionaryDetails/%ID%/%KANJI%";

		String kanji = kanjiEntry.getKanji();
		
		return linkTemplate.replaceAll("%ID%", String.valueOf(kanjiEntry.getId())).
        		replaceAll("%KANJI%", kanji != null ? kanji : "-");
	}
}
