package pl.idedyk.japanese.dictionary.web.common;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
			return remoteIp;
		}
		
		remoteIp = httpServletRequest.getHeader("x-real-ip");

		if (remoteIp != null) {
			return remoteIp;
		}

		return httpServletRequest.getRemoteAddr();		
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
		
		String result = request.getRequestURL().toString();
		
		String queryString = request.getQueryString();
		
		if (queryString != null && queryString.trim().equals("") == false) {
			result += "?" + queryString;
		}
		
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
}
