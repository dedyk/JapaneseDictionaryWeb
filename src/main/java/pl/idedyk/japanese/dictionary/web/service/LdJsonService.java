package pl.idedyk.japanese.dictionary.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary2.api.helper.Dictionary2HelperCommon;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Gloss;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Info;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.KanjiInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.ReadingInfo;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.Sense;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.SenseAdditionalInfo;

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
		
		String name = null;
		List<String> alternativeNameList = new ArrayList<>();
		
		List<KanjiInfo> kanjiInfoList = dictionaryEntry2.getKanjiInfoList();
		List<ReadingInfo> readingInfoList = dictionaryEntry2.getReadingInfoList();
		
		for (KanjiInfo kanjiInfo : kanjiInfoList) {
			
			if (name == null) {
				name = kanjiInfo.getKanji();
				
			} else if (alternativeNameList.contains(kanjiInfo.getKanji()) == false) {
					alternativeNameList.add(kanjiInfo.getKanji());
			}
		}
		
		for (ReadingInfo readingInfo : readingInfoList) {
			
			if (name == null) {
				name = readingInfo.getKana().getValue();
				
				if (alternativeNameList.contains(readingInfo.getKana().getRomaji()) == false) {
					alternativeNameList.add(readingInfo.getKana().getRomaji());
				}
				
			} else {
				if (alternativeNameList.contains(readingInfo.getKana().getValue()) == false) {
					alternativeNameList.add(readingInfo.getKana().getValue());
				}
				
				if (alternativeNameList.contains(readingInfo.getKana().getRomaji()) == false) {
					alternativeNameList.add(readingInfo.getKana().getRomaji());
				}
			}			
		}
		
		//
		
		StringBuffer description = new StringBuffer();
		LinkedHashSet<String> additionalTypeList = new LinkedHashSet<>();
		
		for (int senseNo = 0; senseNo < dictionaryEntry2.getSenseList().size(); ++senseNo) {
			Sense sense = dictionaryEntry2.getSenseList().get(senseNo);
						
			description.append((senseNo + 1) + ". ");
			
			// pobieramy polskie tlumaczenia
			List<Gloss> glossPolList = Dictionary2HelperCommon.getPolishGlossList(sense.getGlossList());
			
			for (Gloss gloss : glossPolList) {
				description.append(gloss.getValue()).append("\n");
			}
			
			// i informacje dodatkowe
			SenseAdditionalInfo senseAdditionalPol = Dictionary2HelperCommon.findFirstPolishAdditionalInfo(sense.getAdditionalInfoList());
			
			if (senseAdditionalPol != null) {
				description.append("- " + senseAdditionalPol.getValue()).append("\n");
			}
			
			//
			
			// czesci mowy
			if (sense.getPartOfSpeechList().size() > 0) {				
				additionalTypeList.addAll(Dictionary2HelperCommon.translateToPolishPartOfSpeechEnum(sense.getPartOfSpeechList()));
			}				

			// dziedzina
			if (sense.getFieldList().size() > 0) {
				additionalTypeList.addAll(Dictionary2HelperCommon.translateToPolishFieldEnumList(sense.getFieldList()));						
			}

			// rozne informacje
			if (sense.getMiscList().size() > 0) {
				additionalTypeList.addAll(Dictionary2HelperCommon.translateToPolishMiscEnumList(sense.getMiscList()));
			}
		}
		
		// pobieramy polskie info
		List<Info> polishInfoList = Dictionary2HelperCommon.getPolishInfoList(dictionaryEntry2.getInfoList());
		
		for (Info info : polishInfoList) {
			description.append("-- " + info.getValue()).append("\n");
		}
		
		//
				
		JSONObject scriptBody = new JSONObject();
		
		scriptBody.put("@context", "https://schema.org");
		scriptBody.put("@type", "DefinedTerm");
		scriptBody.put("@id", baseServer + LinkGenerator.generateDictionaryEntryDetailsLink("", dictionaryEntry2));
		scriptBody.put("termCode", "" + dictionaryEntry2.getEntryId());
		scriptBody.put("name", name);
		scriptBody.put("alternateName", alternativeNameList);
		scriptBody.put("additionalType", additionalTypeList);
		scriptBody.put("description", description.toString());
		scriptBody.put("inLanguage", Arrays.asList("ja", "pl")); // tego nie ma w specyfikacji, ale niech bedzie
		scriptBody.put("inDefinedTermSet", baseServer + "/wordDictionary");
		
		return scriptBody.toString();
	}
}
