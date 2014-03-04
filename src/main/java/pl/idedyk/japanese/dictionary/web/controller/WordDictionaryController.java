package pl.idedyk.japanese.dictionary.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

@Controller
public class WordDictionaryController extends CommonController {
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@RequestMapping(value = "/wordDictionary", method = RequestMethod.GET)
	public String start(@RequestParam(value="q", required=false, defaultValue = "kot") String query, 
			@RequestParam(value="f", required=false, defaultValue = "1") String f, 
			@RequestParam(value="t", required=false, defaultValue = "999") String t, Map<String, Object> model) {
		
		/*
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
		*/
		
		/*
		FindKanjiRequest findKanjiRequest = new FindKanjiRequest();
		
		findKanjiRequest.word = query;
		findKanjiRequest.wordPlaceSearch = FindKanjiRequest.WordPlaceSearch.ANY_PLACE;
		
		FindKanjiResult findKanjiResult = dictionaryManager.findKanji(findKanjiRequest);
		
		List<KanjiEntry> result = findKanjiResult.result;
		
		for (KanjiEntry kanjiEntry : result) {
			System.out.println(kanjiEntry.getKanji() + " - " + kanjiEntry.getGroups() + " - " + kanjiEntry.getPolishTranslates() + " - " + kanjiEntry.getInfo());
		}
		*/
		
		/*
		List<String> radicalsList = new ArrayList<String>();
		
		for (int i = 0; i < query.length(); i++) {
			
			radicalsList.add(String.valueOf(query.charAt(i)));
		}
				
		//System.out.println(dictionaryManager.findAllAvailableRadicals(radicalsList.toArray(new String[0])));
		
		//List<KanjiEntry> result = dictionaryManager.findKnownKanjiFromRadicals(radicalsList.toArray(new String[0]));
		
		List<KanjiEntry> result = dictionaryManager.findKanjisFromStrokeCount(Integer.parseInt(f), Integer.parseInt(t)).result;
		
		for (KanjiEntry kanjiEntry : result) {
			System.out.println(kanjiEntry.getKanji() + " - " + kanjiEntry.getGroups() + " - " + kanjiEntry.getPolishTranslates() + " - " + kanjiEntry.getInfo());
		}
		*/
		
		//System.out.println("AAA: " + dictionaryManager.getDictionaryEntriesSize());

		/*
		KanjiEntry kanjiEntry = dictionaryManager.findKanji(query);
		
		System.out.println(kanjiEntry.getKanji());
		System.out.println(kanjiEntry.getGroups());
		System.out.println(kanjiEntry.getPolishTranslates());
		System.out.println(kanjiEntry.getKanjiDic2Entry().getKunReading());
		System.out.println(kanjiEntry.getKanjiDic2Entry().getOnReading());
		System.out.println(kanjiEntry.getKanjiDic2Entry().getRadicals());
		System.out.println(kanjiEntry.getKanjivgEntry().getStrokePaths());
		*/
		
		/*
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryById(Integer.parseInt(query));
		
		System.out.println(dictionaryEntry.getId() + " - " + dictionaryEntry.getKanji() + " - " + dictionaryEntry.getKanaList() + " - " + 
				dictionaryEntry.getRomajiList() + " - " + dictionaryEntry.getTranslates() + " - " + dictionaryEntry.getInfo());
		*/
		
		/*
		List<DictionaryEntry> wordsGroup = dictionaryManager.getWordsGroup(10, Integer.parseInt(query));
		*/
		
		/*
		List<DictionaryEntry> wordsGroup = dictionaryManager.getGroupDictionaryEntries(GroupEnum.GENKI_1_6);
		
		for (DictionaryEntry dictionaryEntry : wordsGroup) {
			System.out.println(dictionaryEntry.getId() + " - " + dictionaryEntry.getKanji() + " - " + dictionaryEntry.getKanaList() + " - " + 
					dictionaryEntry.getRomajiList() + " - " + dictionaryEntry.getTranslates() + " - " + dictionaryEntry.getInfo());
		}
		*/
		
		//System.out.println(dictionaryManager.getDictionaryEntryGroupTypes());

		/*
		List<KanjiEntry> result = dictionaryManager.getAllKanjis(false, false);
		
		for (KanjiEntry kanjiEntry : result) {
			System.out.println(kanjiEntry.getKanji() + " - " + kanjiEntry.getGroups() + " - " + kanjiEntry.getPolishTranslates() + " - " + kanjiEntry.getInfo());
		}
		*/
		
		List<KanjiEntry> result = dictionaryManager.findKnownKanji(query);

		for (KanjiEntry kanjiEntry : result) {
			System.out.println(kanjiEntry.getKanji() + " - " + kanjiEntry.getGroups() + " - " + kanjiEntry.getPolishTranslates() + " - " + kanjiEntry.getInfo());
		}
		
		return "wordDictionary";
	}
}
