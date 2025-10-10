package pl.idedyk.japanese.dictionary.web.common;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.WordPlaceSearch;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;

public class Utils {
	
	private static final String BASE64_PREFIX = "B64: ";
	
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
				
				if (isPrivateAddress(addr) == false) {
					currentAddr = addr.getHostName();
					
				} else {
					currentAddr = currentIp;
				}				
			
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
	
	public static boolean isPrivateAddress(InetAddress address) {
		
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()) {
            return true;
        }

        if (address.isSiteLocalAddress()) {
            return true;
        }

        byte[] addr = address.getAddress();

        // Dodatkowe ręczne sprawdzenie dla IPv4
        if (addr.length == 4) {
            int firstOctet = Byte.toUnsignedInt(addr[0]);
            int secondOctet = Byte.toUnsignedInt(addr[1]);

            // 10.0.0.0 – 10.255.255.255
            if (firstOctet == 10)
                return true;

            // 172.16.0.0 – 172.31.255.255
            if (firstOctet == 172 && (secondOctet >= 16 && secondOctet <= 31))
                return true;

            // 192.168.0.0 – 192.168.255.255
            if (firstOctet == 192 && secondOctet == 168)
                return true;

            // 169.254.0.0/16 (link-local)
            if (firstOctet == 169 && secondOctet == 254)
                return true;
        }

        return false;
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
		findWordRequest.searchGrammaFormAndExamples = false;
		
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
	
	public static String stringToBase64String(String s) {
		
		if (s == null) {
			return s;
		}
		
		return BASE64_PREFIX + Base64.encodeBase64String(s.getBytes());
	}
	
	public static String tryAndGetBase64String(String s) {
		
		if (s == null) {
			return null;
		}
		
		if (s.startsWith(BASE64_PREFIX) == false) {
			return null;
		}
		
		String base64String = s.substring(BASE64_PREFIX.length());
		
		try {
			return new String(Base64.decodeBase64(base64String));
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public static boolean isMobile(String userAgent) {
		
		// http://detectmobilebrowsers.com/
		
		if (userAgent == null) {
			return false;
		}
		
		userAgent = userAgent.toLowerCase();
		
		return userAgent.matches("(?i).*((android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows ce|xda|xiino).*") || 
				userAgent.substring(0,4).matches("(?i)1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\-|your|zeto|zte\\-");
	}
}
