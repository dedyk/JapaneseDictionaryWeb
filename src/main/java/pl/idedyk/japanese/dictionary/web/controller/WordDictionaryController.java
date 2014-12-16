package pl.idedyk.japanese.dictionary.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.controller.validator.WordDictionarySearchModelValidator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.PageNoFoundExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RedirectLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryCatalogLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryNameCatalogLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryNameDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryStartLoggerModel;

@Controller
public class WordDictionaryController {

	private static final Logger logger = Logger.getLogger(WordDictionaryController.class);

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
		model.put("selectedMenu", "wordDictionary");
		
		return "wordDictionary";
	}

	@RequestMapping(value = "/wordDictionarySearch", method = RequestMethod.GET)
	public String search(HttpServletRequest request, HttpSession session, @ModelAttribute("command") @Valid final WordDictionarySearchModel searchModel,
			BindingResult result, Map<String, Object> model) {
		
		// gdy cos bedzie zmieniane trzeba rowniez zmienic w link generatorze

		if (result.hasErrors() == true) {
						
			model.put("addableDictionaryEntryList", DictionaryEntryType.getAddableDictionaryEntryList());
			model.put("command", searchModel);
			model.put("selectedMenu", "wordDictionary");
			
			return "wordDictionary";
		}
		
		// stworzenie obiektu FindWordRequest
		FindWordRequest findWordRequest = createFindWordRequest(searchModel);
		
		logger.info("Wyszukiwanie słowek dla zapytania: " + findWordRequest);
		
		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findWord(findWordRequest);
		
		// logowanie
		loggerSender.sendLog(new WordDictionarySearchLoggerModel(Utils.createLoggerModelCommon(request), findWordRequest, findWordResult));

		// logowanie wyszukiwania, dodatkowe sprawdzenie przez system, aby zalogowac ewentualne brakujace slowa
		// uruchom w osobnym watku
		new Thread(new Runnable() {
			
			@Override
			public void run() {				
				FindWordRequest findWordRequestForSystemLog = Utils.createFindWordRequestForSystemLog(searchModel.getWord(), FindWordRequest.WordPlaceSearch.valueOf(searchModel.getWordPlace()));
				
				FindWordResult findWordResultForSystemLog = dictionaryManager.findWord(findWordRequestForSystemLog);
			
				loggerSender.sendLog(new WordDictionarySearchLoggerModel(null, findWordRequestForSystemLog, findWordResultForSystemLog));				
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
		
		model.put("addableDictionaryEntryList", DictionaryEntryType.getAddableDictionaryEntryList());
		model.put("command", searchModel);
		model.put("selectedMenu", "wordDictionary");
		model.put("findWordRequest", findWordRequest);
		model.put("findWordResult", findWordResult);
		model.put("doNotShowSocialButtons", Boolean.TRUE);
		
		if (findWordResult.foundGrammaAndExamples == true) {
			model.put("searchResultInfo", messageSource.getMessage("wordDictionary.page.search.info.foundGrammaAndExamples", 
					new Object[] { }, Locale.getDefault()));
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
			List<String> wordAutocomplete = dictionaryManager.getWordAutocomplete(term, 5);

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
	public String showWordDictionaryDetails(HttpServletRequest request, HttpSession session, @PathVariable("id") int id, @PathVariable("kanji") String kanji,
			@PathVariable("kana") String kana, @RequestParam(value = "forceDictionaryEntryType", required = false) String forceDictionaryEntryType, Map<String, Object> model) {
		
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryById(id);
						
		// tytul strony
		if (dictionaryEntry != null) {
			
			logger.info("Znaleziono słówko dla zapytania o szczegóły słowa: " + dictionaryEntry);
			
			// logowanie
			loggerSender.sendLog(new WordDictionaryDetailsLoggerModel(Utils.createLoggerModelCommon(request), dictionaryEntry));

			String[] wordDictionaryDetailsTitleAndDescription = getWordDictionaryDetailsTitleAndDescription(dictionaryEntry);
						
			String pageTitle = wordDictionaryDetailsTitleAndDescription[0];
			String pageDescription = wordDictionaryDetailsTitleAndDescription[1];
			
			model.put("pageTitle", pageTitle);
			model.put("pageDescription", pageDescription);
			
		} else {
			
			logger.info("Nie znaleziono słówka dla zapytania o szczegóły słowa: " + id + " / " + kanji + " / " + kana);
			
			String pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.with.kanji", 
					new Object[] { "-", "-", "-" }, Locale.getDefault());
			
			model.put("pageTitle", pageTitle);
		}
		
		if (forceDictionaryEntryType != null) {
			
			try {
				DictionaryEntryType forceDictionaryEntryTypeType = DictionaryEntryType.valueOf(forceDictionaryEntryType);
				
				model.put("forceDictionaryEntryType", forceDictionaryEntryTypeType);
				
			} catch (Exception e) {
				
				logger.info("Niepoprawna wartość parametru 'forceDictionaryEntryType' = " + forceDictionaryEntryType);				
			}
		}		
				
		model.put("dictionaryEntry", dictionaryEntry);
		model.put("selectedMenu", "wordDictionary");
		
		return "wordDictionaryDetails";
	}
	
	@RequestMapping(value = "/wordDictionaryDetails/{id}", method = RequestMethod.GET)
	public void showWordDictionaryDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("id") int id) throws IOException {
		
		processWordDictionaryDetailsRedirect(request, response, id);
	}

	@RequestMapping(value = "/wordDictionaryDetails/{id}/{kanji}", method = RequestMethod.GET)
	public void showWordDictionaryDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("id") int id, @PathVariable("kanji") String kanji) throws IOException {
		
		processWordDictionaryDetailsRedirect(request, response, id);
	}

	private void processWordDictionaryDetailsRedirect(HttpServletRequest request, HttpServletResponse response, int id) throws IOException {
				
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
			@PathVariable("kana") String kana, Map<String, Object> model) {
		
		// pobranie slowa
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryNameById(id);
						
		// tytul strony
		if (dictionaryEntry != null) {
			
			// sprawdzenie, czy nie odwolujemy sie do innej strony
			String dictionaryEntryKanji = "-";
			
			if (dictionaryEntry.isKanjiExists() == true) {
				dictionaryEntryKanji = dictionaryEntry.getKanji();
			}
			
			String dictionaryEntryKana = dictionaryEntry.getKana();
			
			if (dictionaryEntryKanji.equals(kanji) == false || dictionaryEntryKana.equals(kana) == false) { // przekierowanie na wlasciwa strone o id
				
				String destinationUrl = LinkGenerator.generateDictionaryEntryDetailsLink(request.getContextPath(), dictionaryEntry, null);
				
				RedirectLoggerModel redirectLoggerModel = new RedirectLoggerModel(Utils.createLoggerModelCommon(request), destinationUrl);
				
				loggerSender.sendLog(redirectLoggerModel);	
				
				return "redirect:" + destinationUrl;			
			}			
			
			logger.info("Znaleziono słówko dla zapytania o szczegóły słowa (nazwa): " + dictionaryEntry);
			
			// logowanie
			loggerSender.sendLog(new WordDictionaryNameDetailsLoggerModel(Utils.createLoggerModelCommon(request), dictionaryEntry));

			String[] wordDictionaryDetailsTitleAndDescription = getWordDictionaryDetailsTitleAndDescription(dictionaryEntry);
						
			String pageTitle = wordDictionaryDetailsTitleAndDescription[0];
			String pageDescription = wordDictionaryDetailsTitleAndDescription[1];
			
			model.put("pageTitle", pageTitle);
			model.put("pageDescription", pageDescription);
			
		} else {
			
			logger.info("Nie znaleziono słówka dla zapytania o szczegóły słowa (nazwa): " + id + " / " + kanji + " / " + kana);
			
			String pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.with.kanji", 
					new Object[] { "-", "-", "-" }, Locale.getDefault());
			
			model.put("pageTitle", pageTitle);
		}
						
		model.put("dictionaryEntry", dictionaryEntry);
		model.put("selectedMenu", "wordDictionary");
		
		return "wordDictionaryDetails";
	}
	
	@RequestMapping(value = "/wordDictionaryNameDetails/{id}", method = RequestMethod.GET)
	public void showWordDictionaryNameDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("id") int id) throws IOException {
		
		processWordDictionaryNameDetailsRedirect(request, response, id);
	}

	@RequestMapping(value = "/wordDictionaryNameDetails/{id}/{kanji}", method = RequestMethod.GET)
	public void showWordDictionaryNameDetails(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
			@PathVariable("id") int id, @PathVariable("kanji") String kanji) throws IOException {
		
		processWordDictionaryNameDetailsRedirect(request, response, id);
	}
	
	private void processWordDictionaryNameDetailsRedirect(HttpServletRequest request, HttpServletResponse response, int id) throws IOException {
		
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
	
	private String[] getWordDictionaryDetailsTitleAndDescription(DictionaryEntry dictionaryEntry) {
		
		String dictionaryEntryKanji = dictionaryEntry.getKanji();
		String dictionaryEntryKana = dictionaryEntry.getKana();
		String dictionaryEntryRomaji = dictionaryEntry.getRomaji();

		boolean withKanji = dictionaryEntryKanji != null ? true : false;
		
		String pageTitle = null;
		String pageDescription = null;
		
		if (withKanji == true) {
			
			pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.with.kanji", 
					new Object[] { dictionaryEntryKanji, dictionaryEntryKana, dictionaryEntryRomaji }, Locale.getDefault());
			
			pageDescription = messageSource.getMessage("wordDictionaryDetails.page.pageDescription.with.kanji", 
					new Object[] { dictionaryEntryKanji != null ? dictionaryEntryKanji : "-",
							dictionaryEntryKana,
							dictionaryEntryRomaji,
					}, Locale.getDefault());
			
		} else {
			
			pageTitle = messageSource.getMessage("wordDictionaryDetails.page.title.without.kanji", 
					new Object[] { dictionaryEntryKana, dictionaryEntryRomaji }, Locale.getDefault());
			
			pageDescription = messageSource.getMessage("wordDictionaryDetails.page.pageDescription.without.kanji", 
					new Object[] { dictionaryEntryKana, dictionaryEntryRomaji }, Locale.getDefault());			
		}
		
		return new String[] { pageTitle, pageDescription };		
	}
	
	@RequestMapping(value = "/wordDictionaryCatalog/{page}", method = RequestMethod.GET)
	public String wordDictionaryCatalog(HttpServletRequest request, HttpSession session, 
			@PathVariable("page") int pageNo,
			Map<String, Object> model) {
		
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
		
		int lastPageNo = (dictionaryEntriesSize / pageSize) + 1;
				
		model.put("selectedMenu", "wordDictionary");
		model.put("findWordRequest", findWordRequest);
		model.put("findWordResult", findWordResult);
		model.put("pageNo", pageNo);
		model.put("lastPageNo", lastPageNo);
		//model.put("metaRobots", "noindex, follow");
		
		return "wordDictionaryCatalog";
	}

	@RequestMapping(value = "/wordDictionaryNameCatalog/{page}", method = RequestMethod.GET)
	public String wordDictionaryNameCatalog(HttpServletRequest request, HttpSession session, 
			@PathVariable("page") int pageNo,
			Map<String, Object> model) {
		
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
		
		int lastPageNo = (dictionaryEntriesSize / pageSize) + 1;
				
		model.put("selectedMenu", "wordDictionary");
		model.put("findWordRequest", findWordRequest);
		model.put("findWordResult", findWordResult);
		model.put("pageNo", pageNo);
		model.put("lastPageNo", lastPageNo);
		//model.put("metaRobots", "noindex, follow");
		
		return "wordDictionaryNameCatalog";
	}
}
