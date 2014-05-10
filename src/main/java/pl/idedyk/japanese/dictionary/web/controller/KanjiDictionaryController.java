package pl.idedyk.japanese.dictionary.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
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

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiResult;
import pl.idedyk.japanese.dictionary.api.dto.KanjiDic2Entry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerResultItem;
import pl.idedyk.japanese.dictionary.api.dto.RadicalInfo;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionaryTab;
import pl.idedyk.japanese.dictionary.web.controller.validator.KanjiDictionarySearchModelValidator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.dictionary.ZinniaManager;

@Controller
public class KanjiDictionaryController extends CommonController {

	private static final Logger logger = Logger.getLogger(KanjiDictionaryController.class);

	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Autowired
	private ZinniaManager zinniaManager;
	
	@Autowired  
	private KanjiDictionarySearchModelValidator kanjiDictionarySearchModelValidator;
	
	@Autowired
	private MessageSource messageSource;
	
	@InitBinder(value = { "command" })
	private void initBinder(WebDataBinder binder) {  
		binder.setValidator(kanjiDictionarySearchModelValidator);  
	}

	@RequestMapping(value = "/kanjiDictionary", method = RequestMethod.GET)
	public String start(Map<String, Object> model, HttpSession session) {
						
		// utworzenie model szukania
		KanjiDictionarySearchModel kanjiDictionarySearchModel = new KanjiDictionarySearchModel();

		// ustawienie domyslnych wartosci model szukania
		kanjiDictionarySearchModel.setWordPlace(WordPlaceSearch.START_WITH.toString());
		
		// pobierz elementy podstawowe
		List<RadicalInfo> radicalList = dictionaryManager.getRadicalList();

		model.put("command", kanjiDictionarySearchModel);
		model.put("radicalList", radicalList);
		model.put("selectedMenu", "kanjiDictionary");
		model.put("tabs", KanjiDictionaryTab.values());
		model.put("selectTab", KanjiDictionaryTab.MEANING.getId());
				
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
			model.put("tabs", KanjiDictionaryTab.values());
			model.put("selectTab", KanjiDictionaryTab.MEANING.getId());
			
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
		model.put("tabs", KanjiDictionaryTab.values());
		model.put("selectTab", KanjiDictionaryTab.MEANING.getId());
		
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
	public @ResponseBody String showAvailableRadicals(@RequestParam(value="selectedRadicals[]", required=false) String[] selectedRadicals,
			HttpSession session) {

		if (selectedRadicals == null) {
			selectedRadicals = new String[] { };
		}
		
		logger.info("Pokaż dostępne elementy podstawowe dla zapytania: " + Arrays.toString(selectedRadicals));

		Set<String> allAvailableRadicals = dictionaryManager.findAllAvailableRadicals(selectedRadicals);
		
		List<KanjiEntry> findKnownKanjiFromRadicalsResult = dictionaryManager.findKnownKanjiFromRadicals(selectedRadicals);
				
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("allAvailableRadicals", allAvailableRadicals);
				
		JSONArray kanjiFromRadicalsJSON = new JSONArray();
		
		Collections.sort(findKnownKanjiFromRadicalsResult, new Comparator<KanjiEntry>() {

			@Override
			public int compare(KanjiEntry k1, KanjiEntry k2) {
				
				KanjiDic2Entry k1Dic2Entry = k1.getKanjiDic2Entry();
				KanjiDic2Entry k2Dic2Entry = k2.getKanjiDic2Entry();
				
				if (k1Dic2Entry == null) {
					return -1;
				}

				if (k2Dic2Entry == null) {
					return 1;
				}
				
				return k1Dic2Entry.getStrokeCount() < k2Dic2Entry.getStrokeCount() ? -1 : k1Dic2Entry.getStrokeCount() > k2Dic2Entry.getStrokeCount() ? 1 : 0;
			}
		});
		
		if (findKnownKanjiFromRadicalsResult != null) {
			
			for (KanjiEntry currentKanjiEntry : findKnownKanjiFromRadicalsResult) {
				
				JSONObject currentKanjiFromRadicalJSON = new JSONObject();
				
				currentKanjiFromRadicalJSON.put("id", currentKanjiEntry.getId());
				currentKanjiFromRadicalJSON.put("kanji", currentKanjiEntry.getKanji());
				currentKanjiFromRadicalJSON.put("strokeCount", currentKanjiEntry.getKanjiDic2Entry() != null ? currentKanjiEntry.getKanjiDic2Entry().getStrokeCount() : 0);
				
				kanjiFromRadicalsJSON.put(currentKanjiFromRadicalJSON);		
			}
		}		
		
		jsonObject.put("kanjiFromRadicals", kanjiFromRadicalsJSON);
		
		session.setAttribute("selectedRadicals", selectedRadicals);
				
		return jsonObject.toString();
	}
	
	@RequestMapping(value = "/kanjiDictionaryDetails/{id}/{kanji}", method = RequestMethod.GET)
	public String showKanjiDictionaryDetails(@PathVariable("id") int id, @PathVariable("kanji") String kanji, Map<String, Object> model) {
		
		// pobranie kanji entry
		KanjiEntry kanjiEntry = dictionaryManager.findKanji(kanji);
				
		int fixme = 1;		
		// zrobic powrot
		// logowanie
		
		// tytul strony
		if (kanjiEntry != null) {
			
			logger.info("Znaleziono kanji dla zapytania o szczegóły kanji: " + kanjiEntry);
			
			String kanjiEntryKanji = kanjiEntry.getKanji();
									
			String pageTitle = messageSource.getMessage("kanjiDictionaryDetails.page.title", 
					new Object[] { kanjiEntryKanji }, Locale.getDefault());
			
			model.put("pageTitle", pageTitle);
			
		} else {
			
			logger.info("Nie znaleziono kanji dla zapytania o szczegóły kanji: " + id + " / " + kanji);
			
			String pageTitle = messageSource.getMessage("kanjiDictionaryDetails.page.title", 
					new Object[] { "-", "-", "-" }, Locale.getDefault());
			
			model.put("pageTitle", pageTitle);
		}
						
		model.put("kanjiEntry", kanjiEntry);
		model.put("selectedMenu", "kanjiDictionary");
		model.put("tabs", KanjiDictionaryTab.values());
		model.put("selectTab", KanjiDictionaryTab.MEANING.getId());
		
		return "kanjiDictionaryDetails";
	}
	
	@RequestMapping(produces = "application/json;charset=UTF-8", 
			value = "/kanjiDictionary/detect", method = RequestMethod.POST)
	public @ResponseBody String detect(@RequestParam(value="strokePaths[]", required=false) String[] strokePaths, HttpSession session) throws Exception {
		
		JSONObject jsonObject = new JSONObject();
		
		if (strokePaths != null) {

			StringBuffer strokePathsAll = new StringBuffer();

			for (String currentStrokePath : strokePaths) {
				strokePathsAll.append(currentStrokePath).append("\n");
			}

			logger.info("Rozpoznawanie znakow kanji dla: " + strokePathsAll.toString());

			final int maxX = 500;
			final int maxY = 500;

			ZinniaManager.Character character = null;

			try {
				character = zinniaManager.createNewCharacter();

				character.setWidth(maxX);
				character.setHeight(maxY);

				for (int strokePathNo = 0; strokePathNo < strokePaths.length; ++strokePathNo) {
					String currentStrokePath = strokePaths[strokePathNo];

					if (currentStrokePath == null) {
						throw new Exception("Pusta aktualna sciezka");
					}

					if (currentStrokePath.equals("") == true) {
						break;
					}

					String[] points = currentStrokePath.split(";");

					if (points == null || points.length == 0) {
						throw new Exception("Puste punkty");
					}

					for (String currentPoint : points) {

						if (currentPoint == null) {
							throw new Exception("Pusty aktualny punkt");
						}

						String[] currentPointSplited = currentPoint.split(",");

						if (currentPointSplited == null || currentPointSplited.length != 2) {
							throw new Exception("Niepoprawny aktualny punkt");
						}
																		
						int pointIdx = currentPointSplited[0].indexOf(".");
						
						if (pointIdx != -1) {
							currentPointSplited[0] = currentPointSplited[0].substring(0, pointIdx);
						}

						pointIdx = currentPointSplited[1].indexOf(".");
						
						if (pointIdx != -1) {
							currentPointSplited[1] = currentPointSplited[1].substring(0, pointIdx);
						}
						
						try {				
							Integer currentPointX = Integer.parseInt(currentPointSplited[0]);

							if (currentPointX < 0 || currentPointX > maxX) {
								throw new Exception("Niepoprawny aktualny punkt X: " + currentPointSplited[0]);
							}

							Integer currentPointY = Integer.parseInt(currentPointSplited[1]);

							if (currentPointY < 0 || currentPointY > maxY) {
								throw new Exception("Niepoprawny aktualny punkt Y: " + currentPointSplited[1]);
							}

							character.add(strokePathNo, currentPointX, currentPointY);

						} catch (NumberFormatException e) {
							throw new Exception("Niepoprawny aktualny punkt: " + currentPointSplited[0] + " - " + currentPointSplited[1]);
						}				
					}			
				}

				List<KanjiRecognizerResultItem> recognizeResult = character.recognize(50);
				
				session.setAttribute("findKanjiRecognizerResultItem", recognizeResult);
				
				jsonObject.put("result", "ok");
				
			} finally {
				if (character != null) {
					character.destroy();
				}
			}
		} 
		
		return jsonObject.toString();
	}
	
	@RequestMapping(value = "/kanjiDictionaryDetectSearch", method = RequestMethod.GET)
	public String detectSearchResult(HttpSession session, Map<String, Object> model) {
		
		@SuppressWarnings("unchecked")
		List<KanjiRecognizerResultItem> recognizeResult = (List<KanjiRecognizerResultItem>)session.getAttribute("findKanjiRecognizerResultItem");
		
		if (recognizeResult == null) {
			return "redirect:/kanjiDictionary";
		}
				
		logger.info("Generowanie listy rozpoznanych znakow kanji");
		
		// utworzenie model szukania
		KanjiDictionarySearchModel kanjiDictionarySearchModel = new KanjiDictionarySearchModel();

		// ustawienie domyslnych wartosci model szukania
		kanjiDictionarySearchModel.setWordPlace(WordPlaceSearch.START_WITH.toString());
		
		// pobierz elementy podstawowe
		List<RadicalInfo> radicalList = dictionaryManager.getRadicalList();

		model.put("command", kanjiDictionarySearchModel);
		model.put("radicalList", radicalList);
		model.put("selectedMenu", "kanjiDictionary");
		model.put("tabs", KanjiDictionaryTab.values());
		model.put("selectTab", KanjiDictionaryTab.DETECT.getId());
		
		// uzupelnienie wynikowej listy
		FindKanjiResult findKanjiDetectResult = new FindKanjiResult();
		findKanjiDetectResult.setResult(new ArrayList<KanjiEntry>());
		
		for (KanjiRecognizerResultItem kanjiRecognizerResultItem : recognizeResult) {
			findKanjiDetectResult.getResult().add(dictionaryManager.findKanji(kanjiRecognizerResultItem.getKanji()));
		}		

		model.put("findKanjiDetectResult", findKanjiDetectResult);
				
		return "kanjiDictionary";
	}
}
