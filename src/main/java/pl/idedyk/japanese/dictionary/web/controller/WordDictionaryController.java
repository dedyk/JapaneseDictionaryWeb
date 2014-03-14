package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Arrays;
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

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
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

	@InitBinder 
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

	@RequestMapping(value = "/wordDictionary/search", method = RequestMethod.POST)
	public String search(@ModelAttribute("command") @Valid WordDictionarySearchModel searchModel,
			BindingResult result, Map<String, Object> model) {

		if (result.hasErrors() == true) {
			
			System.out.println("ZZZZZZZ");
			
			model.put("addableDictionaryEntryList", DictionaryEntryType.getAddableDictionaryEntryList());
			model.put("command", searchModel);
			model.put("selectedMenu", "wordDictionary");
			
			return "wordDictionary";
		}
		
		int fixme = 1;
		// szukanie

		List<String> dictionaryTypeStringList = searchModel.getDictionaryTypeStringList();

		for (String string : dictionaryTypeStringList) {
			//System.out.println("AAAA: " + string);
		}

		// usuwanie przecinkow ze slow
		// polskie znaki

		// walidator

		FindWordRequest findWordRequest = new FindWordRequest();

		//findWordRequest.

		model.put("addableDictionaryEntryList", DictionaryEntryType.getAddableDictionaryEntryList());
		model.put("command", searchModel);
		model.put("selectedMenu", "wordDictionary");

		return "wordDictionary";
	}

	@RequestMapping(value = "/wordDictionary/search", method = RequestMethod.GET)
	public String searchRedirect(@ModelAttribute("wordDictionarySearchModel") WordDictionarySearchModel searchModel,
			Map<String, Object> model) {

		return "redirect:";
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
}
