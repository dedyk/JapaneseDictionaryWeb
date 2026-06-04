package pl.idedyk.japanese.dictionary.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.KanjiInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingInfo;

@Service
public class LdJsonService {
	
	@Autowired
	private MessageSource messageSource;
	
	public String generateWordDictionaryScript(String baseServer) {		
		JSONObject scriptBody = new JSONObject();
		
		scriptBody.put("@context", "https://schema.org");
		scriptBody.put("@type", "DefinedTermSet");
		scriptBody.put("@id", baseServer + "/wordDictionary");
		scriptBody.put("name", messageSource.getMessage("wordDictionary.page.ldJson.name", new Object[] { }, Locale.getDefault()));
		scriptBody.put("description", messageSource.getMessage("wordDictionary.page.ldJson.description", new Object[] { }, Locale.getDefault()));		
		scriptBody.put("inLanguage", Arrays.asList("ja", "pl"));
		
		return scriptBody.toString();
	}
	
	public String generateKanjiDictionaryScript(String baseServer) {		
		JSONObject scriptBody = new JSONObject();
		
		scriptBody.put("@context", "https://schema.org");
		scriptBody.put("@type", "DefinedTermSet");
		scriptBody.put("@id", baseServer + "/kanjiDictionary");
		scriptBody.put("name", messageSource.getMessage("kanjiDictionary.page.ldJson.name", new Object[] { }, Locale.getDefault()));
		scriptBody.put("description", messageSource.getMessage("kanjiDictionary.page.ldJson.description", new Object[] { }, Locale.getDefault()));		
		scriptBody.put("inLanguage", Arrays.asList("ja", "pl"));
		
		return scriptBody.toString();
	}
	
	public String generateJmdictScript(String baseServer, JMdict.Entry dictionaryEntry2) {
		JSONObject scriptBody = new JSONObject();
		
		scriptBody.put("@context", "https://schema.org");
		scriptBody.put("@type", "DefinedTerm");
		scriptBody.put("@id", baseServer + LinkGenerator.generateDictionaryEntryDetailsLink("", dictionaryEntry2));
		
		String name = null;
		List<String> alternativeNameList = new ArrayList<>();
		
		List<KanjiInfo> kanjiInfoList = dictionaryEntry2.getKanjiInfoList();
		List<ReadingInfo> readingInfoList = dictionaryEntry2.getReadingInfoList();
		
		for (KanjiInfo kanjiInfo : kanjiInfoList) {
			
			if (name == null) {
				name = kanjiInfo.getKanji();
				
			} else {
				alternativeNameList.add(kanjiInfo.getKanji());
			}
		}
		
		for (ReadingInfo readingInfo : readingInfoList) {
			
			if (name == null) {
				name = readingInfo.getKana().getValue();
				alternativeNameList.add(readingInfo.getKana().getRomaji());
				
			} else {
				alternativeNameList.add(readingInfo.getKana().getValue());
				alternativeNameList.add(readingInfo.getKana().getRomaji());
			}			
		}
		
		scriptBody.put("name", name);
		scriptBody.put("alternateName", alternativeNameList);
		
		
		return scriptBody.toString();
	}
}
