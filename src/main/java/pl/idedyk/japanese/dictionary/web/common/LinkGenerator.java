package pl.idedyk.japanese.dictionary.web.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

public class LinkGenerator {

	public static String generateDictionaryEntryDetailsLink(String contextPath, DictionaryEntry dictionaryEntry, 
			DictionaryEntryType forceDictionaryEntryType) {
				
		try {
			boolean name = dictionaryEntry.isName();
			
			String pathPrefix = null;
			
			if (name == false) {
				pathPrefix = "wordDictionaryDetails";
				
			} else {
				pathPrefix = "wordDictionaryNameDetails";
			}
			
			String kanji = dictionaryEntry.getKanji();
			String kana = dictionaryEntry.getKana();
			
			String uniqueKey = dictionaryEntry.getUniqueKey();
			
			if (uniqueKey == null) { // stary sposob generowania linku
				
				if (forceDictionaryEntryType == null) {
					
					String linkTemplate = contextPath + "/" + pathPrefix + "/%ID%/%KANJI%/%KANA%";
					
		            return linkTemplate.replaceAll("%ID%", String.valueOf(dictionaryEntry.getId())).
		            		replaceAll("%KANJI%", kanji != null ? URLEncoder.encode(kanji, "UTF-8") : "-").
		            		replaceAll("%KANA%", URLEncoder.encode(kana, "UTF-8"));
					
				} else {
					
					String linkTemplate = contextPath + "/" + pathPrefix + "/%ID%/%KANJI%/%KANA%?forceDictionaryEntryType=%FORCEDICTIONARYENTRYTYPE%";
					
		            return linkTemplate.replaceAll("%ID%", String.valueOf(dictionaryEntry.getId())).
		            		replaceAll("%KANJI%", kanji != null ? URLEncoder.encode(kanji, "UTF-8") : "-").
		            		replaceAll("%KANA%", URLEncoder.encode(kana, "UTF-8")).
		            		replaceAll("%FORCEDICTIONARYENTRYTYPE%", forceDictionaryEntryType.toString());			
				}				
				
			} else { // nowy sposob generowania linku
				
				String[] uniqueKeySplited = uniqueKey.split("/");
				StringBuffer uniqueKeySb = new StringBuffer();
				
				for (int i = 0; i < uniqueKeySplited.length; ++i) {
					uniqueKeySb.append(URLEncoder.encode(uniqueKeySplited[i], "UTF-8"));
							
					if (i != uniqueKeySplited.length - 1) {
						uniqueKeySb.append("/");
					}
				}				
				
				if (forceDictionaryEntryType == null) {
					
					String linkTemplate = contextPath + "/" + pathPrefix + "2/" + uniqueKeySb.toString();
					
					return linkTemplate;

				} else {
					
					String linkTemplate = contextPath + "/" + pathPrefix + "2/" + uniqueKeySb.toString() + "?forceDictionaryEntryType=%FORCEDICTIONARYENTRYTYPE%";
					
		            return linkTemplate.replaceAll("%FORCEDICTIONARYENTRYTYPE%", forceDictionaryEntryType.toString());								
				}			
			}
			
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String generateKanjiDetailsLink(String contextPath, KanjiCharacterInfo kanjiEntry) {
		
		try {
			// UWAGA: Jesli tu zmieniasz, zmien rowniez w pliku kanjiDictionary.jsp
			
			String linkTemplate = contextPath + "/kanjiDictionaryDetails/%ID%/%KANJI%";
	
			String kanji = kanjiEntry.getKanji();
			
			return linkTemplate.replaceAll("%ID%", String.valueOf(kanjiEntry.getId())).
	        		replaceAll("%KANJI%", kanji != null ? URLEncoder.encode(kanji, "UTF-8") : "-");
			
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String generateWordSearchLink(String contextPath, WordDictionarySearchModel searchModel) {
		
		StringBuffer link = new StringBuffer(contextPath + "/wordDictionarySearch?");
		
		// word
		String word = searchModel.getWord();
		
		link.append("word=");
		
		if (word != null) {
			link.append(word);
		}
		
		// word place
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
	
	public static String generateKanjiSearchLink(String contextPath, KanjiDictionarySearchModel searchModel) {
		
		StringBuffer link = new StringBuffer(contextPath + "/kanjiDictionarySearch?");
		
		// word
		String word = searchModel.getWord();
		
		link.append("word=");
		
		if (word != null) {
			link.append(word);
		}

		// word place
		String wordPlace = searchModel.getWordPlace();
		
		link.append("&amp;wordPlace=");
		
		if (wordPlace != null) {
			link.append(wordPlace);
		}

		// strokeCountFrom
		String strokeCountFrom = searchModel.getStrokeCountFrom();
		
		link.append("&amp;strokeCountFrom=");
		
		if (strokeCountFrom != null) {
			link.append(strokeCountFrom);
		}
		
		// strokeCountTo
		String strokeCountTo = searchModel.getStrokeCountTo();
		
		link.append("&amp;strokeCountTo=");
		
		if (strokeCountTo != null) {
			link.append(strokeCountTo);
		}
		
		return link.toString();
	}
	
	public static String generateSendSuggestionLink(String contextPath) {
		
		// UWAGA: Jesli tu zmieniasz, zmien rowniez w pliku SuggestionController.java
		
		return contextPath + "/suggestion/sendSuggestion";
	}
	
	public static String generateShowGenericLog(String contextPath, GenericLog genericLog) {

		// UWAGA: Jesli tu zmieniasz, zmien rowniez w pliku AdminController.java
		
		return contextPath + "/adm/showGenericLog/" + genericLog.getId();
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
