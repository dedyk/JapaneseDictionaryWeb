package pl.idedyk.japanese.dictionary.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiResult;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.dto.RadicalInfo;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.controller.validator.KanjiDictionarySearchModelValidator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

@Controller
public class KanjiDictionaryController extends CommonController {

	private static final Logger logger = Logger.getLogger(KanjiDictionaryController.class);

	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Autowired  
	private KanjiDictionarySearchModelValidator kanjiDictionarySearchModelValidator;
	
	@InitBinder(value = { "command" })
	private void initBinder(WebDataBinder binder) {  
		binder.setValidator(kanjiDictionarySearchModelValidator);  
	}

	@RequestMapping(value = "/kanjiDictionary", method = RequestMethod.GET)
	public String start(Map<String, Object> model) {
		
		// utworzenie model szukania
		KanjiDictionarySearchModel kanjiDictionarySearchModel = new KanjiDictionarySearchModel();

		// ustawienie domyslnych wartosci model szukania
		kanjiDictionarySearchModel.setWordPlace(WordPlaceSearch.START_WITH.toString());
		
		// pobierz elementy podstawowe
		List<RadicalInfo> radicalList = dictionaryManager.getRadicalList();

		model.put("command", kanjiDictionarySearchModel);
		model.put("radicalList", radicalList);
		model.put("selectedMenu", "kanjiDictionary");
		
		return "kanjiDictionary";
	}
	
	@RequestMapping(value = "/kanjiDictionarySearch", method = RequestMethod.GET)
	public String search(@ModelAttribute("command") @Valid KanjiDictionarySearchModel searchModel,
			BindingResult result, Map<String, Object> model) {

		// pobierz elementy podstawowe
		List<RadicalInfo> radicalList = dictionaryManager.getRadicalList();
		
		if (result.hasErrors() == true) {
						
			model.put("command", searchModel);
			model.put("radicalList", radicalList);
			model.put("selectedMenu", "kanjiDictionary");
			
			return "kanjiDictionary";
		}
		
		// stworzenie obiektu FindKanjiRequest
		FindKanjiRequest findKanjiRequest = createFindKanjiRequest(searchModel);

		logger.info("Wyszukiwanie kanji dla zapytania: " + findKanjiRequest);

		// logowanie
		int fixme = 1;

		// szukanie
		FindKanjiResult findKanjiResult = dictionaryManager.findKanji(findKanjiRequest);
		
		model.put("command", searchModel);
		model.put("radicalList", radicalList);
		model.put("selectedMenu", "kanjiDictionary");
		model.put("findKanjiRequest", findKanjiRequest);
		model.put("findKanjiResult", findKanjiResult);
		
		return "kanjiDictionary";
	}
	
	private FindKanjiRequest createFindKanjiRequest(KanjiDictionarySearchModel searchModel) {
		
		FindKanjiRequest findKanjiRequest = new FindKanjiRequest();

		List<String> tokenWord = Utils.tokenWord(searchModel.getWord());
		
		StringBuffer wordJoined = new StringBuffer();
		
		for (int idx = 0; idx < tokenWord.size(); ++idx) {
			
			wordJoined.append(tokenWord.get(idx));
			
			if (idx != tokenWord.size() - 1) {
				wordJoined.append(" ");
			}
		}
		
		// word
		findKanjiRequest.word = wordJoined.toString();

		// wordPlace
		findKanjiRequest.wordPlaceSearch = FindKanjiRequest.WordPlaceSearch.valueOf(searchModel.getWordPlace());

		// strokeCountFrom
		String strokeCountFrom = searchModel.getStrokeCountFrom();
		
		if (strokeCountFrom != null && strokeCountFrom.trim().equals("") == false) {
			findKanjiRequest.strokeCountFrom = Integer.parseInt(strokeCountFrom);
		}

		// strokeCountTo
		String strokeCountTo = searchModel.getStrokeCountTo();
		
		if (strokeCountTo != null && strokeCountTo.trim().equals("") == false) {
			findKanjiRequest.strokeCountTo = Integer.parseInt(strokeCountTo);
		}

		return findKanjiRequest;
	}
	
	@RequestMapping(produces = "application/json;charset=UTF-8", 
			value = "/kanjiDictionary/autocomplete", method = RequestMethod.GET)
	public @ResponseBody String autocomplete(@RequestParam(value="term", required=true) String term) {

		logger.info("Podpowiadacz słówkowy kanji dla wyrażenia: " + term);

		try {
			List<String> kanjiAutocomplete = dictionaryManager.getKanjiAutocomplete(term, 5);

			JSONArray jsonArray = new JSONArray();

			for (String currentWordAutocomplete : kanjiAutocomplete) {

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
	
	@RequestMapping(produces = "application/json;charset=UTF-8", 
			value = "/kanjiDictionary/showAvailableRadicals", method = RequestMethod.POST)
	public @ResponseBody String showAvailableRadicals(@RequestParam(value="selectedRadicals[]", required=false) String[] selectedRadicals) {

		if (selectedRadicals == null) {
			selectedRadicals = new String[] { };
		}
		
		logger.info("Pokaż dostępne elementy podstawowe dla zapytania: " + Arrays.toString(selectedRadicals));

		Set<String> allAvailableRadicals = dictionaryManager.findAllAvailableRadicals(selectedRadicals);
		
		List<KanjiEntry> findKnownKanjiFromRadicalsResult = dictionaryManager.findKnownKanjiFromRadicals(selectedRadicals);
				
		JSONArray jsonArray = new JSONArray();
		
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("allAvailableRadicals", allAvailableRadicals);
		
		List<String> kanjiFromRadicals = new ArrayList<String>();
		boolean kanjiFromRadicalsMore = false;
		
		if (findKnownKanjiFromRadicalsResult != null) {
			
			for (KanjiEntry currentKanjiEntry : findKnownKanjiFromRadicalsResult) {
				
				kanjiFromRadicals.add(currentKanjiEntry.getKanji());
				
				if (kanjiFromRadicals.size() > 23 - 1) {
					
					kanjiFromRadicalsMore = true;
					
					break;
				}
			}
		}		
		
		jsonObject.put("kanjiFromRadicals", kanjiFromRadicals);
		jsonObject.put("kanjiFromRadicalsMore", kanjiFromRadicalsMore);
		
		jsonArray.put(jsonObject);
		
		return jsonArray.toString();
	}
}
