package pl.idedyk.japanese.dictionary.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.WordDictionarySearchLoggerModel;

@Controller
public class AndroidController {
	
	private static final Logger logger = Logger.getLogger(AndroidController.class);
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Autowired
	private LoggerSender loggerSender;

	@RequestMapping(value = "/android/sendMissingWord", method = RequestMethod.POST)
	public void sendMissingWord(HttpServletRequest request, HttpServletResponse response, 
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
		
		logger.info("[AndroidSendMissingWord] Parsuję żądanie: " + jsonRequestSb.toString());
		
		JSONObject jsonObject = new JSONObject(jsonRequestSb.toString());
		
		String word = (String)jsonObject.get("word");
		FindWordRequest.WordPlaceSearch wordPlaceSearch = FindWordRequest.WordPlaceSearch.valueOf((String)jsonObject.get("wordPlaceSearch"));
		
		if (word == null || word.length() == 0) {
			logger.info("[SendMissingWord] Brak słowa");
			
			return;
		}
		
		// stworzenie obiektu FindWordRequest
		FindWordRequest findWordRequest = createFindWordRequest(word, wordPlaceSearch);
		
		logger.info("[AndroidSendMissingWord] Wyszukiwanie słowek dla zapytania: " + findWordRequest);
		
		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findWord(findWordRequest);
		
		// logowanie
		loggerSender.sendLog(new WordDictionarySearchLoggerModel(Utils.createLoggerModelCommon(request), findWordRequest, findWordResult));
				
		// brak odpowiedzi
		response.sendError(204); // No content
	}
	
	private FindWordRequest createFindWordRequest(String word, FindWordRequest.WordPlaceSearch wordPlaceSearch) {
		
		FindWordRequest findWordRequest = new FindWordRequest();
		
		List<String> tokenWord = Utils.tokenWord(word);
		
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
		findWordRequest.wordPlaceSearch = wordPlaceSearch;
		
		// searchIn
		findWordRequest.searchKanji = true;
		findWordRequest.searchKana = true;
		findWordRequest.searchRomaji = true;
		findWordRequest.searchTranslate = true;
		findWordRequest.searchInfo = true;
						
		// dictionaryEntryList
		findWordRequest.dictionaryEntryTypeList = null;
				
		// searchGrammaFormAndExamples
		findWordRequest.searchGrammaFormAndExamples = true;
		
		// searchName
		findWordRequest.searchName = true;
		
		return findWordRequest;
	}
}
