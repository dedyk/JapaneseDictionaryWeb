package pl.idedyk.japanese.dictionary.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult.ResultItem;
import pl.idedyk.japanese.dictionary.api.dto.Attribute;
import pl.idedyk.japanese.dictionary.api.dto.AttributeList;
import pl.idedyk.japanese.dictionary.api.dto.AttributeType;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntry;
import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;
import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.AndroidSendMissingWordLoggerModel;
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
		FindWordRequest findWordRequest = Utils.createFindWordRequestForSystemLog(word, wordPlaceSearch);
		
		logger.info("[AndroidSendMissingWord] Wyszukiwanie słowek dla zapytania: " + findWordRequest);
		
		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findWord(findWordRequest);
		
		// logowanie
		loggerSender.sendLog(new AndroidSendMissingWordLoggerModel(Utils.createLoggerModelCommon(request), word, wordPlaceSearch));
		loggerSender.sendLog(new WordDictionarySearchLoggerModel(null, findWordRequest, findWordResult));
				
		// brak odpowiedzi
		response.sendError(204); // No content
	}
		
	@RequestMapping(value = "/android/search", method = RequestMethod.POST)
	public void search(HttpServletRequest request, HttpServletResponse response, Writer writer,
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
		
		logger.info("[AndroidSearch] Parsuję żądanie: " + jsonRequestSb.toString());
		
		JSONObject jsonObject = new JSONObject(jsonRequestSb.toString());

		FindWordRequest findWordRequest = createFindWordRequest(jsonObject);

		logger.info("[AndroidSendMissingWord] Wyszukiwanie słowek dla zapytania: " + findWordRequest);
		
		// szukanie		
		FindWordResult findWordResult = dictionaryManager.findWord(findWordRequest);
		
		// logowanie
		loggerSender.sendLog(new WordDictionarySearchLoggerModel(Utils.createLoggerModelCommon(request), findWordRequest, findWordResult));
		
		// przygotowanie odpowiedzi
		JSONObject jsonObjectFromFindWordResult = createJSONObjectFromFindWordResult(findWordResult);
		
		// zwrocenie wyniku
		writer.append(jsonObjectFromFindWordResult.toString());		
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
		findWordRequest.wordPlaceSearch = FindWordRequest.WordPlaceSearch.valueOf((String)searchJSONObject.get("wordPlaceSearch"));;
		
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
}
