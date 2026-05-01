package pl.idedyk.japanese.dictionary.web.service;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.hash.Hashing;
import com.google.common.net.HttpHeaders;

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
		checkETagAndGenerateHttp304NotModified(request, dictionaryEntry2.getClass().getSimpleName(), dictionaryEntry2.getEntryId());
	}
	
	public void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, JMnedict.Entry nameDictionaryEntry2) {
		checkETagAndGenerateHttp304NotModified(request, nameDictionaryEntry2.getClass().getSimpleName(), nameDictionaryEntry2.getEntryId());
	}
	
	public void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, KanjiCharacterInfo kanjiCharacterInfo) {
		checkETagAndGenerateHttp304NotModified(request, kanjiCharacterInfo.getClass().getSimpleName(), kanjiCharacterInfo.getId());
	}
	
	private void checkETagAndGenerateHttp304NotModified(HttpServletRequest request, String objectName, Integer id) {

		// pobranie If-None-Match z wywolania
		String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
		
		// jezeli nic nie ma to wychodzimy
		if (ifNoneMatch == null) {
			return;
		}
		
		// wygenerowanie ETag dla naszego obiektu
		String objectETag = generateEtag(request, objectName, id);
		
		// jezeli sa identyczne to generujemy 304 Not Modified
		if (ifNoneMatch.equals(objectETag) == true) {
			throw new HttpNotModifiedException("Not modified");
		}
	}
	
	public void addETagToResponse(HttpServletRequest request, HttpServletResponse response, JMdict.Entry dictionaryEntry2) {
		addETagToResponse(request, response, dictionaryEntry2.getClass().getSimpleName(), dictionaryEntry2.getEntryId());
	}
	
	public void addETagToResponse(HttpServletRequest request, HttpServletResponse response, JMnedict.Entry nameDictionaryEntry2) {
		addETagToResponse(request, response, nameDictionaryEntry2.getClass().getSimpleName(), nameDictionaryEntry2.getEntryId());
	}
	
	public void addETagToResponse(HttpServletRequest request, HttpServletResponse response, KanjiCharacterInfo kanjiCharacterInfo) {
		addETagToResponse(request, response, kanjiCharacterInfo.getClass().getSimpleName(), kanjiCharacterInfo.getId());
	}
	
	private void addETagToResponse(HttpServletRequest request, HttpServletResponse response, String objectName, Integer id) {

		String etag = generateEtag(request, objectName, id);
		
		response.addHeader(HttpHeaders.ETAG, etag);
	}
	
	private String generateEtag(HttpServletRequest request, String objectName, Integer id) {
		
		String userAgent = request.getHeader("User-Agent");
		boolean mobile = Utils.isMobile(userAgent);

		//
		
		// Gson gson = new Gson();
		
		StringBuffer dataToCount = new StringBuffer(4096);
		
		ThemeType theme = Utils.getTheme(request);
		
		// musimy pobrac motyw, aby poprawnie dzialalo przelaczanie
		dataToCount.append(version);
		dataToCount.append("/").append(mobile);
		dataToCount.append("/").append(theme);
		//dataToCount.append("/").append(gson.toJson(object));
		dataToCount.append("/").append(objectName);
		dataToCount.append("/").append(id);
		
		return "\"" + Hashing.sha256().hashString(dataToCount.toString(), Charset.defaultCharset()) + "\"";
	}
}
