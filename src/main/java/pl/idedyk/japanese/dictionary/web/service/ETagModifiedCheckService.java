package pl.idedyk.japanese.dictionary.web.service;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.hash.Hashing;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.common.Utils.ThemeType;
import pl.idedyk.japanese.dictionary.web.service.exception.HttpNotModifiedException;
import pl.idedyk.japanese.dictionary2.jmdict.xsd.JMdict;
import pl.idedyk.japanese.dictionary2.jmnedict.xsd.JMnedict;
import pl.idedyk.japanese.dictionary2.kanjidic2.xsd.KanjiCharacterInfo;

@Service
public class ETagModifiedCheckService {
	
	@Value("${app.version}")
	private String version;
	
	public void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, JMdict.Entry dictionaryEntry2) {
		checkETagAndGenerateHttp304NotModified(request, (Object)dictionaryEntry2);
	}
	
	public void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, JMnedict.Entry nameDictionaryEntry2) {
		checkETagAndGenerateHttp304NotModified(request, (Object)nameDictionaryEntry2);
	}
	
	public void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, KanjiCharacterInfo kanjiCharacterInfo) {
		checkETagAndGenerateHttp304NotModified(request, (Object)kanjiCharacterInfo);
	}
	
	private void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, Object object) {
		
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
	
	public void addETagToResponse(HttpServletRequest request, HttpServletResponse response, JMdict.Entry dictionaryEntry2) {
		addETagToResponse(request, response, (Object)dictionaryEntry2);
	}
	
	public void addETagToResponse(HttpServletRequest request, HttpServletResponse response, JMnedict.Entry nameDictionaryEntry2) {
		addETagToResponse(request, response, (Object)nameDictionaryEntry2);
	}
	
	public void addETagToResponse(HttpServletRequest request, HttpServletResponse response, KanjiCharacterInfo kanjiCharacterInfo) {
		addETagToResponse(request, response, (Object)kanjiCharacterInfo);
	}
	
	private void addETagToResponse(HttpServletRequest request, HttpServletResponse response, Object object) {
		
		if (object == null) {
			return;
		}
		
		String etag = generateEtag(request, object);
		
		response.addHeader(HttpHeaders.ETAG, etag);
	}
	
	private String generateEtag(HttpServletRequest request, Object object) {
		
		Gson gson = new Gson();		
		
		StringBuffer dataToCount = new StringBuffer(4096);
		
		ThemeType theme = Utils.getTheme(request);
		
		// musimy pobrac motyw, aby poprawnie dzialalo przelaczanie
		dataToCount.append(version);
		dataToCount.append("/").append(theme);		
		dataToCount.append("/").append(gson.toJson(object));
		
		return "\"" + Hashing.sha256().hashString(dataToCount.toString(), Charset.defaultCharset()) + "\"";
	}
}
