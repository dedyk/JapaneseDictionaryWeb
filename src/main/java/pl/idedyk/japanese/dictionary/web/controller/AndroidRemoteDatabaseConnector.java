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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import pl.idedyk.japanese.dictionary.api.dictionary.IDatabaseConnector.FindKanjisFromStrokeCountWrapper;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.KanjiEntry;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionaryRadicalsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.KanjiDictionarySearchStrokeCountLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionaryNameDetailsLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;

@Controller
public class AndroidRemoteDatabaseConnector {
	
	private static final Logger logger = Logger.getLogger(AndroidRemoteDatabaseConnector.class);
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
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
		
		// zwrocenie wyniku
		writer.append(gson.toJson(dictionaryEntry));
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
		
		// zwrocenie wyniku
		writer.append(gson.toJson(findKanjiResult));
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