package pl.idedyk.japanese.dictionary.web.common;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
}
