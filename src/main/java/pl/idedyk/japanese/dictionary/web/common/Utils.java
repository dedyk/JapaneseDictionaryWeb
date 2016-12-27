package pl.idedyk.japanese.dictionary.web.common;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.mysql.jdbc.MysqlDataTruncation;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;

public class Utils {
	
	public static List<String> tokenWord(String word) {
		
		if (word == null) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		
		StringTokenizer st = new StringTokenizer(word, " \t\n\r\f.,:;()[]\"'?!-–{}");
		
		while (st.hasMoreTokens()) {
			result.add(st.nextToken());
		}
		
		return result;
	}
	
	public static boolean isKanjiSearchIn(String searchIn) {
		
		if (searchIn == null) {
			return false;
		}
		
		if (searchIn.equals("KANJI") == true) {
			return true;
		}
		
		return false;
	}

	public static boolean isKanaSearchIn(String searchIn) {
		
		if (searchIn == null) {
			return false;
		}
		
		if (searchIn.equals("KANA") == true) {
			return true;
		}
		
		return false;
	}

	public static boolean isRomajiSearchIn(String searchIn) {
		
		if (searchIn == null) {
			return false;
		}
		
		if (searchIn.equals("ROMAJI") == true) {
			return true;
		}
		
		return false;
	}

	public static boolean isTranslateSearchIn(String searchIn) {
		
		if (searchIn == null) {
			return false;
		}
		
		if (searchIn.equals("TRANSLATE") == true) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isInfoSearchIn(String searchIn) {
		
		if (searchIn == null) {
			return false;
		}
		
		if (searchIn.equals("INFO") == true) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isOnlyCommonWordsSearchIn(String searchIn) {
		
		if (searchIn == null) {
			return false;
		}
		
		if (searchIn.equals("COMMON_WORDS") == true) {
			return true;
		}
		
		return false;
	}

	public static boolean isGrammaFormAndExamples(String searchIn) {
		
		if (searchIn == null) {
			return false;
		}
		
		if (searchIn.equals("GRAMMA_FORM_AND_EXAMPLES") == true) {
			return true;
		}
		
		return false;
	}

	public static boolean isNames(String searchIn) {
		
		if (searchIn == null) {
			return false;
		}
		
		if (searchIn.equals("NAMES") == true) {
			return true;
		}
		
		return false;
	}
	
	public static Integer parseInteger(String text) {
		
		if (text == null) {
			return null;
		}
		
		text = text.trim();
		
		try {
			return Integer.parseInt(text);
			
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static String getRemoteIp(HttpServletRequest httpServletRequest) {
				
		String remoteIp = httpServletRequest.getHeader("x-forwarded-for");
				
		if (remoteIp != null) {
			return removeDuplicateIp(remoteIp);
		}
		
		remoteIp = httpServletRequest.getHeader("x-real-ip");

		if (remoteIp != null) {
			return removeDuplicateIp(remoteIp);
		}

		return removeDuplicateIp(httpServletRequest.getRemoteAddr());		
	}
	
	private static String removeDuplicateIp(String ip) {
		
		if (ip == null) {
			return ip;
		}
		
		String[] ipSplited = ip.split(",");
		
		if (ipSplited == null || ipSplited.length == 0) {
			return ip;
		}
		
		Set<String> uniqueIpSet = new LinkedHashSet<>();
		
		for (String currentIp : ipSplited) {
			
			currentIp = currentIp.trim();

			uniqueIpSet.add(currentIp);
		}
		
		StringBuffer result = new StringBuffer();
		
		for (String currentIp : uniqueIpSet) {
			
			if (result.length() > 0) {
				result.append(", ");
			}
			
			result.append(currentIp);			
		}
		
		return result.toString();
	}
		
	public static String getHostname(String ip) {
		
		if (ip == null) {
			return null;
		}
				
		String[] ipSplited = ip.split(",");
		
		if (ipSplited == null || ipSplited.length == 0) {
			return ip;
		}
		
		StringBuffer result = new StringBuffer();
		
		for (String currentIp : ipSplited) {
			
			currentIp = currentIp.trim();
			
			String currentAddr = null;
			
			try {
				InetAddress addr = InetAddress.getByName(currentIp);
			
				currentAddr = addr.getHostName();
			
			} catch (Exception e) {
				currentAddr = currentIp;
			}
			
			if (result.length() > 0) {
				result.append(", ");
			}
			
			result.append(currentAddr);			
		}
		
		return result.toString();
	}
	
	public static String getRequestURL(HttpServletRequest request) {
				
        String queryString = request.getQueryString();
        String requestUrl = request.getRequestURI() + ((queryString == null) ? "" : ("?" + queryString));
        String serverName = request.getServerName();
		String scheme = request.getScheme();
        
        int serverPort = request.getServerPort();
        
        //        
		
    	String xForwardedProto = request.getHeader("x-forwarded-proto");
    	
    	if (xForwardedProto != null) {
    		scheme = xForwardedProto;
    	}
    	
        String result = scheme + "://" + serverName + (serverPort != 80 && serverPort != 443 ? (":" + serverPort) : "") + requestUrl;	
		
		return result;
	}
	
	public static LoggerModelCommon createLoggerModelCommon(HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		
		LoggerModelCommon loggerModelCommon = LoggerModelCommon.createLoggerModelCommon(
				session.getId(),
				getRemoteIp(request),
				request.getHeader("User-Agent"),
				getRequestURL(request),
				request.getHeader("Referer"));
		
		return loggerModelCommon;
	}	
	
	public static FindWordRequest createFindWordRequestForSystemLog(String word, WordPlaceSearch wordPlaceSearch) {
		
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
		
		// searchOnlyCommonWord
		findWordRequest.searchOnlyCommonWord = false;
		
		// searchMainDictionary
		findWordRequest.searchMainDictionary = true;
		
		// dictionaryEntryList
		findWordRequest.dictionaryEntryTypeList = null;
				
		// searchGrammaFormAndExamples
		findWordRequest.searchGrammaFormAndExamples = true;
		
		// searchName
		findWordRequest.searchName = false;
		
		return findWordRequest;
	}
	
	public static boolean isMysqlDataTruncationException(Exception e) {
		
		Throwable eCause = e.getCause();
		
		if (e instanceof MysqlDataTruncation == true) {
			return true;
		}
		
		if (eCause != null && eCause instanceof MysqlDataTruncation == true) {
			return true;
		}
		
		if (e instanceof SQLException == true && e.getMessage().contains("Incorrect string value") == true) {
			return true;
		}
		
		if (eCause != null && eCause instanceof SQLException == true && eCause.getMessage().contains("Incorrect string value") == true) {
			return true;
		}

		return false;
	}
}
