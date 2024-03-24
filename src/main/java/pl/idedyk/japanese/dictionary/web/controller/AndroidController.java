package pl.idedyk.japanese.dictionary.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.api.android.queue.event.IQueueEvent;
import pl.idedyk.japanese.dictionary.api.android.queue.event.QueueEventWrapper;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult.ResultItem;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.api.dto.Attribute;
import pl.idedyk.japanese.dictionary.api.dto.AttributeList;
import pl.idedyk.japanese.dictionary.api.dto.AttributeType;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.lucene.LuceneDatabaseSuggesterAndSpellCheckerSource;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidGetMessageLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidGetSpellCheckerSuggestionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidQueueEventLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidSendMissingWordLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryAutocompleteLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.service.MessageService;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.AndroidAutocompleteMessageEntry;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message.MessageEntry;

@Controller
public class AndroidController {
	
	private static final Logger logger = LogManager.getLogger(AndroidController.class);
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired
	private MessageService messageService;

	@RequestMapping(value = "/android/sendMissingWord", method = RequestMethod.POST)
	public void sendMissingWord(HttpServletRequest request, HttpServletResponse response, 
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
		
		String readLine = null;
		
		StringBuffer jsonRequestSb = new StringBuffer();
		
		while (true) {		
			readLine = inputStreamReader.readLine();
			
			if (readLine == null) {
				break;
			}
			
			jsonRequestSb.append(readLine);			
		}
		
		logger.info("[AndroidSendMissingWord] Parsuję żądanie: " + jsonRequestSb.toString());
		
		JSONObject jsonObject = new JSONObject(jsonRequestSb.toString());
		
		String word = (String)jsonObject.get("word");
		WordPlaceSearch wordPlaceSearch = WordPlaceSearch.valueOf((String)jsonObject.get("wordPlaceSearch"));
		
		if (word == null || word.length() == 0) {
			logger.info("[SendMissingWord] Brak słowa");
			
			return;
		}
		
		// stworzenie obiektu FindWordRequest
		FindWordRequest findWordRequest = Utils.createFindWordRequestForSystemLog(word, wordPlaceSearch);
		
		logger.info("[AndroidSendMissingWord] Wyszukiwanie słowek dla zapytania: " + findWordRequest);
		
		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findWord(findWordRequest);
		
		// logowanie
		loggerSender.sendLog(new AndroidSendMissingWordLoggerModel(Utils.createLoggerModelCommon(request), word, wordPlaceSearch));
		loggerSender.sendLog(new WordDictionarySearchLoggerModel(null, findWordRequest, findWordResult, 1));
				
		// brak odpowiedzi
		response.sendError(204); // No content
	}
		
	@RequestMapping(value = "/android/search", method = RequestMethod.POST)
	public void search(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
		
		String readLine = null;
		
		StringBuffer jsonRequestSb = new StringBuffer();
		
		while (true) {		
			readLine = inputStreamReader.readLine();
			
			if (readLine == null) {
				break;
			}
			
			jsonRequestSb.append(readLine);			
		}
		
		logger.info("[AndroidSearch] Parsuję żądanie: " + jsonRequestSb.toString());
		
		JSONObject jsonObject = new JSONObject(jsonRequestSb.toString());

		FindWordRequest findWordRequest = createFindWordRequest(jsonObject);

		logger.info("[AndroidSearch] Wyszukiwanie słowek dla zapytania: " + findWordRequest);
		
		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findWord(findWordRequest);
		
		// logowanie
		loggerSender.sendLog(new WordDictionarySearchLoggerModel(Utils.createLoggerModelCommon(request), findWordRequest, findWordResult, 1));
		
		// przygotowanie odpowiedzi
		JSONObject jsonObjectFromFindWordResult = createJSONObjectFromFindWordResult(findWordResult);
		
		// zwrocenie wyniku
		writer.append(jsonObjectFromFindWordResult.toString());		
	}
	
	@RequestMapping(value = "/android/autocomplete", method = RequestMethod.POST)
	public void autocomplete(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException {
		
		BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
		
		String readLine = null;
		
		StringBuffer jsonRequestSb = new StringBuffer();
		
		while (true) {		
			readLine = inputStreamReader.readLine();
			
			if (readLine == null) {
				break;
			}
			
			jsonRequestSb.append(readLine);			
		}
		
		logger.info("[AndroidGetAutoComplete] Parsuję żądanie: " + jsonRequestSb.toString());
		
		JSONObject jsonObject = new JSONObject(jsonRequestSb.toString());
		
		String word = (String)jsonObject.get("word");
		String type = (String)jsonObject.get("type");
		
		if (word == null || word.length() == 0) {
			logger.info("[AndroidGetAutoComplete] Brak słowa");
			
			return;
		}
		
		if (word.length() < 2) {
			logger.info("[AndroidGetAutoComplete] Zbyt krótkie słowo: " + word);
			
			return;
		}
		
		if (type == null || type.length() == 0) {
			logger.info("[AndroidGetAutoComplete] Brak typu");
			
			return;
		}
		
		if (type.equals("wordDictionaryEntry") == false && type.equals("kanjiDictionaryEntry") == false) {
			logger.info("[AndroidGetAutoComplete] Niepoprawny typ: " + type);
			
			return;
		}
		
		word = word.trim();
		
		try {
			
			List<String> autocomplete = null;
			
			if (type.equals("wordDictionaryEntry") == true) {
				
				autocomplete = dictionaryManager.getAutocomplete(LuceneDatabaseSuggesterAndSpellCheckerSource.DICTIONARY_ENTRY_ANDROID, word, 5);
				
				// logowanie
				loggerSender.sendLog(new WordDictionaryAutocompleteLoggerModel(Utils.createLoggerModelCommon(request), word, autocomplete.size()));

				
			} else if (type.equals("kanjiDictionaryEntry") == true) {
				
				autocomplete = dictionaryManager.getAutocomplete(LuceneDatabaseSuggesterAndSpellCheckerSource.KANJI_ENTRY_ANDROID, word, 5);
				
				// logowanie
				loggerSender.sendLog(new KanjiDictionaryAutocompleteLoggerModel(Utils.createLoggerModelCommon(request), word, autocomplete.size()));
				
			}
			
			// wczytywanie komunikatu dla klienta
			AndroidAutocompleteMessageEntry androidMessageEntry = messageService.getMessageForAndroidAutocomplete(request.getHeader("User-Agent"), type);

			if (androidMessageEntry != null) { // may komunikat
				
				String additionalMessageForAutocomplete = androidMessageEntry.getMessage();
				
				if (additionalMessageForAutocomplete != null) {
					additionalMessageForAutocomplete = additionalMessageForAutocomplete.trim();
				}
				
				if (additionalMessageForAutocomplete != null && additionalMessageForAutocomplete.equals("") == false) {
										
					String[] additionalMessageForAutocompleteSplited = additionalMessageForAutocomplete.split("\n");
					
					for (int i = 0; i < additionalMessageForAutocompleteSplited.length; ++i) {
						autocomplete.add(i, additionalMessageForAutocompleteSplited[i]);
					}					
				}
			}
						
			JSONArray resultJsonArray = new JSONArray();

			for (String currentWordAutocomplete : autocomplete) {

				JSONObject resultJsonObject = new JSONObject();

				resultJsonObject.put("label", currentWordAutocomplete);
				resultJsonObject.put("value", currentWordAutocomplete);

				resultJsonArray.put(resultJsonObject);
			}

			// zwrocenie wyniku
			writer.append(resultJsonArray.toString());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@RequestMapping(value = "/android/spellCheckerSuggestion", method = RequestMethod.POST)
	public void spellCheckerSuggestion(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException {
		
		BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
		
		String readLine = null;
		
		StringBuffer jsonRequestSb = new StringBuffer();
		
		while (true) {		
			readLine = inputStreamReader.readLine();
			
			if (readLine == null) {
				break;
			}
			
			jsonRequestSb.append(readLine);			
		}
		
		logger.info("[AndroidSpellCheckerSuggestion] Parsuję żądanie: " + jsonRequestSb.toString());
		
		JSONObject jsonObject = new JSONObject(jsonRequestSb.toString());
		
		String word = (String)jsonObject.get("word");
		String type = (String)jsonObject.get("type");
		
		if (word == null || word.length() == 0) {
			logger.info("[AndroidGetAutoComplete] Brak słowa");
			
			return;
		}
		
		if (word.length() < 2) {
			logger.info("[AndroidGetAutoComplete] Zbyt krótkie słowo: " + word);
			
			return;
		}
		
		if (type == null || type.length() == 0) {
			logger.info("[AndroidGetAutoComplete] Brak typu");
			
			return;
		}
		
		if (type.equals("wordDictionaryEntry") == false && type.equals("kanjiDictionaryEntry") == false) {
			logger.info("[AndroidGetAutoComplete] Niepoprawny typ: " + type);
			
			return;
		}
		
		word = word.trim();
		
		try {
			
			List<String> spellCheckerSuggestion = null;
			
			if (type.equals("wordDictionaryEntry") == true) {
				
				spellCheckerSuggestion = dictionaryManager.getSpellCheckerSuggestion(LuceneDatabaseSuggesterAndSpellCheckerSource.DICTIONARY_ENTRY_ANDROID, word, 10);
				
				// logowanie
				loggerSender.sendLog(new AndroidGetSpellCheckerSuggestionLoggerModel(Utils.createLoggerModelCommon(request), word, type, spellCheckerSuggestion));

				
			} else if (type.equals("kanjiDictionaryEntry") == true) {
				
				spellCheckerSuggestion = dictionaryManager.getSpellCheckerSuggestion(LuceneDatabaseSuggesterAndSpellCheckerSource.KANJI_ENTRY_ANDROID, word, 10);
				
				// logowanie
				loggerSender.sendLog(new AndroidGetSpellCheckerSuggestionLoggerModel(Utils.createLoggerModelCommon(request), word, type, spellCheckerSuggestion));
				
			}			
			
			JSONArray resultJsonArray = new JSONArray();

			for (String currentSpellCheckerSuggesion : spellCheckerSuggestion) {

				JSONObject resultJsonObject = new JSONObject();

				resultJsonObject.put("value", currentSpellCheckerSuggesion);

				resultJsonArray.put(resultJsonObject);
			}

			// zwrocenie wyniku
			writer.append(resultJsonArray.toString());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private FindWordRequest createFindWordRequest(JSONObject searchJSONObject) {
		
		FindWordRequest findWordRequest = new FindWordRequest();
		
		List<String> tokenWord = Utils.tokenWord(searchJSONObject.getString("word"));
		
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
		findWordRequest.wordPlaceSearch = WordPlaceSearch.valueOf((String)searchJSONObject.get("wordPlaceSearch"));;
		
		// searchIn
		findWordRequest.searchKanji = searchJSONObject.getBoolean("searchKanji");
		findWordRequest.searchKana = searchJSONObject.getBoolean("searchKana");
		findWordRequest.searchRomaji = searchJSONObject.getBoolean("searchRomaji");
		findWordRequest.searchTranslate = searchJSONObject.getBoolean("searchTranslate");
		findWordRequest.searchInfo = searchJSONObject.getBoolean("searchInfo");
		
		// searchOnlyCommonWord
		findWordRequest.searchOnlyCommonWord = searchJSONObject.getBoolean("searchOnlyCommonWord");
		
		// searchMainDictionary
		findWordRequest.searchMainDictionary = searchJSONObject.getBoolean("searchMainDictionary");
		
		// dictionaryEntryList
		List<DictionaryEntryType> dictionaryEntryTypeList = null;		
		
		if (searchJSONObject.has("dictionaryEntryTypeList") == true) {
			
			JSONArray dictionaryEntryListJsonArray = searchJSONObject.getJSONArray("dictionaryEntryTypeList");
			
			dictionaryEntryTypeList = new ArrayList<DictionaryEntryType>();
			
			for (int idx = 0; idx < dictionaryEntryListJsonArray.length(); ++idx) {				
				dictionaryEntryTypeList.add(DictionaryEntryType.valueOf(dictionaryEntryListJsonArray.getString(idx)));				
			}			
		}
				
		findWordRequest.dictionaryEntryTypeList = dictionaryEntryTypeList;
				
		// searchGrammaFormAndExamples
		findWordRequest.searchGrammaFormAndExamples = searchJSONObject.getBoolean("searchGrammaFormAndExamples");
		
		// searchName
		findWordRequest.searchName = searchJSONObject.getBoolean("searchName");
		
		return findWordRequest;
	}
	
	@SuppressWarnings("deprecation")
	private JSONObject createJSONObjectFromFindWordResult(FindWordResult findWordResult) {
		
		JSONObject jsonObject = new JSONObject();
				
		jsonObject.put("moreElemetsExists", findWordResult.moreElemetsExists);
		jsonObject.put("foundGrammaAndExamples", findWordResult.foundGrammaAndExamples);
		jsonObject.put("foundNames", findWordResult.foundNames);
		
		JSONArray resultJsonObject = new JSONArray();
		
		List<ResultItem> result = findWordResult.result;
		
		for (ResultItem resultItem : result) {
			
			DictionaryEntry resultItemDictionaryEntry = resultItem.getDictionaryEntry();
			
			JSONObject resultItemDictionaryEntryJSONObject = new JSONObject();
									
			resultItemDictionaryEntryJSONObject.put("id", resultItemDictionaryEntry.getId());
			
			resultItemDictionaryEntryJSONObject.put("dictionaryEntryTypeList", convertListEnumToJSONArray(resultItemDictionaryEntry.getDictionaryEntryTypeList()));
			
			AttributeList attributeList = resultItemDictionaryEntry.getAttributeList();
			
			if (attributeList != null) {
				
				JSONArray attributeListJSONArray = new JSONArray();
				
				List<Attribute> attributeListList = attributeList.getAttributeList();
				
				if (attributeListList != null) {
					
					for (Attribute currentAttribute : attributeListList) {
						
						JSONObject currentAttributeJSONObject = new JSONObject();
						
						AttributeType currentAttributeAttributeType = currentAttribute.getAttributeType();
						List<String> currentAttributeAttributeValue = currentAttribute.getAttributeValue();
						
						currentAttributeJSONObject.put("attributeType", currentAttributeAttributeType.toString());
						currentAttributeJSONObject.put("attributeValue", new JSONArray(currentAttributeAttributeValue));
						
						attributeListJSONArray.put(currentAttributeJSONObject);						
					}					
				}				
				
				resultItemDictionaryEntryJSONObject.put("attributeList", attributeListJSONArray);
			}
						
			List<GroupEnum> groups = resultItemDictionaryEntry.getGroups();
			
			if (groups != null) {
				resultItemDictionaryEntryJSONObject.put("groups", convertListEnumToJSONArray(groups));
			}			
			
			resultItemDictionaryEntryJSONObject.put("kanji", resultItemDictionaryEntry.getKanji());
			
			resultItemDictionaryEntryJSONObject.put("prefixKana", resultItemDictionaryEntry.getPrefixKana());
			resultItemDictionaryEntryJSONObject.put("kanaList", new JSONArray(resultItemDictionaryEntry.getKanaList()));
			resultItemDictionaryEntryJSONObject.put("kana", resultItemDictionaryEntry.getKana());

			resultItemDictionaryEntryJSONObject.put("prefixRomaji", resultItemDictionaryEntry.getPrefixRomaji());
			resultItemDictionaryEntryJSONObject.put("romajiList", new JSONArray(resultItemDictionaryEntry.getRomajiList()));
			resultItemDictionaryEntryJSONObject.put("romaji", resultItemDictionaryEntry.getRomaji());
			
			resultItemDictionaryEntryJSONObject.put("translates", new JSONArray(resultItemDictionaryEntry.getTranslates()));
			
			resultItemDictionaryEntryJSONObject.put("info", resultItemDictionaryEntry.getInfo());
			
			resultItemDictionaryEntryJSONObject.put("name", resultItemDictionaryEntry.isName());
			
			resultJsonObject.put(resultItemDictionaryEntryJSONObject);
		}
		
		jsonObject.put("result", resultJsonObject);
		
		return jsonObject;
	}
	
	private JSONArray convertListEnumToJSONArray(List<? extends Enum<?>> listEnum) {
		
		JSONArray result = new JSONArray();
		
		if (listEnum == null) {
			return result;
		}
		
		for (Enum<?> currentEnum : listEnum) {
			result.put(currentEnum.toString());
		}
		
		return result;
	}
	
	@RequestMapping(value = "/android/receiveQueueEvent", method = RequestMethod.POST)
	public void receiveQueueEvent(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidController.receiveQueueEvent] Parsuję żądanie: " + jsonRequest);
	
		if (jsonRequest == null || StringUtils.isBlank(jsonRequest) == true) {
			
			logger.error("[AndroidController.receiveQueueEvent] Puste dane w json request.");
			
			response.sendError(400); // Bad request
			
			return;
		}
		
		// tworzenie wywolania z json'a
		QueueEventWrapper queueEventWrapper;
		
		try {
			queueEventWrapper = gson.fromJson(jsonRequest, QueueEventWrapper.class);
			
		} catch (Exception e) {
			
			logger.error("[AndroidController.receiveQueueEvent] Błąd parsowania obiektu zdarzeia", e);
			
			// logowanie
			GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(Utils.createLoggerModelCommon(request), -1, e);

			loggerSender.sendLog(generalExceptionLoggerModel);
			
			//
			
			response.sendError(400); // Bad request
			
			return;
		}
		
		if (queueEventWrapper == null || queueEventWrapper.getOperation() == null || queueEventWrapper.getCreateDate() == null) {
			
			logger.error("[AndroidController.receiveQueueEvent] Nie udało się wczytać obiektu zdarzenia. Otrzymano pusty lub niepełny obiekt.");
			
			response.sendError(400); // Bad request
			
			return;
		}
		
		//
		
        SimpleDateFormat sdf = new SimpleDateFormat(IQueueEvent.dateFormat);

        Date createDateDate = null;

        try {
            createDateDate = sdf.parse(queueEventWrapper.getCreateDate());

        } catch (ParseException e) {
        	createDateDate = new Date();
        }
		
		// logowanie
		loggerSender.sendLog(new AndroidQueueEventLoggerModel(Utils.createLoggerModelCommon(request), 
				queueEventWrapper.getUserId(), queueEventWrapper.getOperation(), createDateDate, queueEventWrapper.getParams()));

		// typ odpowiedzi
		response.setContentType("application/json");
		
		// brak odpowiedzi
		response.sendError(204); // No content
	}
	
	@RequestMapping(value = "/android/getMessage", method = RequestMethod.POST)
	public void getMessage(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws Exception {

		// wczytywanie komunikatu dla klienta
		MessageEntry androidMessageEntry = messageService.getMessageForAndroid(request.getHeader("User-Agent"));
		
		// logowanie
		loggerSender.sendLog(new AndroidGetMessageLoggerModel(Utils.createLoggerModelCommon(request)));
		
		// zwrocenie wyniku
		JSONObject resultJsonObject = new JSONObject();
		
		if (androidMessageEntry != null) {
			resultJsonObject.put("timestamp", androidMessageEntry.getTimestamp().trim());
			resultJsonObject.put("message", androidMessageEntry.getMessage() != null ? androidMessageEntry.getMessage().trim() : null);
		}		
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// logowanie
		logger.info("[AndroidController.getMessage] Zwrócenie odpowiedzi: " + resultJsonObject.toString());
		
		// zwrocenie wyniku
		writer.append(resultJsonObject.toString());
	}
	
	private String getJson(HttpServletRequest request) throws IOException {
		
		BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
		
		String readLine = null;
		
		StringBuffer jsonRequestSb = new StringBuffer();
		
		while (true) {		
			readLine = inputStreamReader.readLine();
			
			if (readLine == null) {
				break;
			}
			
			jsonRequestSb.append(readLine);
		}

		return jsonRequestSb.toString();
	}
}
