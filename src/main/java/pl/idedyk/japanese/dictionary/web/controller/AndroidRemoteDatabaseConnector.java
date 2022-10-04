package pl.idedyk.japanese.dictionary.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import pl.idedyk.japanese.dictionary.api.dictionary.IDatabaseConnector.FindKanjisFromStrokeCountWrapper;
import pl.idedyk.japanese.dictionary.api.dictionary.IDatabaseConnector.GetAllKanjisWrapper;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPowerList;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;
import pl.idedyk.japanese.dictionary.api.dto.GroupWithTatoebaSentenceList;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerRequest;
import pl.idedyk.japanese.dictionary.api.dto.KanjiRecognizerResultItem;
import pl.idedyk.japanese.dictionary.api.dto.TransitiveIntransitivePairWithDictionaryEntry;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.dictionary.ZinniaManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryAllKanjisLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryDetectLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryGetKanjiEntryListLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryRadicalsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionarySearchStrokeCountLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionary2DetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetDictionaryEntriesNameSizeLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetDictionaryEntriesSizeLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetDictionaryEntryGroupTypesLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetGroupDictionaryEntriesLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetTatoebaSentenceGroupLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetTransitiveIntransitivePairsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryGetWordPowerListLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryNameDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;

@Controller
public class AndroidRemoteDatabaseConnector {
	
	private static final Logger logger = LogManager.getLogger(AndroidRemoteDatabaseConnector.class);
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Autowired
	private ZinniaManager zinniaManager;
	
	@Autowired
	private LoggerSender loggerSender;

	//
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/findDictionaryEntries", method = RequestMethod.POST)
	public void findDictionaryEntries(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.findDictionaryEntries] Parsuję żądanie: " + jsonRequest);
	
		// tworzenie wywolania z json'a
		FindWordRequest findWordRequest = gson.fromJson(jsonRequest, FindWordRequest.class);
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.findDictionaryEntries] Wyszukiwanie słowek dla zapytania: " + findWordRequest);

		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findDictionaryEntriesForRemoteDatabaseConnector(findWordRequest);
		
		// logowanie
		loggerSender.sendLog(new WordDictionarySearchLoggerModel(Utils.createLoggerModelCommon(request), findWordRequest, findWordResult, 1));

		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(findWordResult));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getDictionaryEntryById", method = RequestMethod.POST)
	public void getDictionaryEntryById(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);

		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryById] Parsuję żądanie: " + jsonRequest);
		
		// pobranie id
		String id = gson.fromJson(jsonRequest, String.class);
		
		// pobranie slowka
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryById(Integer.parseInt(id));
		
		if (dictionaryEntry != null) {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryById]: Znaleziono słowo: " + dictionaryEntry);
			
			loggerSender.sendLog(new WordDictionaryDetailsLoggerModel(Utils.createLoggerModelCommon(request), dictionaryEntry));
			
		} else {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryById]: Nie znaleziono słowa o id: " + id);
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(dictionaryEntry));
	}

	@RequestMapping(value = "/android/remoteDatabaseConnector/getDictionaryEntryByUniqueKey", method = RequestMethod.POST)
	public void getDictionaryEntryByUniqueKey(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);

		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryByUniqueKey] Parsuję żądanie: " + jsonRequest);
		
		// pobranie uniqueKey
		String uniqueKey = gson.fromJson(jsonRequest, String.class);
		
		// pobranie slowka
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryByUniqueKey(uniqueKey);
		
		if (dictionaryEntry != null) {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryByUniqueKey]: Znaleziono słowo: " + dictionaryEntry);
			
			loggerSender.sendLog(new WordDictionaryDetailsLoggerModel(Utils.createLoggerModelCommon(request), dictionaryEntry));
			
		} else {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryByUniqueKey]: Nie znaleziono słowa o uniqueKey: " + uniqueKey);
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(dictionaryEntry));
	}

	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getDictionaryEntry2ById", method = RequestMethod.POST)
	public void getDictionaryEntry2ById(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
				
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);

		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntry2ById] Parsuję żądanie: " + jsonRequest);
		
		// pobranie id
		Integer id = gson.fromJson(jsonRequest, Integer.class);
		
		// pobranie slowka
		JMdict.Entry entry = dictionaryManager.getDictionaryEntry2ById(id);
		
		if (entry != null) {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntry2ById]: Znaleziono słowo: " + entry.getEntryId());
			
			loggerSender.sendLog(new WordDictionary2DetailsLoggerModel(Utils.createLoggerModelCommon(request), entry));
			
		} else {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntry2ById]: Nie znaleziono słowa o id: " + id);
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(entry));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getDictionaryEntryNameById", method = RequestMethod.POST)
	public void getDictionaryEntryNameById(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);

		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryNameById] Parsuję żądanie: " + jsonRequest);
		
		// pobranie id
		String id = gson.fromJson(jsonRequest, String.class);
		
		// pobranie slowka
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryNameById(Integer.parseInt(id));
		
		if (dictionaryEntry != null) {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryNameById]: Znaleziono słowo: " + dictionaryEntry);
			
			loggerSender.sendLog(new WordDictionaryNameDetailsLoggerModel(Utils.createLoggerModelCommon(request), dictionaryEntry));
			
		} else {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryNameById]: Nie znaleziono słowa o id: " + id);
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(dictionaryEntry));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getDictionaryEntryNameByUniqueKey", method = RequestMethod.POST)
	public void getDictionaryEntryNameByUniqueKey(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);

		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryNameByUniqueKey] Parsuję żądanie: " + jsonRequest);
		
		// pobranie uniqueKey
		String uniqueKey = gson.fromJson(jsonRequest, String.class);
		
		// pobranie slowka
		DictionaryEntry dictionaryEntry = dictionaryManager.getDictionaryEntryNameByUniqueKey(uniqueKey);
		
		if (dictionaryEntry != null) {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryNameByUniqueKey]: Znaleziono słowo: " + dictionaryEntry);
			
			loggerSender.sendLog(new WordDictionaryNameDetailsLoggerModel(Utils.createLoggerModelCommon(request), dictionaryEntry));
			
		} else {
			
			// logowanie
			logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryNameByUniqueKey]: Nie znaleziono słowa o uniqueKey: " + uniqueKey);
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(dictionaryEntry));
	}	
		
	@RequestMapping(value = "/android/remoteDatabaseConnector/findAllAvailableRadicals", method = RequestMethod.POST)
	public void findAllAvailableRadicals(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);

		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.findAllAvailableRadicals] Parsuję żądanie: " + jsonRequest);

		// pobranie wejscie
		String[] radicals = gson.fromJson(jsonRequest, new TypeToken<String[]>(){}.getType());

		if (radicals == null) {
			radicals = new String[] { };
		}
		
		logger.info("[AndroidRemoteDatabaseConnector.findAllAvailableRadicals] Pokaż dostępne elementy podstawowe dla zapytania: " + Arrays.toString(radicals));

		Set<String> allAvailableRadicals = dictionaryManager.findAllAvailableRadicals(radicals);
		
		List<KanjiEntry> findKnownKanjiFromRadicalsResult = dictionaryManager.findKnownKanjiFromRadicals(radicals);

		// logowanie
		if (radicals.length > 0) {
			loggerSender.sendLog(new KanjiDictionaryRadicalsLoggerModel(Utils.createLoggerModelCommon(request), radicals, findKnownKanjiFromRadicalsResult.size()));
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");

		// zwrocenie wyniku
		writer.append(gson.toJson(allAvailableRadicals));
	}
		
	@RequestMapping(value = "/android/remoteDatabaseConnector/findKanjiFromRadicals", method = RequestMethod.POST)
	public void findKanjiFromRadicals(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);

		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.findKanjiFromRadicals] Parsuję żądanie: " + jsonRequest);

		// pobranie wejscie
		String[] radicals = gson.fromJson(jsonRequest, new TypeToken<String[]>(){}.getType());

		if (radicals == null) {
			radicals = new String[] { };
		}
		
		logger.info("[AndroidRemoteDatabaseConnector.findKanjiFromRadicals] Pokaż znaki kanji dla elementow podstawowych: " + Arrays.toString(radicals));
		
		List<KanjiEntry> findKnownKanjiFromRadicalsResult = dictionaryManager.findKnownKanjiFromRadicals(radicals);

		// logowanie
		if (radicals.length > 0) {
			loggerSender.sendLog(new KanjiDictionaryRadicalsLoggerModel(Utils.createLoggerModelCommon(request), radicals, findKnownKanjiFromRadicalsResult.size()));
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");

		// zwrocenie wyniku
		writer.append(gson.toJson(findKnownKanjiFromRadicalsResult));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/findKanjisFromStrokeCount", method = RequestMethod.POST)
	public void findKanjisFromStrokeCount(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);

		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.findKanjisFromStrokeCount] Parsuję żądanie: " + jsonRequest);
		
		// pobranie wejscie
        FindKanjisFromStrokeCountWrapper findKanjisFromStrokeCountWrapper = gson.fromJson(jsonRequest, FindKanjisFromStrokeCountWrapper.class);
		
		logger.info("[AndroidRemoteDatabaseConnector.findKanjisFromStrokeCount] Pokaż znaki kanji dla kresek od " + findKanjisFromStrokeCountWrapper.getFrom() + " do " + findKanjisFromStrokeCountWrapper.getTo());
		
		FindKanjiResult findKanjiResult = dictionaryManager.findKanjisFromStrokeCount(findKanjisFromStrokeCountWrapper.getFrom(), findKanjisFromStrokeCountWrapper.getTo());

		// logowanie
		loggerSender.sendLog(new KanjiDictionarySearchStrokeCountLoggerModel(Utils.createLoggerModelCommon(request), findKanjisFromStrokeCountWrapper.getFrom(), findKanjisFromStrokeCountWrapper.getTo()));
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(findKanjiResult));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/findKanji", method = RequestMethod.POST)
	public void findKanji(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.findKanji] Parsuję żądanie: " + jsonRequest);
	
		// tworzenie wywolania z json'a
		FindKanjiRequest findKanjiRequest = gson.fromJson(jsonRequest, FindKanjiRequest.class);
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.findKanji] Wyszukiwanie kanji dla zapytania: " + findKanjiRequest);

		// szukanie		
		FindKanjiResult findKanjiResult = dictionaryManager.findKanji(findKanjiRequest);
		
		// logowanie
		loggerSender.sendLog(new KanjiDictionarySearchLoggerModel(Utils.createLoggerModelCommon(request), findKanjiRequest, findKanjiResult));

		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(findKanjiResult));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getKanjiEntryById", method = RequestMethod.POST)
	public void getKanjiEntryById(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getKanjiEntryById] Parsuję żądanie: " + jsonRequest);
	
		// tworzenie wywolania z json'a
		Integer id = Integer.parseInt(gson.fromJson(jsonRequest, String.class));
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getKanjiEntryById] Pobierz kanji dla id: " + id);

		// pobranie		
		KanjiEntry kanjiEntry = dictionaryManager.getKanjiEntryById(id);
		
		if (kanjiEntry != null) {
			// logowanie
			loggerSender.sendLog(new KanjiDictionaryDetailsLoggerModel(Utils.createLoggerModelCommon(request), kanjiEntry));
			
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");

		// zwrocenie wyniku
		writer.append(gson.toJson(kanjiEntry));
	}	
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getKanjiEntry", method = RequestMethod.POST)
	public void getKanjiEntry(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getKanjiEntry] Parsuję żądanie: " + jsonRequest);
	
		// tworzenie wywolania z json'a
		String kanji = gson.fromJson(jsonRequest, String.class);
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getKanjiEntry] Pobierz kanji dla kanji: " + kanji);

		// szukanie		
		KanjiEntry kanjiEntry = dictionaryManager.findKanji(kanji);
		
		if (kanjiEntry != null) {
			// logowanie
			loggerSender.sendLog(new KanjiDictionaryDetailsLoggerModel(Utils.createLoggerModelCommon(request), kanjiEntry));			
		}
		
		// typ odpowiedzi
		response.setContentType("application/json");

		// zwrocenie wyniku
		writer.append(gson.toJson(kanjiEntry));
	}

	@RequestMapping(value = "/android/remoteDatabaseConnector/getKanjiEntryList", method = RequestMethod.POST)
	public void getKanjiEntryList(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getKanjiEntryList] Parsuję żądanie: " + jsonRequest);
	
		// tworzenie wywolania z json'a
		List<String> kanjiList = gson.fromJson(jsonRequest, new TypeToken<List<String>>(){}.getType());
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getKanjiEntry] Pobierz kanji dla listy kanji: " + kanjiList);

		// szukanie		
		List<KanjiEntry> kanjiEntryList = dictionaryManager.findKanjiList(kanjiList);
		
		// logowanie
		loggerSender.sendLog(new KanjiDictionaryGetKanjiEntryListLoggerModel(Utils.createLoggerModelCommon(request), kanjiEntryList));
		
		// typ odpowiedzi
		response.setContentType("application/json");

		// zwrocenie wyniku
		writer.append(gson.toJson(kanjiEntryList));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getAllKanjis", method = RequestMethod.POST)
	public void getAllKanjis(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getAllKanjis] Parsuję żądanie: " + jsonRequest);
	
		// tworzenie wywolania z json'a
		GetAllKanjisWrapper getAllKanjisWrapperRequest = gson.fromJson(jsonRequest, GetAllKanjisWrapper.class);
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getKanjiEntry] Pobierz wszystkie kanji dla withDetails: " + getAllKanjisWrapperRequest.isWithDetails() + " - onlyUsed" + getAllKanjisWrapperRequest.isOnlyUsed());

		// pobranie		
		List<KanjiEntry> kanjiEntryList = dictionaryManager.getAllKanjis(getAllKanjisWrapperRequest.isWithDetails(), getAllKanjisWrapperRequest.isOnlyUsed());
		
		// logowanie
		loggerSender.sendLog(new KanjiDictionaryAllKanjisLoggerModel(Utils.createLoggerModelCommon(request), getAllKanjisWrapperRequest.isWithDetails(), getAllKanjisWrapperRequest.isOnlyUsed()));
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(kanjiEntryList));
	}

	@RequestMapping(value = "/android/remoteDatabaseConnector/getTatoebaSentenceGroup", method = RequestMethod.POST)
	public void getTatoebaSentenceGroup(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getTatoebaSentenceGroup] Parsuję żądanie: " + jsonRequest);
	
		// tworzenie wywolania z json'a
		String groupId = gson.fromJson(jsonRequest, String.class);
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getTatoebaSentenceGroup] Pobierz zdania dla groupId: " + groupId);

		// pobranie		
		GroupWithTatoebaSentenceList groupWithTatoebaSentenceList = dictionaryManager.getTatoebaSentenceGroup(groupId);
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryGetTatoebaSentenceGroupLoggerModel(Utils.createLoggerModelCommon(request), groupId));
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(groupWithTatoebaSentenceList));
	}

	@RequestMapping(value = "/android/remoteDatabaseConnector/getGroupDictionaryEntries", method = RequestMethod.POST)
	public void getGroupDictionaryEntries(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getGroupDictionaryEntries] Parsuję żądanie: " + jsonRequest);
	
		// tworzenie wywolania z json'a
		GroupEnum groupName = gson.fromJson(jsonRequest, GroupEnum.class);
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getGroupDictionaryEntries] Pobierz słówka dla grupy: " + groupName);

		// pobranie		
		List<DictionaryEntry> result = dictionaryManager.getGroupDictionaryEntries(groupName);
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryGetGroupDictionaryEntriesLoggerModel(Utils.createLoggerModelCommon(request), groupName));
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(result));
	}

	@RequestMapping(value = "/android/remoteDatabaseConnector/getDictionaryEntriesSize", method = RequestMethod.POST)
	public void getDictionaryEntriesSize(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntriesSize] Parsuję żądanie: " + jsonRequest);
			
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getGroupDictionaryEntries] Pobierz rozmiar bazy słówek");

		// pobranie		
		int result = dictionaryManager.getDictionaryEntriesSize();
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryGetDictionaryEntriesSizeLoggerModel(Utils.createLoggerModelCommon(request)));
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(result));
	}

	@RequestMapping(value = "/android/remoteDatabaseConnector/getDictionaryEntriesNameSize", method = RequestMethod.POST)
	public void getDictionaryEntriesNameSize(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntriesNameSize] Parsuję żądanie: " + jsonRequest);
			
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntriesNameSize] Pobierz rozmiar bazy słówek nazw");

		// pobranie		
		int result = dictionaryManager.getDictionaryEntriesNameSize();
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryGetDictionaryEntriesNameSizeLoggerModel(Utils.createLoggerModelCommon(request)));
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(result));
	}

	@RequestMapping(value = "/android/remoteDatabaseConnector/getDictionaryEntryGroupTypes", method = RequestMethod.POST)
	public void getDictionaryEntryGroupTypes(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryGroupTypes] Parsuję żądanie: " + jsonRequest);
			
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getDictionaryEntryGroupTypes] Pobierz typy grupy słówek");

		// pobranie		
		List<GroupEnum> result = dictionaryManager.getDictionaryEntryGroupTypes();
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryGetDictionaryEntryGroupTypesLoggerModel(Utils.createLoggerModelCommon(request)));
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(result));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/kanjiRecognize", method = RequestMethod.POST)
	public void kanjiRecognize(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
						
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.kanjiRecognize] Parsuję żądanie: " + jsonRequest);
		
		KanjiRecognizerRequest kanjiRecognizerRequest = gson.fromJson(jsonRequest, KanjiRecognizerRequest.class);
		
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.kanjiRecognize] Rozpoznawanie znaków kanji");
		
		
		ZinniaManager.Character character = null;
		
		List<KanjiRecognizerResultItem> result = null;

		try {
			character = zinniaManager.createNewCharacter();

			character.setWidth(kanjiRecognizerRequest.getWidth());
			character.setHeight(kanjiRecognizerRequest.getHeight());

			for (int strokePathNo = 0; strokePathNo < kanjiRecognizerRequest.getStrokes().size(); ++strokePathNo) {
				
				List<KanjiRecognizerRequest.Point> pointList = kanjiRecognizerRequest.getStrokes().get(strokePathNo);
				
				for (KanjiRecognizerRequest.Point point : pointList) {					
					character.add(strokePathNo, point.getX(), point.getY());					
				}
			}

			result = character.recognize(100);
						
		} finally {
			if (character != null) {
				character.destroy();
			}
		}
		
		StringBuffer detectKanjiResultSb = new StringBuffer();
		
		for (int idx = 0; idx < 10 && idx < result.size(); ++idx) {
			
			KanjiRecognizerResultItem currentKanjiRecognizerResultItem = result.get(idx);
			
			detectKanjiResultSb.append(currentKanjiRecognizerResultItem.getKanji() + " " + currentKanjiRecognizerResultItem.getScore());
			
			if (idx != 10 - 1) {
				detectKanjiResultSb.append("\n");
			}
		}
		
		logger.info("[AndroidRemoteDatabaseConnector.kanjiRecognize] Rozpoznano znaki kanji:\n\n" + detectKanjiResultSb.toString());
		
		// logowanie
		loggerSender.sendLog(new KanjiDictionaryDetectLoggerModel(Utils.createLoggerModelCommon(request), "", result));

		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(result));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getTransitiveIntransitivePairsList", method = RequestMethod.POST)
	public void getTransitiveIntransitivePairsList(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getTransitiveIntransitivePairsList] Parsuję żądanie: " + jsonRequest);
			
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getTransitiveIntransitivePairsList] Pobierz pary czasowników przechodnich i nieprzechodnich");

		// pobranie		
		List<TransitiveIntransitivePairWithDictionaryEntry> result = dictionaryManager.getTransitiveIntransitivePairsList();
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryGetTransitiveIntransitivePairsLoggerModel(Utils.createLoggerModelCommon(request)));
		
		// typ odpowiedzi
		response.setContentType("application/json");
				
		// zwrocenie wyniku
		writer.append(gson.toJson(result));
	}
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/getWordPowerList", method = RequestMethod.POST)
	public void getWordPowerList(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException, DictionaryException {
		
		Gson gson = new Gson();
		
		// pobranie wejscia
		String jsonRequest = getJson(request);
				
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getWordPowerList] Parsuję żądanie: " + jsonRequest);
			
		// logowanie
		logger.info("[AndroidRemoteDatabaseConnector.getWordPowerList] Pobierz moce słów");

		// pobranie		
		WordPowerList result = dictionaryManager.getWordPowerList();
		
		// logowanie
		loggerSender.sendLog(new WordDictionaryGetWordPowerListLoggerModel(Utils.createLoggerModelCommon(request)));
		
		// typ odpowiedzi
		response.setContentType("application/json");
		
		// zwrocenie wyniku
		writer.append(gson.toJson(result));
	}

	//
	
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