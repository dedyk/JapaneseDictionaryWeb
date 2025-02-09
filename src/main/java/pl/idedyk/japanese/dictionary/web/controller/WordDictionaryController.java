package pl.idedyk.japanese.dictionary.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.lucene.LuceneDatabaseSuggesterAndSpellCheckerSource;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.controller.validator.WordDictionarySearchModelValidator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.logger.model.PageNoFoundExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RedirectLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryCatalogLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryNameCatalogLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryNameDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryPdfDictionaryLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryStartLoggerModel;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;

@Controller
public class WordDictionaryController {

	private static final Logger logger = LogManager.getLogger(WordDictionaryController.class);

	@Autowired
	private DictionaryManager dictionaryManager;

	@Autowired  
	private WordDictionarySearchModelValidator wordDictionarySearchModelValidator;

	@Autowired
	private MessageSource messageSource;
	
	@InitBinder(value = { "command" })
	private void initBinder(WebDataBinder binder) {  
		binder.setValidator(wordDictionarySearchModelValidator);  
	}
	
	@Autowired
	private LoggerSender loggerSender;

	@RequestMapping(value = "/wordDictionary", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {
		
		// utworzenie model szukania
		WordDictionarySearchModel wordDictionarySearchModel = new WordDictionarySearchModel();

		// ustawienie domyslnych wartosci model szukania
		wordDictionarySearchModel.setWordPlace(WordPlaceSearch.START_WITH.toString());

		// ustawienie miejsca szukania		
		wordDictionarySearchModel.setSearchIn(Arrays.asList("GRAMMA_FORM_AND_EXAMPLES", "NAMES", "KANJI", "KANA", "ROMAJI", "TRANSLATE", "INFO"));

		// pobranie wyswietlanych typow
		List<DictionaryEntryType> addableDictionaryEntryList = DictionaryEntryType.getAddableDictionaryEntryList();

		for (DictionaryEntryType dictionaryEntryType : addableDictionaryEntryList) {
			wordDictionarySearchModel.addDictionaryType(dictionaryEntryType);
		}
		
		// logowanie
		logger.info("WordDictionaryController: start");
		
		loggerSender.sendLog(new WordDictionaryStartLoggerModel(Utils.createLoggerModelCommon(request)));

		model.put("addableDictionaryEntryList", addableDictionaryEntryList);
		model.put("command", wordDictionarySearchModel);
		model.put("wordAutocompleteInitialized", dictionaryManager.isAutocompleteInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource.DICTIONARY_ENTRY_WEB));
		model.put("selectedMenu", "wordDictionary");
		
		return "wordDictionary";
	}

	@RequestMapping(value = "/wordDictionarySearch", method = RequestMethod.GET)
	public String search(HttpServletRequest request, HttpSession session, @ModelAttribute("command") @Valid final WordDictionarySearchModel searchModel,
			BindingResult result, Map<String, Object> model) throws DictionaryException {
		
		// gdy cos bedzie zmieniane trzeba rowniez zmienic w link generatorze

		if (result.hasErrors() == true) {
						
			model.put("addableDictionaryEntryList", DictionaryEntryType.getAddableDictionaryEntryList());
			model.put("command", searchModel);
			model.put("wordAutocompleteInitialized", dictionaryManager.isAutocompleteInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource.DICTIONARY_ENTRY_WEB));
			model.put("selectedMenu", "wordDictionary");
			
			return "wordDictionary";
		}
		
		// stworzenie obiektu FindWordRequest
		FindWordRequest findWordRequest = createFindWordRequest(searchModel);
		
		logger.info("Wyszukiwanie słowek dla zapytania: " + findWordRequest);
		
		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findWord(findWordRequest);
		
		final String remoteUser = request.getRemoteUser();
		
		// logowanie
		loggerSender.sendLog(new WordDictionarySearchLoggerModel(Utils.createLoggerModelCommon(request), findWordRequest, findWordResult, remoteUser == null ? 1 : 2));

		// logowanie wyszukiwania, dodatkowe sprawdzenie przez system, aby zalogowac ewentualne brakujace slowa
		// uruchom w osobnym watku
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {				
					FindWordRequest findWordRequestForSystemLog = Utils.createFindWordRequestForSystemLog(searchModel.getWord(), WordPlaceSearch.valueOf(searchModel.getWordPlace()));
					
					FindWordResult findWordResultForSystemLog = dictionaryManager.findWord(findWordRequestForSystemLog);
				
					loggerSender.sendLog(new WordDictionarySearchLoggerModel(null, findWordRequestForSystemLog, findWordResultForSystemLog, remoteUser == null ? 1 : 2));
					
				} catch (DictionaryException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
		
		// sprawdzanie, czy uruchomic animacje przewijania
		Integer lastWordDictionarySearchHash = (Integer)session.getAttribute("lastWordDictionarySearchHash");
		
		session.setAttribute("lastWordDictionarySearchHash", findWordRequest.hashCode());
		
		if (lastWordDictionarySearchHash == null) {
			
			model.put("runScrollAnim", true);
			
		} else {
			
			model.put("runScrollAnim", findWordRequest.hashCode() != lastWordDictionarySearchHash);
		}
		
		// jesli nie znaleziono wynikow, nastepuje proba znalezienia podobnych hasel do wyboru przez uzytkownika
		List<String> wordDictionaryEntrySpellCheckerSuggestionList = null;
		
		if (findWordResult.getResult().isEmpty() == true) {
						
			try {
				
				wordDictionaryEntrySpellCheckerSuggestionList = dictionaryManager.getSpellCheckerSuggestion(LuceneDatabaseSuggesterAndSpellCheckerSource.DICTIONARY_ENTRY_WEB, findWordRequest.word, 10);
				
			} catch (DictionaryException e) {
				
				// przygotowanie info do logger'a
				GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(
						LoggerModelCommon.createLoggerModelCommon(null, null, null, null, null), -1, e);
				
				// wyslanie do logger'a
				loggerSender.sendLog(generalExceptionLoggerModel);
			}
			
			logger.info("Dla słowa: '" + findWordRequest.word + "' znaleziono następujące sugestie: " + wordDictionaryEntrySpellCheckerSuggestionList.toString());
		}
		
		model.put("addableDictionaryEntryList", DictionaryEntryType.getAddableDictionaryEntryList());
		model.put("command", searchModel);
		model.put("wordAutocompleteInitialized", dictionaryManager.isAutocompleteInitialized(LuceneDatabaseSuggesterAndSpellCheckerSource.DICTIONARY_ENTRY_WEB));
		model.put("selectedMenu", "wordDictionary");
		model.put("findWordRequest", findWordRequest);
		model.put("findWordResult", findWordResult);
		model.put("doNotShowSocialButtons", Boolean.TRUE);
		
		if (findWordResult.foundGrammaAndExamples == true) {
			model.put("searchResultInfo", messageSource.getMessage("wordDictionary.page.search.info.foundGrammaAndExamples", 
					new Object[] { }, Locale.getDefault()));
		}
		
		if (wordDictionaryEntrySpellCheckerSuggestionList != null) {
			model.put("wordDictionaryEntrySpellCheckerSuggestionList", wordDictionaryEntrySpellCheckerSuggestionList);
		}
		
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
		findWordRequest.wordPlaceSearch = WordPlaceSearch.valueOf(searchModel.getWordPlace());
		
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
			
			if (Utils.isOnlyCommonWordsSearchIn(currentSearch) == true) {
				findWordRequest.searchOnlyCommonWord = true;
			}
			
			if (Utils.isGrammaFormAndExamples(currentSearch) == true) {
				findWordRequest.searchGrammaFormAndExamples = true;
			}

			if (Utils.isNames(currentSearch) == true) {
				findWordRequest.searchName = true;
			}
		}
						
		// dictionaryEntryList
		List<String> dictionaryTypeStringList = searchModel.getDictionaryTypeStringList();
				
		List<DictionaryEntryType> addableDictionaryEntryList = DictionaryEntryType.getAddableDictionaryEntryList();
		
		if (dictionaryTypeStringList.size() == addableDictionaryEntryList.size()) {			
			findWordRequest.dictionaryEntryTypeList = null;
			
		} else {
			findWordRequest.dictionaryEntryTypeList = new ArrayList<DictionaryEntryType>();
			
			for (String currentDictionaryTypeString : dictionaryTypeStringList) {
				findWordRequest.dictionaryEntryTypeList.add(DictionaryEntryType.valueOf(currentDictionaryTypeString));
			}			
		}		
		
		// searchMainDictionary
		findWordRequest.searchMainDictionary = true;
				
		return findWordRequest;
	}
	
	@RequestMapping(produces = "application/json;charset=UTF-8", 
			value = "/wordDictionary/autocomplete", method = RequestMethod.GET)
	public @ResponseBody String autocomplete(HttpServletRequest request, HttpSession session, @RequestParam(value="term", required=true) String term) {

		logger.info("Podpowiadacz słówkowy dla wyrażenia: " + term);

		term = term.trim();
		
		try {
			List<String> wordAutocomplete = dictionaryManager.getAutocomplete(LuceneDatabaseSuggesterAndSpellCheckerSource.DICTIONARY_ENTRY_WEB, term, 5);

			// logowanie
			loggerSender.sendLog(new WordDictionaryAutocompleteLoggerModel(Utils.createLoggerModelCommon(request), term, wordAutocomplete.size()));
			
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
	public String showWordDictionaryDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("id") int id, @PathVariable("kanji") String kanji,
			@PathVariable("kana") String kana, @RequestParam(value = "forceDictionaryEntryType", required = false) String forceDictionaryEntryType, Map<String, Object> model) throws IOException, DictionaryException {
		
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryById(id);
	
		return showWordDictionaryDetailsCommon(request, response, model, id, kanji, kana, dictionaryEntry, forceDictionaryEntryType, true);
	}
	
	public String showWordDictionaryDetailsCommon(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model, 
			Integer id, String kanji, String kana, DictionaryEntry dictionaryEntry, String forceDictionaryEntryType, 
			boolean checkUniqueKey) throws DictionaryException, IOException {
		
		JMdict.Entry dictionaryEntry2 = null;
		
		DictionaryEntryType forceDictionaryEntryTypeType = null;
		
		if (forceDictionaryEntryType != null) {
			
			try {
				forceDictionaryEntryTypeType = DictionaryEntryType.valueOf(forceDictionaryEntryType);
				
				model.put("forceDictionaryEntryType", forceDictionaryEntryTypeType);
				
			} catch (Exception e) {
				
				logger.info("Niepoprawna wartość parametru 'forceDictionaryEntryType' = " + forceDictionaryEntryType);				
			}
		}
		
		// tytul strony
		if (dictionaryEntry != null) {
			
			// sprawdzenie, czy wystepuje slowo w formacie JMdict
			// pobieramy entry id
			Integer entryId = dictionaryEntry.getJmdictEntryId();
			
			if (entryId != null) {
				// pobieramy z bazy danych
				dictionaryEntry2 = dictionaryManager.getDictionaryEntry2ById(entryId);
			}			
			
			if (forceDictionaryEntryTypeType != null) { // sprawdzamy, czy nie zostal podany zly parametr forceDictionaryEntryTypeType
				
				List<DictionaryEntryType> dictionaryEntryTypeList = dictionaryEntry.getDictionaryEntryTypeList();
				
				if (dictionaryEntryTypeList.contains(forceDictionaryEntryTypeType) == false) {
					
					response.sendError(404);
					
					PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
					
					loggerSender.sendLog(pageNoFoundExceptionLoggerModel);

					return null;
				}
			}
			
			// sprawdzenie, czy nie odwolujemy sie do innej strony
			String dictionaryEntryKanji = "-";
			
			if (dictionaryEntry.isKanjiExists() == true) {
				dictionaryEntryKanji = dictionaryEntry.getKanji();
			}
			
			String dictionaryEntryKana = dictionaryEntry.getKana();
						
			if (	dictionaryEntryKanji.equals(kanji) == false || 
					dictionaryEntryKana.equals(kana) == false ||
					(checkUniqueKey == true && dictionaryEntry.getUniqueKey() != null)) { // przekierowanie na wlasciwa strone o id
				
				String destinationUrl = LinkGenerator.generateDictionaryEntryDetailsLink(request.getContextPath(), dictionaryEntry, null);
				
				RedirectLoggerModel redirectLoggerModel = new RedirectLoggerModel(Utils.createLoggerModelCommon(request), destinationUrl);
				
				loggerSender.sendLog(redirectLoggerModel);	
				
				return "redirect:" + destinationUrl;			
			}			
			
			//logger.info("Znaleziono słówko dla zapytania o szczegóły słowa: " + dictionaryEntry);
			
			// logowanie
			loggerSender.sendLog(new WordDictionaryDetailsLoggerModel(Utils.createLoggerModelCommon(request), dictionaryEntry));

			String[] wordDictionaryDetailsTitleAndDescription = getWordDictionaryDetailsTitleAndDescription(dictionaryEntry, forceDictionaryEntryTypeType);
						
			String pageTitle = wordDictionaryDetailsTitleAndDescription[0];
			String pageDescription = wordDictionaryDetailsTitleAndDescription[1];
			
			model.put("pageTitle", pageTitle);
			model.put("pageDescription", pageDescription);
			
		} else {
			
			logger.info("Nie znaleziono słówka dla zapytania o szczegóły słowa: " + id + " / " + kanji + " / " + kana);
			
			String pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.with.kanji.without.forceDictionaryEntryTypeType",
					new Object[] { "-", "-", "-" }, Locale.getDefault());
			
			model.put("pageTitle", pageTitle);
		}
						
		model.put("dictionaryEntry", dictionaryEntry);
		model.put("dictionaryEntry2", dictionaryEntry2);
		model.put("selectedMenu", "wordDictionary");
		
		return "wordDictionaryDetails";
	}
	
	@RequestMapping(value = "/wordDictionaryDetails2/{kanji}/{kana}/{counter}", method = RequestMethod.GET)
	public String showWordDictionaryDetails2(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("kanji") String kanji, @PathVariable("kana") String kana, @PathVariable("counter") int counter,
			@RequestParam(value = "forceDictionaryEntryType", required = false) String forceDictionaryEntryType, Map<String, Object> model) throws IOException, DictionaryException {
		
		// stworzenie unique key
		String uniqueKey = kanji + "/" + kana + "/" + counter;
		
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryByUniqueKey(uniqueKey);
	
		return showWordDictionaryDetailsCommon(request, response, model, null, kanji, kana, dictionaryEntry, forceDictionaryEntryType, false);
	}
	
	@RequestMapping(value = "/wordDictionaryDetails/{id}", method = RequestMethod.GET)
	public void showWordDictionaryDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("id") int id) throws IOException, DictionaryException {
		
		processWordDictionaryDetailsRedirect(request, response, id);
	}

	@RequestMapping(value = "/wordDictionaryDetails/{id}/{kanji}", method = RequestMethod.GET)
	public void showWordDictionaryDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("id") int id, @PathVariable("kanji") String kanji) throws IOException, DictionaryException {
		
		processWordDictionaryDetailsRedirect(request, response, id);
	}

	private void processWordDictionaryDetailsRedirect(HttpServletRequest request, HttpServletResponse response, int id) throws IOException, DictionaryException {
				
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryById(id);

		if (dictionaryEntry != null) {
			
			String destinationUrl = LinkGenerator.generateDictionaryEntryDetailsLink(request.getContextPath(), dictionaryEntry, null);
			
			RedirectLoggerModel redirectLoggerModel = new RedirectLoggerModel(Utils.createLoggerModelCommon(request), destinationUrl);
			
			loggerSender.sendLog(redirectLoggerModel);	
			
			response.sendRedirect(destinationUrl);
			
		} else {			
			response.sendError(404);
			
			PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
			
			loggerSender.sendLog(pageNoFoundExceptionLoggerModel);	
		}		
	}
	
	@RequestMapping(value = "/wordDictionaryNameDetails/{id}/{kanji}/{kana}", method = RequestMethod.GET)
	public String showWordDictionaryNameDetails(HttpServletRequest request, HttpSession session, @PathVariable("id") int id, @PathVariable("kanji") String kanji,
			@PathVariable("kana") String kana, Map<String, Object> model) throws DictionaryException {
		
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryNameById(id);
		
		return showWordDictionaryNameDetailsCommon(request, model, id, kanji, kana, dictionaryEntry, true);								
	}
	
	private String showWordDictionaryNameDetailsCommon(HttpServletRequest request, Map<String, Object> model, 
			Integer id, String kanji, String kana, DictionaryEntry dictionaryEntry, boolean checkUniqueKey) {
		
		// tytul strony
		if (dictionaryEntry != null) {
			
			// sprawdzenie, czy nie odwolujemy sie do innej strony
			String dictionaryEntryKanji = "-";
			
			if (dictionaryEntry.isKanjiExists() == true) {
				dictionaryEntryKanji = dictionaryEntry.getKanji();
			}
			
			String dictionaryEntryKana = dictionaryEntry.getKana();
			
			if (	dictionaryEntryKanji.equals(kanji) == false || 
					dictionaryEntryKana.equals(kana) == false || 
					(checkUniqueKey == true && dictionaryEntry.getUniqueKey() != null)) { // przekierowanie na wlasciwa strone o id
				
				String destinationUrl = LinkGenerator.generateDictionaryEntryDetailsLink(request.getContextPath(), dictionaryEntry, null);
				
				RedirectLoggerModel redirectLoggerModel = new RedirectLoggerModel(Utils.createLoggerModelCommon(request), destinationUrl);
				
				loggerSender.sendLog(redirectLoggerModel);	
				
				return "redirect:" + destinationUrl;			
			}		
						
			//logger.info("Znaleziono słówko dla zapytania o szczegóły słowa (nazwa): " + dictionaryEntry);
			
			// logowanie
			loggerSender.sendLog(new WordDictionaryNameDetailsLoggerModel(Utils.createLoggerModelCommon(request), dictionaryEntry));

			String[] wordDictionaryDetailsTitleAndDescription = getWordDictionaryDetailsTitleAndDescription(dictionaryEntry, null);
						
			String pageTitle = wordDictionaryDetailsTitleAndDescription[0];
			String pageDescription = wordDictionaryDetailsTitleAndDescription[1];
			
			model.put("pageTitle", pageTitle);
			model.put("pageDescription", pageDescription);
			
		} else {
			
			logger.info("Nie znaleziono słówka dla zapytania o szczegóły słowa (nazwa): " + id + " / " + kanji + " / " + kana);
			
			String pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.with.kanji.without.forceDictionaryEntryTypeType",
					new Object[] { "-", "-", "-" }, Locale.getDefault());
			
			model.put("pageTitle", pageTitle);
		}
						
		model.put("dictionaryEntry", dictionaryEntry);
		model.put("selectedMenu", "wordDictionary");
		
		return "wordDictionaryDetails";
	}
	
	@RequestMapping(value = "/wordDictionaryNameDetails/{id}", method = RequestMethod.GET)
	public void showWordDictionaryNameDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("id") int id) throws IOException, DictionaryException {
		
		processWordDictionaryNameDetailsRedirect(request, response, id);
	}

	@RequestMapping(value = "/wordDictionaryNameDetails/{id}/{kanji}", method = RequestMethod.GET)
	public void showWordDictionaryNameDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("id") int id, @PathVariable("kanji") String kanji) throws IOException, DictionaryException {
		
		processWordDictionaryNameDetailsRedirect(request, response, id);
	}
	
	private void processWordDictionaryNameDetailsRedirect(HttpServletRequest request, HttpServletResponse response, int id) throws IOException, DictionaryException {
		
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryNameById(id);

		if (dictionaryEntry != null) {
			
			String destinationUrl = LinkGenerator.generateDictionaryEntryDetailsLink(request.getContextPath(), dictionaryEntry, null);
			
			RedirectLoggerModel redirectLoggerModel = new RedirectLoggerModel(Utils.createLoggerModelCommon(request), destinationUrl);
			
			loggerSender.sendLog(redirectLoggerModel);	
			
			response.sendRedirect(destinationUrl);
			
		} else {			
			response.sendError(404);
			
			PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
			
			loggerSender.sendLog(pageNoFoundExceptionLoggerModel);	
		}
	}
		
	@RequestMapping(value = "/wordDictionaryNameDetails2/{kanji}/{kana}/{counter}", method = RequestMethod.GET)
	public String showWordDictionaryNameDetails2(HttpServletRequest request, HttpSession session, @PathVariable("kanji") String kanji,
			@PathVariable("kana") String kana, @PathVariable("counter") int counter, Map<String, Object> model) throws DictionaryException {
		
		// stworzenie unique key
		String uniqueKey = kanji + "/" + kana + "/" + counter;
		
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryNameByUniqueKey(uniqueKey);
		
		return showWordDictionaryNameDetailsCommon(request, model, null, kanji, kana, dictionaryEntry, false);
	}
	
	private String[] getWordDictionaryDetailsTitleAndDescription(DictionaryEntry dictionaryEntry, DictionaryEntryType forceDictionaryEntryTypeType) {
		
		String dictionaryEntryKanji = dictionaryEntry.getKanji();
		String dictionaryEntryKana = dictionaryEntry.getKana();
		String dictionaryEntryRomaji = dictionaryEntry.getRomaji();

		boolean withKanji = dictionaryEntryKanji != null ? true : false;
		
		String pageTitle = null;
		String pageDescription = null;
		
		if (forceDictionaryEntryTypeType == null) {
			
			if (withKanji == true) {
				
				pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.with.kanji.without.forceDictionaryEntryTypeType", 
						new Object[] { dictionaryEntryKanji, dictionaryEntryKana, dictionaryEntryRomaji }, Locale.getDefault());
				
				pageDescription = messageSource.getMessage("wordDictionaryDetails.page.pageDescription.with.kanji.without.forceDictionaryEntryTypeType", 
						new Object[] { dictionaryEntryKanji != null ? dictionaryEntryKanji : "-",
								dictionaryEntryKana,
								dictionaryEntryRomaji,
						}, Locale.getDefault());
				
			} else {
				
				pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.without.kanji.without.forceDictionaryEntryTypeType", 
						new Object[] { dictionaryEntryKana, dictionaryEntryRomaji }, Locale.getDefault());
				
				pageDescription = messageSource.getMessage("wordDictionaryDetails.page.pageDescription.without.kanji.without.forceDictionaryEntryTypeType", 
						new Object[] { dictionaryEntryKana, dictionaryEntryRomaji }, Locale.getDefault());			
			}
			
		} else {

			if (withKanji == true) {
				
				pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.with.kanji.with.forceDictionaryEntryTypeType", 
						new Object[] { dictionaryEntryKanji, dictionaryEntryKana, dictionaryEntryRomaji, forceDictionaryEntryTypeType.getName() }, Locale.getDefault());
				
				pageDescription = messageSource.getMessage("wordDictionaryDetails.page.pageDescription.with.kanji.with.forceDictionaryEntryTypeType", 
						new Object[] { dictionaryEntryKanji != null ? dictionaryEntryKanji : "-",
								dictionaryEntryKana,
								dictionaryEntryRomaji, forceDictionaryEntryTypeType.getName()
						}, Locale.getDefault());
				
			} else {
				
				pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.without.kanji.with.forceDictionaryEntryTypeType", 
						new Object[] { dictionaryEntryKana, dictionaryEntryRomaji, forceDictionaryEntryTypeType.getName() }, Locale.getDefault());
				
				pageDescription = messageSource.getMessage("wordDictionaryDetails.page.pageDescription.without.kanji.with.forceDictionaryEntryTypeType", 
						new Object[] { dictionaryEntryKana, dictionaryEntryRomaji, forceDictionaryEntryTypeType.getName() }, Locale.getDefault());			
			}			
		}
		
		
		return new String[] { pageTitle, pageDescription };		
	}
	
	@RequestMapping(value = "/wordDictionaryCatalog/{page}", method = RequestMethod.GET)
	public String wordDictionaryCatalog(HttpServletRequest request, HttpSession session, 
			@PathVariable("page") int pageNo,
			Map<String, Object> model) throws DictionaryException {
		
		final int pageSize = 50;  // zmiana tego parametru wiaze sie ze zmiana w SitemapManager
		
		if (pageNo < 1) {
			pageNo = 1;
		}
				
		logger.info("Wyświetlanie katalogu słów dla strony: " + pageNo);
		
		// szukanie		
		List<DictionaryEntry> dictionaryEntryList = dictionaryManager.getWordsGroup(pageSize, pageNo - 1);
		
		int dictionaryEntriesSize = dictionaryManager.getDictionaryEntriesSize();
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryCatalogLoggerModel(Utils.createLoggerModelCommon(request), pageNo));

		// stworzenie zaslepkowego findWordRequest i findWordResult
		FindWordRequest findWordRequest = new FindWordRequest();
		
		FindWordResult findWordResult = new FindWordResult();
		
		List<FindWordResult.ResultItem> resultItemList = new ArrayList<FindWordResult.ResultItem>();
		
		for (DictionaryEntry dictionaryEntry : dictionaryEntryList) {
			resultItemList.add(new FindWordResult.ResultItem(dictionaryEntry));
		}

		findWordResult.setResult(resultItemList);
		
		if (dictionaryEntriesSize > (pageNo) * pageSize) {
			findWordResult.setMoreElemetsExists(true);
			
		} else {
			findWordResult.setMoreElemetsExists(false);
		}
		
		int lastPageNo = (dictionaryEntriesSize / pageSize) + (dictionaryEntriesSize % pageSize > 0 ? 1 : 0);
				
		model.put("selectedMenu", "wordDictionary");
		model.put("findWordRequest", findWordRequest);
		model.put("findWordResult", findWordResult);
		model.put("pageNo", pageNo);
		model.put("lastPageNo", lastPageNo);
		model.put("metaRobots", "noindex, follow");
		
		String pageTitle = messageSource.getMessage("wordDictionary.catalog.page.title", 
				new Object[] { String.valueOf((pageNo - 1) * pageSize + 1), String.valueOf(((pageNo - 1) * pageSize + 1) + dictionaryEntryList.size() - 1) }, Locale.getDefault());
		
		String pageDescription = messageSource.getMessage("wordDictionary.catalog.page.pageDescription", 
				new Object[] { String.valueOf((pageNo - 1) * pageSize + 1), String.valueOf(((pageNo - 1) * pageSize + 1) + dictionaryEntryList.size() - 1) }, Locale.getDefault());
		
		model.put("pageTitle", pageTitle);
		model.put("pageDescription", pageDescription);
		
		return "wordDictionaryCatalog";
	}

	@RequestMapping(value = "/wordDictionaryNameCatalog/{page}", method = RequestMethod.GET)
	public String wordDictionaryNameCatalog(HttpServletRequest request, HttpSession session, 
			@PathVariable("page") int pageNo,
			Map<String, Object> model) throws DictionaryException {
		
		final int pageSize = 50;  // zmiana tego parametru wiaze sie ze zmiana w SitemapManager
		
		if (pageNo < 1) {
			pageNo = 1;
		}
				
		logger.info("Wyświetlanie katalogu słów(nazwa) dla strony: " + pageNo);
		
		// szukanie		
		List<DictionaryEntry> dictionaryEntryList = dictionaryManager.getWordsNameGroup(pageSize, pageNo - 1);
		
		int dictionaryEntriesSize = dictionaryManager.getDictionaryEntriesNameSize();
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryNameCatalogLoggerModel(Utils.createLoggerModelCommon(request), pageNo));

		// stworzenie zaslepkowego findWordRequest i findWordResult
		FindWordRequest findWordRequest = new FindWordRequest();
		
		FindWordResult findWordResult = new FindWordResult();
		
		List<FindWordResult.ResultItem> resultItemList = new ArrayList<FindWordResult.ResultItem>();
		
		for (DictionaryEntry dictionaryEntry : dictionaryEntryList) {
			resultItemList.add(new FindWordResult.ResultItem(dictionaryEntry));
		}

		findWordResult.setResult(resultItemList);
		
		if (dictionaryEntriesSize > (pageNo) * pageSize) {
			findWordResult.setMoreElemetsExists(true);
			
		} else {
			findWordResult.setMoreElemetsExists(false);
		}
		
		int lastPageNo = (dictionaryEntriesSize / pageSize) + (dictionaryEntriesSize % pageSize > 0 ? 1 : 0);
				
		model.put("selectedMenu", "wordDictionary");
		model.put("findWordRequest", findWordRequest);
		model.put("findWordResult", findWordResult);
		model.put("pageNo", pageNo);
		model.put("lastPageNo", lastPageNo);
		model.put("metaRobots", "noindex, follow");
		
		String pageTitle = messageSource.getMessage("wordDictionaryName.catalog.page.title", 
				new Object[] { String.valueOf((pageNo - 1) * pageSize + 1), String.valueOf(((pageNo - 1) * pageSize + 1) + dictionaryEntryList.size() - 1) }, Locale.getDefault());
		
		String pageDescription = messageSource.getMessage("wordDictionaryName.catalog.page.pageDescription", 
				new Object[] { String.valueOf((pageNo - 1) * pageSize + 1), String.valueOf(((pageNo - 1) * pageSize + 1) + dictionaryEntryList.size() - 1) }, Locale.getDefault());
		
		model.put("pageTitle", pageTitle);
		model.put("pageDescription", pageDescription);
				
		return "wordDictionaryNameCatalog";
	}
	
	@RequestMapping(value = "/wordDictionary/dictionary.pdf", method = RequestMethod.GET)
	public void getWordDictionaryPdf(HttpServletRequest request, HttpServletResponse response, HttpSession session, OutputStream outputStream) throws IOException {
		
		logger.info("Pobieranie słownika w postaci pdf");
				
		// pobranie sciezki do pliku ze slownikiem
		File pdfDictionary = dictionaryManager.getPdfDictionary();
		
		if (pdfDictionary == null || pdfDictionary.isFile() == false || pdfDictionary.canRead() == false) { // gdy nie mozna odczytac pliku
			
			response.sendError(404);
			
			PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
			
			loggerSender.sendLog(pageNoFoundExceptionLoggerModel);
			
			return;
		}

		// logowanie
		loggerSender.sendLog(new WordDictionaryPdfDictionaryLoggerModel(Utils.createLoggerModelCommon(request)));
		
		// ustawianie naglowkow
		response.setContentType("application/pdf");
		response.setContentLengthLong(pdfDictionary.length());
		//response.setHeader("Content-Disposition","attachment; filename=\"maly-skromny-japonski-slownik.pdf\""); 
		
		// wysylanie pliku
		FileInputStream sitemapFileInputStream = null;
		
		try {
			sitemapFileInputStream = new FileInputStream(pdfDictionary);
			
			copyStream(sitemapFileInputStream, outputStream);
			
		} catch (Exception e) {
			// INFO: czasami uzytkownik przerywa pobieranie PDF-a i to powoduje wyjatek po stronie serwera z komunikatem:
			// org.springframework.web.context.request.async.AsyncRequestNotUsableException: ServletOutputStream failed to write: java.io.IOException: Broken pipe
			// wiec wyjatki ignorujemy
			logger.error("Error during sending PDF", e);

		} finally {
			
			if (sitemapFileInputStream != null) {
				sitemapFileInputStream.close();
			}			
		}
	}
	
	private void copyStream(InputStream input, OutputStream output) throws IOException {
		
		byte[] buffer = new byte[1024];
		
		int bytesRead;
		
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
}
