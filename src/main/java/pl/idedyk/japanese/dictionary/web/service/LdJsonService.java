package pl.idedyk.japanese.dictionary.web.service;

import java.util.Arrays;
import java.util.Locale;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class LdJsonService {
	
	@Autowired
	private MessageSource messageSource;
	
	public String generateWordDictionaryScript(String contextPath) {		
		JSONObject scriptBody = new JSONObject();
		
		scriptBody.put("@context", "https://schema.org");
		scriptBody.put("@type", "DefinedTermSet");
		scriptBody.put("@id", contextPath + "/wordDictionary");
		scriptBody.put("name", messageSource.getMessage("wordDictionary.page.ldJson.name", new Object[] { }, Locale.getDefault()));
		scriptBody.put("description", messageSource.getMessage("wordDictionary.page.ldJson.description", new Object[] { }, Locale.getDefault()));		
		scriptBody.put("inLanguage", Arrays.asList("ja", "pl"));
		
		return scriptBody.toString();
	}
	
	public String generateKanjiDictionaryScript(String contextPath) {		
		JSONObject scriptBody = new JSONObject();
		
		scriptBody.put("@context", "https://schema.org");
		scriptBody.put("@type", "DefinedTermSet");
		scriptBody.put("@id", contextPath + "/kanjiDictionary");
		scriptBody.put("name", messageSource.getMessage("kanjiDictionary.page.ldJson.name", new Object[] { }, Locale.getDefault()));
		scriptBody.put("description", messageSource.getMessage("kanjiDictionary.page.ldJson.description", new Object[] { }, Locale.getDefault()));		
		scriptBody.put("inLanguage", Arrays.asList("ja", "pl"));
		
		return scriptBody.toString();
	}
}
