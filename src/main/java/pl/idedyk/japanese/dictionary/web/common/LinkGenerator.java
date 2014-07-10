package pl.idedyk.japanese.dictionary.web.common;

import java.util.List;
import java.util.Properties;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;

public class LinkGenerator {

	public static String generateWordDictionaryLink(String contextPath) {
		
		String linkTemplate = contextPath + "/wordDictionary";
		
		return linkTemplate;		
	}
	
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
		
		// UWAGA: Jesli tu zmieniasz, zmien rowniez w pliku kanjiDictionary.jsp
		
		String linkTemplate = contextPath + "/kanjiDictionaryDetails/%ID%/%KANJI%";

		String kanji = kanjiEntry.getKanji();
		
		return linkTemplate.replaceAll("%ID%", String.valueOf(kanjiEntry.getId())).
        		replaceAll("%KANJI%", kanji != null ? kanji : "-");
	}
	
	public static String generateWordSearchLink(String contextPath, WordDictionarySearchModel searchModel) {
		
		StringBuffer link = new StringBuffer(contextPath + "/wordDictionarySearch?");
		
		// word
		String word = searchModel.getWord();
		
		link.append("word=");
		
		if (word != null) {
			link.append(word);
		}
		
		//word place
		String wordPlace = searchModel.getWordPlace();
		
		link.append("&amp;wordPlace=");
		
		if (wordPlace != null) {
			link.append(wordPlace);
		}
		
		// searchIn
		List<String> searchIn = searchModel.getSearchIn();
		
		if (searchIn != null && searchIn.size() > 0) {			
			for (String currentSearchIn : searchIn) {
				link.append("&amp;searchIn=").append(currentSearchIn);
			}
		}

		// dictionaryTypeStringList
		List<String> dictionaryTypeStringList = searchModel.getDictionaryTypeStringList();
		
		if (dictionaryTypeStringList != null && dictionaryTypeStringList.size() > 0) {			
			for (String currentDictionaryTypeString : dictionaryTypeStringList) {
				link.append("&amp;dictionaryTypeStringList=").append(currentDictionaryTypeString);
			}
		}		
		
		return link.toString();
	}
	
	public static String generateSendSuggestionLink(String contextPath) {
		
		// UWAGA: Jesli tu zmieniasz, zmien rowniez w pliku SuggestionController.java
		
		return contextPath + "/suggestion/sendSuggestion";
	}
	
	public static String getStaticPrefix(String contextPath, Properties applicationProperties) {
		
		boolean useExternalStaticFiles = Boolean.valueOf((String)applicationProperties.get("use.external.static.files"));
		
		if (useExternalStaticFiles == true) {			
			return (String)applicationProperties.getProperty("use.external.static.path");
			
		} else {
			
			return contextPath;
		}
	}
}
