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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;

@Controller
public class AndroidRemoteDatabaseConnector {
	
	private static final Logger logger = Logger.getLogger(AndroidRemoteDatabaseConnector.class);
	
	@RequestMapping(value = "/android/remoteDatabaseConnector/findDictionaryEntries", method = RequestMethod.POST)
	public void findDictionaryEntries(HttpServletRequest request, HttpServletResponse response, Writer writer,
			HttpSession session, Map<String, Object> model) throws IOException {
		
		String jsonRequest = getJson(request);
				
		logger.info("[AndroidRemoteDatabaseConnector.findDictionaryEntries] Parsuję żądanie: " + jsonRequest);
	
		Gson gson = new Gson();
		
		// {"searchGrammaFormAndExamples":false,"searchInfo":true,"searchKana":true,"searchKanji":true,"searchMainDictionary":true,"searchName":false,"searchOnlyCommonWord":false,"searchRomaji":true,"searchTranslate":true,"word":"kot","wordPlaceSearch":"START_WITH"}
		
	
		FindWordRequest findWordRequest = gson.fromJson(jsonRequest, FindWordRequest.class);
		
		//
		
		int a = 0;
		a++;
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