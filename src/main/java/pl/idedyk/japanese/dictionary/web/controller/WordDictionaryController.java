package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult.ResultItem;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

@Controller
public class WordDictionaryController extends CommonController {
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@RequestMapping(value = "/wordDictionary", method = RequestMethod.GET)
	public String start(@RequestParam(value="q", required=false, defaultValue = "kot") String query, Map<String, Object> model) {
				
		long start = System.currentTimeMillis();
		
		try {			
			FindWordRequest findWordRequest = new FindWordRequest();
			
			findWordRequest.word = query;
			findWordRequest.wordPlaceSearch = WordPlaceSearch.START_WITH;
			
			//findWordRequest.dictionaryEntryList = new ArrayList<DictionaryEntryType>();
			
			//findWordRequest.dictionaryEntryList.add(DictionaryEntryType.WORD_VERB_U);
			// findWordRequest.dictionaryEntryList.add(DictionaryEntryType.WORD_VERB_RU);
			//findWordRequest.dictionaryEntryList.add(DictionaryEntryType.WORD_NOUN);
			//findWordRequest.dictionaryEntryList.add(DictionaryEntryType.WORD_VERB_IRREGULAR);
			
			long findStart = System.currentTimeMillis();
			FindWordResult findDictionaryEntries = dictionaryManager.findWord(findWordRequest);
			long findStop = System.currentTimeMillis();
			
			System.out.println("Czas(find): " + (findStop - findStart));
			
			for (ResultItem resultItem : findDictionaryEntries.result) {
				
				System.out.println(resultItem.getKanji() + " - " + resultItem.getKanaList() + " - " + 
				resultItem.getRomajiList() + " - " + resultItem.getTranslates() + " - " + resultItem.getInfo());
				
			}
			
			System.out.println("Ilosc: " + findDictionaryEntries.result.size());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			//sqliteConnector.close();
		}
		
		long stop = System.currentTimeMillis();
		
		System.out.println("Czas: " + (stop - start));
		
		return "wordDictionary";
	}
	
}
