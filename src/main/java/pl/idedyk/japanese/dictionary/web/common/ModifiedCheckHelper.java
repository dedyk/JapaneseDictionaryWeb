package pl.idedyk.japanese.dictionary.web.common;

import java.util.zip.CRC32;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.idedyk.japanese.dictionary.web.common.Utils.ThemeType;
import pl.idedyk.japanese.dictionary.web.service.exception.HttpNotModifiedException;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;
import pl.idedyk.japanese.dictionary2.jmnedict.xsd.JMnedict;
import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

public class ModifiedCheckHelper {
	
	public static void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, JMdict.Entry dictionaryEntry2) {
		checkETagAndGenerateHttp304NotModified(request, (Object)dictionaryEntry2);
	}
	
	public static void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, JMnedict.Entry nameDictionaryEntry2) {
		checkETagAndGenerateHttp304NotModified(request, (Object)nameDictionaryEntry2);
	}
	
	public static void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, KanjiCharacterInfo kanjiCharacterInfo) {
		checkETagAndGenerateHttp304NotModified(request, (Object)kanjiCharacterInfo);
	}
	
	private static void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, Object object) {
		
		if (object == null) {
			return;
		}
		
		// pobranie If-None-Match z wywolania
		String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
		
		// jezeli nic nie ma to wychodzimy
		if (ifNoneMatch == null) {
			return;
		}
		
		// wygenerowanie ETag dla naszego obiektu
		String objectETag = generateEtag(request, object);
		
		// jezeli sa identyczne to generujemy 304 Not Modified
		if (ifNoneMatch.equals(objectETag) == true) {
			throw new HttpNotModifiedException("Not modified");
		}
	}
	
	public static void addETagToResponse(HttpServletRequest request, HttpServletResponse response, JMdict.Entry dictionaryEntry2) {
		addETagToResponse(request, response, (Object)dictionaryEntry2);
	}
	
	public static void addETagToResponse(HttpServletRequest request, HttpServletResponse response, JMnedict.Entry nameDictionaryEntry2) {
		addETagToResponse(request, response, (Object)nameDictionaryEntry2);
	}
	
	public static void addETagToResponse(HttpServletRequest request, HttpServletResponse response, KanjiCharacterInfo kanjiCharacterInfo) {
		addETagToResponse(request, response, (Object)kanjiCharacterInfo);
	}
	
	private static void addETagToResponse(HttpServletRequest request, HttpServletResponse response, Object object) {
		
		if (object == null) {
			return;
		}
		
		String etag = generateEtag(request, object);
		
		response.addHeader(HttpHeaders.ETAG, etag);
	}
	
	private static String generateEtag(HttpServletRequest request, Object object) {
		
		Gson gson = new Gson();		
		CRC32 crc32 = new CRC32();
		
		StringBuffer dataToCount = new StringBuffer(gson.toJson(object));
		
		ThemeType theme = Utils.getTheme(request);
		
		// musimy pobrac motyw, aby poprawnie dzialalo przelaczanie
		if (theme != ThemeType.LIGHT) {
			dataToCount.append(theme);
		}
		
		crc32.update(dataToCount.toString().getBytes());
		
		return "\"" + Long.toHexString(crc32.getValue()) + "\"";
	}
}
