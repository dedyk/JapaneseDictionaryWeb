package pl.idedyk.japanese.dictionary.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
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