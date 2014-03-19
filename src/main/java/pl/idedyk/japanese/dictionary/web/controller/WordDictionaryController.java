package pl.idedyk.japanese.dictionary.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.controller.validator.WordDictionarySearchModelValidator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

@Controller
public class WordDictionaryController extends CommonController {

	private static final Logger logger = Logger.getLogger(WordDictionaryController.class);

	@Autowired
	private DictionaryManager dictionaryManager;

	@Autowired  
	private WordDictionarySearchModelValidator wordDictionarySearchModelValidator;

	 @Autowired private MessageSource messageSource;
	
	@InitBinder(value = { "command" })
	private void initBinder(WebDataBinder binder) {  
		binder.setValidator(wordDictionarySearchModelValidator);  
	}

	@RequestMapping(value = "/wordDictionary", method = RequestMethod.GET)
	public String start(Map<String, Object> model) {

		// utworzenie model szukania
		WordDictionarySearchModel wordDictionarySearchModel = new WordDictionarySearchModel();

		// ustawienie domyslnych wartosci model szukania
		wordDictionarySearchModel.setWordPlace(WordPlaceSearch.START_WITH.toString());

		// ustawienie miejsca szukania		
		wordDictionarySearchModel.setSearchIn(Arrays.asList("KANJI", "KANA", "ROMAJI", "TRANSLATE", "INFO"));

		// pobranie wyswietlanych typow
		List<DictionaryEntryType> addableDictionaryEntryList = DictionaryEntryType.getAddableDictionaryEntryList();

		for (DictionaryEntryType dictionaryEntryType : addableDictionaryEntryList) {
			wordDictionarySearchModel.addDictionaryType(dictionaryEntryType);
		}

		model.put("addableDictionaryEntryList", addableDictionaryEntryList);
		model.put("command", wordDictionarySearchModel);
		model.put("selectedMenu", "wordDictionary");

		return "wordDictionary";
	}

	@RequestMapping(value = "/wordDictionary", method = RequestMethod.POST)
	public String search(@ModelAttribute("command") @Valid WordDictionarySearchModel searchModel,
			BindingResult result, Map<String, Object> model) {

		if (result.hasErrors() == true) {
						
			model.put("addableDictionaryEntryList", DictionaryEntryType.getAddableDictionaryEntryList());
			model.put("command", searchModel);
			model.put("selectedMenu", "wordDictionary");
			
			return "wordDictionary";
		}
		
		// stworzenie obiektu FindWordRequest
		FindWordRequest findWordRequest = createFindWordRequest(searchModel);
		
		// logowanie
		int fixme = 1;

		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findWord(findWordRequest);
		
		model.put("addableDictionaryEntryList", DictionaryEntryType.getAddableDictionaryEntryList());
		model.put("command", searchModel);
		model.put("selectedMenu", "wordDictionary");
		model.put("findWordRequest", findWordRequest);
		model.put("findWordResult", findWordResult);
		
		return "wordDictionary";
	}
	
	private FindWordRequest createFindWordRequest(WordDictionarySearchModel searchModel) {
		
		FindWordRequest findWordRequest = new FindWordRequest();
		
		List<String> tokenWord = Utils.tokenWord(searchModel.getWord());
		
		StringBuffer wordJoined = new StringBuffer();
		
		for (int idx = 0; idx < tokenWord.size(); ++idx) {
			
			wordJoined.append(tokenWord.get(idx));
			
			if (idx != tokenWord.size() - 1) {
				wordJoined.append(" ");
			}
		}
		
		// word
		findWordRequest.word = wordJoined.toString();
		
		// wordPlace
		findWordRequest.wordPlaceSearch = FindWordRequest.WordPlaceSearch.valueOf(searchModel.getWordPlace());
		
		// searchIn
		findWordRequest.searchKanji = false;
		findWordRequest.searchKana = false;
		findWordRequest.searchRomaji = false;
		findWordRequest.searchTranslate = false;
		findWordRequest.searchInfo = false;

		List<String> searchIn = searchModel.getSearchIn();
		
		for (String currentSearch : searchIn) {
			
			if (Utils.isKanjiSearchIn(currentSearch) == true) {
				findWordRequest.searchKanji = true;
			}

			if (Utils.isKanaSearchIn(currentSearch) == true) {
				findWordRequest.searchKana = true;
			}

			if (Utils.isRomajiSearchIn(currentSearch) == true) {
				findWordRequest.searchRomaji = true;
			}

			if (Utils.isTranslateSearchIn(currentSearch) == true) {
				findWordRequest.searchTranslate = true;
			}

			if (Utils.isInfoSearchIn(currentSearch) == true) {
				findWordRequest.searchInfo = true;
			}
		}
		
		// dictionaryEntryList
		List<String> dictionaryTypeStringList = searchModel.getDictionaryTypeStringList();
				
		List<DictionaryEntryType> addableDictionaryEntryList = DictionaryEntryType.getAddableDictionaryEntryList();
		
		if (dictionaryTypeStringList.size() == addableDictionaryEntryList.size()) {			
			findWordRequest.dictionaryEntryList = null;
			
		} else {
			findWordRequest.dictionaryEntryList = new ArrayList<DictionaryEntryType>();
			
			for (String currentDictionaryTypeString : dictionaryTypeStringList) {
				findWordRequest.dictionaryEntryList.add(DictionaryEntryType.valueOf(currentDictionaryTypeString));
			}			
		}		
		
		return findWordRequest;
	}

	@RequestMapping(produces = "application/json;charset=UTF-8", 
			value = "/wordDictionary/autocomplete", method = RequestMethod.GET)
	public @ResponseBody String autocomplete(@RequestParam(value="term", required=true) String term) {

		logger.info("Podpowiadacz słówkowy dla wyrażenia: " + term);

		try {
			List<String> wordAutocomplete = dictionaryManager.getWordAutocomplete(term, 5);

			JSONArray jsonArray = new JSONArray();

			for (String currentWordAutocomplete : wordAutocomplete) {

				JSONObject jsonObject = new JSONObject();

				jsonObject.put("label", currentWordAutocomplete);
				jsonObject.put("value", currentWordAutocomplete);

				jsonArray.put(jsonObject);
			}

			return jsonArray.toString();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@RequestMapping(value = "/wordDictionaryDetails/{id}/{kanji}/{kana}", method = RequestMethod.GET)
	public String start(@PathVariable("id") int id, @PathVariable("kanji") String kanji,
			@PathVariable("kana") String kana, Map<String, Object> model) {
		
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryById(id);
		
		int fixme = 1;
		// nie znaleziono slowka
		
		// zrobic powrot
		// logowanie
		
		// tytul strony
		if (dictionaryEntry != null) {
			
			String dictionaryEntryKanji = dictionaryEntry.getKanji();
			List<String> dictionaryEntryKanaList = dictionaryEntry.getKanaList();
			List<String> dictionaryEntryRomajiList = dictionaryEntry.getRomajiList();
						
			String pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title", 
					new Object[] { dictionaryEntryKanji != null ? dictionaryEntryKanji : "-",
							dictionaryEntryKanaList != null && dictionaryEntryKanaList.size() > 0 ? dictionaryEntryKanaList.get(0) : "-",
							dictionaryEntryRomajiList != null && dictionaryEntryRomajiList.size() > 0 ? dictionaryEntryRomajiList.get(0) : "-",
					}, Locale.getDefault());
			
			model.put("pageTitle", pageTitle);
			
		} else {
			
			String pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title", 
					new Object[] { "-", "-", "-" }, Locale.getDefault());
			
			model.put("pageTitle", pageTitle);
		}
				
		model.put("dictionaryEntry", dictionaryEntry);
		model.put("selectedMenu", "wordDictionary");
		
		return "wordDictionaryDetails";
	}
}
