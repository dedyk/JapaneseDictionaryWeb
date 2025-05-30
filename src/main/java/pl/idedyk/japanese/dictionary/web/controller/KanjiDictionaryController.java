package pl.idedyk.japanese.dictionary.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiResult;
import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerResultItem;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.lucene.LuceneDatabaseSuggesterAndSpellCheckerSource;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionaryDrawStroke;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.controller.model.KanjiDictionaryTab;
import pl.idedyk.japanese.dictionary.web.controller.validator.KanjiDictionarySearchModelValidator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.dictionary.ZinniaManager;
import pl.idedyk.japanese.dictionary.web.dictionary.dto.WebRadicalInfo;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryCatalogLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetectLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryRadicalsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryStartLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.logger.model.PageNoFoundExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RedirectLoggerModel;
import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

@Controller
public class KanjiDictionaryController {

	private static final Logger logger = LogManager.getLogger(KanjiDictionaryController.class);

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
	
	@Autowired
	private LoggerSender loggerSender;

	@RequestMapping(value = "/kanjiDictionary", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {
						
		// utworzenie model szukania
		KanjiDictionarySearchModel kanjiDictionarySearchModel = new KanjiDictionarySearchModel();

		// ustawienie domyslnych wartosci model szukania
		kanjiDictionarySearchModel.setWordPlace(WordPlaceSearch.START_WITH.toString());
		
		// pobierz elementy podstawowe
		List<WebRadicalInfo> radicalList = dictionaryManager.getWebRadicalList();

		// logowanie
		logger.info("KanjiDictionaryController: start");
		
		loggerSender.sendLog(new KanjiDictionaryStartLoggerModel(Utils.createLoggerModelCommon(request)));
		
		model.put("command", kanjiDictionarySearchModel);
		model.put("radicalList", radicalList);
		model.put("selectedMenu", "kanjiDictionary");
		model.put("tabs", KanjiDictionaryTab.values());
		model.put("kanjiAutocompleteInitialized", dictionaryManager.isAutocompleteInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource.KANJI_ENTRY_WEB));
		model.put("selectTab", getSelectTabId(session, KanjiDictionaryTab.MEANING));
				
		return "kanjiDictionary";
	}
	
	@RequestMapping(value = "/kanjiDictionary/saveCurrectTab", method = RequestMethod.GET)
	public @ResponseBody String saveCurrectTab(HttpServletRequest request, HttpSession session, @RequestParam(value="tabId", required=true) String tabId) {

		KanjiDictionaryTab[] kanjiDictionaryTabValues = KanjiDictionaryTab.values();
		
		for (KanjiDictionaryTab kanjiDictionaryTab : kanjiDictionaryTabValues) {
			
			if (kanjiDictionaryTab.getId().equals(tabId) == true) {
				session.setAttribute("kanjiDictionarySelectedTab", tabId);
			}
		}
		
		return "ok";
	}
	
	private String getSelectTabId(HttpSession session, KanjiDictionaryTab defaultTab) {
		
		String kanjiDictionarySelectedTab = (String) session.getAttribute("kanjiDictionarySelectedTab");
		
		if (kanjiDictionarySelectedTab != null) {
			return kanjiDictionarySelectedTab;
		}
		
		return defaultTab.getId();
	}
	
	@RequestMapping(value = "/kanjiDictionarySearch", method = RequestMethod.GET)
	public String search(HttpServletRequest request, HttpSession session, @ModelAttribute("command") @Valid KanjiDictionarySearchModel searchModel,
			BindingResult result, Map<String, Object> model) throws DictionaryException {

		// pobierz elementy podstawowe
		List<WebRadicalInfo> radicalList = dictionaryManager.getWebRadicalList();
		
		if (result.hasErrors() == true) {
						
			model.put("command", searchModel);
			model.put("radicalList", radicalList);
			model.put("selectedMenu", "kanjiDictionary");
			model.put("tabs", KanjiDictionaryTab.values());
			model.put("kanjiAutocompleteInitialized", dictionaryManager.isAutocompleteInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource.KANJI_ENTRY_WEB));
			model.put("selectTab", getSelectTabId(session, KanjiDictionaryTab.MEANING));
			
			return "kanjiDictionary";
		}
		
		// stworzenie obiektu FindKanjiRequest
		FindKanjiRequest findKanjiRequest = createFindKanjiRequest(searchModel);

		logger.info("Wyszukiwanie kanji dla zapytania: " + findKanjiRequest);

		// szukanie
		FindKanjiResult findKanjiResult = dictionaryManager.findKanji(findKanjiRequest);
		
		// jesli nic nie znaleziono, proba pobrania znakow z napisu
		if (findKanjiResult.getResult().size() == 0 && findKanjiRequest.word != null && findKanjiRequest.word.trim().equals("") == false) {
			
			List<KanjiCharacterInfo> findKnownKanjiResult = dictionaryManager.findKnownKanji(findKanjiRequest.word);
			
			if (findKnownKanjiResult.size() > 0) { // gdy cos znajdziemy
				
				List<KanjiCharacterInfo> filteredFindKnownKanjiResult = new ArrayList<KanjiCharacterInfo>();
				
				for (KanjiCharacterInfo currentKanjiEntry : findKnownKanjiResult) {
					
					if (findKanjiRequest.strokeCountFrom == null && findKanjiRequest.strokeCountTo == null) {
						filteredFindKnownKanjiResult.add(currentKanjiEntry);
						
						continue;
					}
					
					List<Integer> strokeCountList = currentKanjiEntry.getMisc().getStrokeCountList();
										
					if (strokeCountList == null || strokeCountList.size() == 0) {
						continue;
					}
					
					int currentKanjiStrokeCount = strokeCountList.get(0);
					
					if (findKanjiRequest.strokeCountFrom != null && currentKanjiStrokeCount < findKanjiRequest.strokeCountFrom) {
						continue;
					}
					
					if (findKanjiRequest.strokeCountTo != null && currentKanjiStrokeCount > findKanjiRequest.strokeCountTo) {
						continue;
					}
					
					filteredFindKnownKanjiResult.add(currentKanjiEntry);					
				}
				
				findKanjiResult.setResult(filteredFindKnownKanjiResult);
			}
		}
		
		// logowanie
		loggerSender.sendLog(new KanjiDictionarySearchLoggerModel(Utils.createLoggerModelCommon(request), findKanjiRequest, findKanjiResult));
		
		// jesli nie znaleziono wynikow, nastepuje proba znalezienia podobnych hasel do wyboru przez uzytkownika
		List<String> kanjiDictionaryEntrySpellCheckerSuggestionList = null;
		
		if (findKanjiResult.getResult().isEmpty() == true) {
						
			try {
				
				kanjiDictionaryEntrySpellCheckerSuggestionList = dictionaryManager.getSpellCheckerSuggestion(LuceneDatabaseSuggesterAndSpellCheckerSource.KANJI_ENTRY_WEB, findKanjiRequest.word, 10);
				
			} catch (DictionaryException e) {
				
				// przygotowanie info do logger'a
				GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(
						LoggerModelCommon.createLoggerModelCommon(null, null, null, null, null), -1, e);
				
				// wyslanie do logger'a
				loggerSender.sendLog(generalExceptionLoggerModel);
			}
			
			logger.info("Dla słowa: '" + findKanjiRequest.word + "' znaleziono następujące sugestie: " + kanjiDictionaryEntrySpellCheckerSuggestionList.toString());
		}
		
		// sprawdzanie, czy uruchomic animacje przewijania
		Integer lastKanjiDictionarySearchHash = (Integer)session.getAttribute("lastKanjiDictionarySearchHash");
		
		session.setAttribute("lastKanjiDictionarySearchHash", findKanjiRequest.hashCode());
		
		if (lastKanjiDictionarySearchHash == null) {			
			model.put("runScrollAnim", true);
						
		} else {			
			model.put("runScrollAnim", findKanjiRequest.hashCode() != lastKanjiDictionarySearchHash);
		}
		
		model.put("command", searchModel);
		model.put("radicalList", radicalList);
		model.put("selectedMenu", "kanjiDictionary");
		model.put("tabs", KanjiDictionaryTab.values());
		model.put("kanjiAutocompleteInitialized", dictionaryManager.isAutocompleteInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource.KANJI_ENTRY_WEB));
		model.put("selectTab", getSelectTabId(session, KanjiDictionaryTab.MEANING));
		
		model.put("findKanjiRequest", findKanjiRequest);
		model.put("findKanjiResult", findKanjiResult);
		model.put("doNotShowSocialButtons", Boolean.TRUE);
		
		if (kanjiDictionaryEntrySpellCheckerSuggestionList != null) {
			model.put("kanjiDictionaryEntrySpellCheckerSuggestionList", kanjiDictionaryEntrySpellCheckerSuggestionList);
		}
		
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
		findKanjiRequest.wordPlaceSearch = WordPlaceSearch.valueOf(searchModel.getWordPlace());

		// strokeCountFrom
		String strokeCountFrom = searchModel.getStrokeCountFrom();
		
		if (strokeCountFrom != null && strokeCountFrom.trim().equals("") == false) {
			findKanjiRequest.strokeCountFrom = Integer.parseInt(strokeCountFrom.trim());
		}

		// strokeCountTo
		String strokeCountTo = searchModel.getStrokeCountTo();
		
		if (strokeCountTo != null && strokeCountTo.trim().equals("") == false) {
			findKanjiRequest.strokeCountTo = Integer.parseInt(strokeCountTo.trim());
		}

		return findKanjiRequest;
	}
	
	@RequestMapping(produces = "application/json;charset=UTF-8", 
			value = "/kanjiDictionary/autocomplete", method = RequestMethod.GET)
	public @ResponseBody String autocomplete(HttpServletRequest request, HttpSession session, @RequestParam(value="term", required=true) String term) {

		logger.info("Podpowiadacz słówkowy kanji dla wyrażenia: " + term);
		
		term = term.trim();

		try {
			List<String> kanjiAutocomplete = dictionaryManager.getAutocomplete(LuceneDatabaseSuggesterAndSpellCheckerSource.KANJI_ENTRY_WEB, term, 5);

			// logowanie
			loggerSender.sendLog(new KanjiDictionaryAutocompleteLoggerModel(Utils.createLoggerModelCommon(request), term, kanjiAutocomplete.size()));
			
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
	public @ResponseBody String showAvailableRadicals(HttpServletRequest request, HttpSession session, @RequestParam(value="selectedRadicals[]", required=false) String[] selectedRadicals) throws DictionaryException{

		if (selectedRadicals == null) {
			selectedRadicals = new String[] { };
		}
		
		logger.info("Pokaż dostępne elementy podstawowe dla zapytania: " + Arrays.toString(selectedRadicals));

		Set<String> allAvailableRadicals = dictionaryManager.findAllAvailableRadicals(selectedRadicals);
		
		List<KanjiCharacterInfo> findKnownKanjiFromRadicalsResult = dictionaryManager.findKnownKanjiFromRadicals(selectedRadicals);
		
		// logowanie
		if (selectedRadicals.length > 0) {
			loggerSender.sendLog(new KanjiDictionaryRadicalsLoggerModel(Utils.createLoggerModelCommon(request), selectedRadicals, findKnownKanjiFromRadicalsResult.size()));
		}
		
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("allAvailableRadicals", allAvailableRadicals);
				
		JSONArray kanjiFromRadicalsJSON = new JSONArray();
		
		Collections.sort(findKnownKanjiFromRadicalsResult, new Comparator<KanjiCharacterInfo>() {

			@Override
			public int compare(KanjiCharacterInfo k1, KanjiCharacterInfo k2) {
				
				List<Integer> k1StrokeCountList = k1.getMisc().getStrokeCountList();
				List<Integer> k2StrokeCountList = k2.getMisc().getStrokeCountList();
								
				if (k1StrokeCountList == null) {
					return -1;
				}

				if (k2StrokeCountList == null) {
					return 1;
				}
				
				Integer k1StrokeCount =  k1StrokeCountList.get(0);
				Integer k2StrokeCount =  k2StrokeCountList.get(0);				
				
				return k1StrokeCount < k2StrokeCount ? -1 : k1StrokeCount > k2StrokeCount ? 1 : 0;
			}
		});
		
		if (findKnownKanjiFromRadicalsResult != null) {
			
			for (KanjiCharacterInfo currentKanjiEntry : findKnownKanjiFromRadicalsResult) {
				
				JSONObject currentKanjiFromRadicalJSON = new JSONObject();
				
				currentKanjiFromRadicalJSON.put("id", currentKanjiEntry.getId());
				currentKanjiFromRadicalJSON.put("kanji", currentKanjiEntry.getKanji());
				currentKanjiFromRadicalJSON.put("strokeCount", currentKanjiEntry.getMisc().getStrokeCountList() != null ? currentKanjiEntry.getMisc().getStrokeCountList().get(0) : 0);
				
				kanjiFromRadicalsJSON.put(currentKanjiFromRadicalJSON);		
			}
		}		
		
		jsonObject.put("kanjiFromRadicals", kanjiFromRadicalsJSON);
		
		session.setAttribute("selectedRadicals", selectedRadicals);
				
		return jsonObject.toString();
	}
	
	@RequestMapping(value = "/kanjiDictionaryDetails/{id}/{kanji}", method = RequestMethod.GET)
	public String showKanjiDictionaryDetails(HttpServletRequest request, HttpSession session, @PathVariable("id") int id, @PathVariable("kanji") String kanji, Map<String, Object> model) throws DictionaryException {
		
		// pobranie kanji entry
		KanjiCharacterInfo kanjiEntry = dictionaryManager.findKanji(kanji);
						
		// tytul strony
		if (kanjiEntry != null) {
			
			//logger.info("Znaleziono kanji dla zapytania o szczegóły kanji: " + kanjiEntry);
			
			// logowanie
			loggerSender.sendLog(new KanjiDictionaryDetailsLoggerModel(Utils.createLoggerModelCommon(request), kanjiEntry));
			
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
		model.put("selectTab", getSelectTabId(session, KanjiDictionaryTab.MEANING));
		
		return "kanjiDictionaryDetails";
	}
	
	@RequestMapping(value = "/kanjiDictionaryDetails/{id}", method = RequestMethod.GET)
	public void showKanjiDictionaryDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("id") int id) throws IOException, DictionaryException {

		// pobranie znaku
		KanjiCharacterInfo kanjiEntry = dictionaryManager.getKanjiEntryById(id);

		if (kanjiEntry != null) {
			
			String destinationUrl = LinkGenerator.generateKanjiDetailsLink(request.getContextPath(), kanjiEntry);
			
			RedirectLoggerModel redirectLoggerModel = new RedirectLoggerModel(Utils.createLoggerModelCommon(request), destinationUrl);
			
			loggerSender.sendLog(redirectLoggerModel);	
			
			response.sendRedirect(destinationUrl);
			
		} else {			
			response.sendError(404);
			
			PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
			
			loggerSender.sendLog(pageNoFoundExceptionLoggerModel);	
		}		
	}
		
	@RequestMapping(value = "/kanjiDictionaryDetectSearch", method = RequestMethod.POST)
	public String detectSearchResult(HttpServletRequest request, HttpSession session, Map<String, Object> model,
			@RequestParam(value="strokes", required=false) String strokes,
			@RequestParam(value="width", required=false) Integer width,
			@RequestParam(value="height", required=false) Integer height) throws DictionaryException {
		
		logger.info("Rozpoznawanie znakow kanji dla: " + strokes);
		
		List<KanjiRecognizerResultItem> detectKanjiResult = null;
		String errorMessage = null;
		
		KanjiDictionaryDrawStroke kanjiDictionaryDrawStroke = new KanjiDictionaryDrawStroke();
		
		try {
			detectKanjiResult = detectKanji(strokes, width, height, kanjiDictionaryDrawStroke);
			
		} catch (Exception e) {
			errorMessage = e.getMessage();
			
			logger.error("Bład podczas rozpoznawania znaków kanji: " + errorMessage);
		}
				
		// utworzenie model szukania
		KanjiDictionarySearchModel kanjiDictionarySearchModel = new KanjiDictionarySearchModel();

		// ustawienie domyslnych wartosci model szukania
		kanjiDictionarySearchModel.setWordPlace(WordPlaceSearch.START_WITH.toString());
		
		// pobierz elementy podstawowe
		List<WebRadicalInfo> radicalList = dictionaryManager.getWebRadicalList();

		// uzupelnienie wynikowej listy
		FindKanjiResult findKanjiDetectResult = null;
		
		if (detectKanjiResult != null) {
			StringBuffer detectKanjiResultSb = new StringBuffer();
			
			for (int idx = 0; idx < 10 && idx < detectKanjiResult.size(); ++idx) {
				
				KanjiRecognizerResultItem currentKanjiRecognizerResultItem = detectKanjiResult.get(idx);
				
				detectKanjiResultSb.append(currentKanjiRecognizerResultItem.getKanji() + " " + currentKanjiRecognizerResultItem.getScore());
				
				if (idx != 10 - 1) {
					detectKanjiResultSb.append("\n");
				}
			}
			
			logger.info("Rozpoznano znaki kanji:\n\n" + detectKanjiResultSb.toString());
			
			// logowanie
			loggerSender.sendLog(new KanjiDictionaryDetectLoggerModel(Utils.createLoggerModelCommon(request), strokes, detectKanjiResult));
			
			findKanjiDetectResult = new FindKanjiResult();
			findKanjiDetectResult.setResult(new ArrayList<KanjiCharacterInfo>());
			
			for (KanjiRecognizerResultItem kanjiRecognizerResultItem : detectKanjiResult) {
				findKanjiDetectResult.getResult().add(dictionaryManager.findKanji(kanjiRecognizerResultItem.getKanji()));
			}	
			
		} else {
			logger.info("Nie rozpoznano żadnych znaków kanji");
			
			kanjiDictionaryDrawStroke = null;
		}
		
		// sprawdzanie, czy uruchomic animacje przewijania
		Integer lastKanjiDetectSearchResultHash = (Integer)session.getAttribute("lastKanjiDetectSearchResultHash");
		
		session.setAttribute("lastKanjiDetectSearchResultHash", strokes.hashCode());
		
		if (lastKanjiDetectSearchResultHash == null) {			
			model.put("runScrollAnim", true);
						
		} else {			
			model.put("runScrollAnim", strokes.hashCode() != lastKanjiDetectSearchResultHash);
		}

		model.put("command", kanjiDictionarySearchModel);
		model.put("radicalList", radicalList);
		model.put("selectedMenu", "kanjiDictionary");
		model.put("tabs", KanjiDictionaryTab.values());
		model.put("kanjiAutocompleteInitialized", dictionaryManager.isAutocompleteInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource.KANJI_ENTRY_WEB));
		model.put("selectTab", getSelectTabId(session, KanjiDictionaryTab.DETECT));
		
		model.put("kanjiDictionaryDetectErrorMessage", errorMessage);
		model.put("findKanjiDetectResult", findKanjiDetectResult);
		model.put("kanjiDictionaryDrawStroke", kanjiDictionaryDrawStroke);
		
		model.put("doNotShowSocialButtons", Boolean.TRUE);
				
		return "kanjiDictionary";
	}
	
	private List<KanjiRecognizerResultItem> detectKanji(String strokes, Integer width, Integer height, KanjiDictionaryDrawStroke kanjiDictionaryDrawStroke) throws Exception {
		
		if (strokes == null) {
			throw new Exception(messageSource.getMessage("kanjiDictionaryDetails.page.title", new Object[] { }, Locale.getDefault()));
		}
		
		String[] strokesSplited = strokes.split("\n");
		
		if (strokesSplited.length == 0) {
			throw new Exception(messageSource.getMessage("kanjiDictionaryDetails.page.title", new Object[] { }, Locale.getDefault()));
		}
		
		final int maxX = width;
		final int maxY = height;

		ZinniaManager.Character character = null;

		try {
			character = zinniaManager.createNewCharacter();

			character.setWidth(maxX);
			character.setHeight(maxY);

			for (int strokePathNo = 0; strokePathNo < strokesSplited.length; ++strokePathNo) {
				
				kanjiDictionaryDrawStroke.newStroke();
				
				String currentStrokePath = strokesSplited[strokePathNo];

				if (currentStrokePath == null) {
					throw new Exception(messageSource.getMessage("kanjiDictionary.page.tab.detect.problem", new Object[] { }, Locale.getDefault()));
				}

				if (currentStrokePath.equals("") == true) {
					break;
				}

				String[] points = currentStrokePath.split(";");

				if (points == null || points.length == 0) {
					throw new Exception(messageSource.getMessage("kanjiDictionary.page.tab.detect.problem", new Object[] { }, Locale.getDefault()));
				}

				for (String currentPoint : points) {

					if (currentPoint == null) {
						throw new Exception(messageSource.getMessage("kanjiDictionary.page.tab.detect.problem", new Object[] { }, Locale.getDefault()));
					}
					
					currentPoint = currentPoint.trim();
					
					if (currentPoint.equals("") == true) {
						continue;
					}

					String[] currentPointSplited = currentPoint.split(",");

					if (currentPointSplited == null || currentPointSplited.length != 2) {
						throw new Exception(messageSource.getMessage("kanjiDictionary.page.tab.detect.problem", new Object[] { }, Locale.getDefault()));
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
							throw new Exception(messageSource.getMessage("kanjiDictionary.page.tab.detect.problem", new Object[] { }, Locale.getDefault()));
						}

						Integer currentPointY = Integer.parseInt(currentPointSplited[1]);

						if (currentPointY < 0 || currentPointY > maxY) {
							throw new Exception(messageSource.getMessage("kanjiDictionary.page.tab.detect.problem", new Object[] { }, Locale.getDefault()));
						}

						character.add(strokePathNo, currentPointX, currentPointY);
						
						kanjiDictionaryDrawStroke.addPoint(currentPointX, currentPointY);

					} catch (NumberFormatException e) {
						throw new Exception(messageSource.getMessage("kanjiDictionary.page.tab.detect.problem", new Object[] { }, Locale.getDefault()));
					}				
				}			
			}

			return character.recognize(100);
						
		} finally {
			if (character != null) {
				character.destroy();
			}
		}		
	}
	
	@RequestMapping(value = "/kanjiDictionaryCatalog/{page}", method = RequestMethod.GET)
	public String kanjiDictionaryCatalog(HttpServletRequest request, HttpSession session, 
			@PathVariable("page") int pageNo,
			Map<String, Object> model) throws DictionaryException {
		
		final int pageSize = 50;  // zmiana tego parametru wiaze sie ze zmiana w SitemapManager
		
		if (pageNo < 1) {
			pageNo = 1;
		}
				
		logger.info("Wyświetlanie katalogu znakow kanji dla strony: " + pageNo);
		
		// szukanie	
		List<KanjiCharacterInfo> allKanjis = dictionaryManager.getAllKanjis(false, false);
		
		List<KanjiCharacterInfo> resultList = new ArrayList<KanjiCharacterInfo>(); 
		
		for (int kanjiIdx = (pageNo - 1) * pageSize; kanjiIdx < allKanjis.size() && kanjiIdx < ((pageNo) * pageSize); ++kanjiIdx) {
			resultList.add(allKanjis.get(kanjiIdx));
		}
				
		// logowanie
		loggerSender.sendLog(new KanjiDictionaryCatalogLoggerModel(Utils.createLoggerModelCommon(request), pageNo));

		// stworzenie zaslepkowego findWordRequest i findWordResult
		FindKanjiRequest findKanjiRequest = new FindKanjiRequest();
		
		FindKanjiResult findKanjiResult = new FindKanjiResult();
		
		findKanjiResult.setResult(resultList);
		
		if (allKanjis.size() > pageNo * pageSize) {
			findKanjiResult.setMoreElemetsExists(true);
			
		} else {
			findKanjiResult.setMoreElemetsExists(false);
		}
		
		int lastPageNo = (allKanjis.size() / pageSize) + (allKanjis.size() % pageSize > 0 ? 1 : 0);
						
		model.put("selectedMenu", "kanjiDictionary");		
		model.put("findKanjiRequest", findKanjiRequest);
		model.put("findKanjiResult", findKanjiResult);
		model.put("pageNo", pageNo);
		model.put("lastPageNo", lastPageNo);
		model.put("metaRobots", "noindex, follow");
		
		String pageTitle = messageSource.getMessage("kanjiDictionary.catalog.page.title", 
				new Object[] { String.valueOf((pageNo - 1) * pageSize + 1), String.valueOf(((pageNo - 1) * pageSize + 1) + resultList.size() - 1) }, Locale.getDefault());
		
		String pageDescription = messageSource.getMessage("kanjiDictionary.catalog.page.pageDescription", 
				new Object[] { String.valueOf((pageNo - 1) * pageSize + 1), String.valueOf(((pageNo - 1) * pageSize + 1) + resultList.size() - 1) }, Locale.getDefault());
		
		model.put("pageTitle", pageTitle);
		model.put("pageDescription", pageDescription);
				
		return "kanjiDictionaryCatalog";
	}
}
