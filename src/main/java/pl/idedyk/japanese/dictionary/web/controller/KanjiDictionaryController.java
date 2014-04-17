package pl.idedyk.japanese.dictionary.web.controller;

import java.util.List;
import java.util.Map;

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

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest.WordPlaceSearch;
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

		model.put("command", kanjiDictionarySearchModel);
		model.put("selectedMenu", "kanjiDictionary");
		
		return "kanjiDictionary";
	}
	
	@RequestMapping(value = "/kanjiDictionaryDetails", method = RequestMethod.GET)
	public String search(@ModelAttribute("command") @Valid KanjiDictionarySearchModel searchModel,
			BindingResult result, Map<String, Object> model) {

		if (result.hasErrors() == true) {
						
			model.put("command", searchModel);
			model.put("selectedMenu", "kanjiDictionary");
			
			return "kanjiDictionary";
		}
		
		/*
		// stworzenie obiektu FindWordRequest
		FindWordRequest findWordRequest = createFindWordRequest(searchModel);
		
		logger.info("Wyszukiwanie słowek dla zapytania: " + findWordRequest);
		
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
		*/
		
		model.put("command", searchModel);
		model.put("selectedMenu", "kanjiDictionary");
		
		return "kanjiDictionary";
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

}
