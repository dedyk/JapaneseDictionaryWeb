package pl.idedyk.japanese.dictionary.web.service;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
public class PageModifiedCheckService {
	
	@Value("${app.version}")
	private String version;
	
	public void checkIfPageIsModifiedAndGenerateHttp304NotModified(HttpServletRequest request, JMdict.Entry dictionaryEntry2) {
		checkIfPageIsModifiedAndGenerateHttp304NotModified(request, dictionaryEntry2.getClass().getSimpleName(), dictionaryEntry2.getEntryId(), dictionaryEntry2.getMisc().getLastModified());
	}
	
	public void checkIfPageIsModifiedAndGenerateHttp304NotModified(HttpServletRequest request, JMnedict.Entry nameDictionaryEntry2) {
		checkIfPageIsModifiedAndGenerateHttp304NotModified(request, nameDictionaryEntry2.getClass().getSimpleName(), nameDictionaryEntry2.getEntryId(), nameDictionaryEntry2.getMisc().getLastModified());
	}
	
	public void checkIfPageIsModifiedAndGenerateHttp304NotModified(HttpServletRequest request, KanjiCharacterInfo kanjiCharacterInfo) {
		checkIfPageIsModifiedAndGenerateHttp304NotModified(request, kanjiCharacterInfo.getClass().getSimpleName(), kanjiCharacterInfo.getId(), kanjiCharacterInfo.getMisc2().getLastModified());
	}
	
	private void checkIfPageIsModifiedAndGenerateHttp304NotModified(HttpServletRequest request, String objectName, Integer id, String realModifiedDateString) {
				
		// pobranie If-None-Match z wywolania
		String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
		
		// sprawdzamy, czy etag jest takie same
		if (ifNoneMatch != null) {
			// wygenerowanie ETag dla naszego obiektu
			String objectETag = generateEtag(request, objectName, id);
			
			// jezeli sa identyczne to generujemy 304 Not Modified
			if (ifNoneMatch.equals(objectETag) == true) {
				throw new HttpNotModifiedException("Not modified", objectETag, generateLastModified(realModifiedDateString));
				
			} else {
				return; // jezeli sa rozne, ale ETag istnieje to nie generujemy 304 tylko normalne 200 Ok
			}
		}
		
		// pobrnaie Last-Modified z wywolania
		String ifModifiedSince = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
		
		// sprawdzenie If-Modified-Since czy takie same
		if (ifModifiedSince != null) {
			String realModifiedDateStringInHttpFormat = generateLastModified(realModifiedDateString);
			
			// jezeli sa identyczne to generujemy 304 Not Modified
			if (realModifiedDateStringInHttpFormat.equals(ifModifiedSince) == true) {
				throw new HttpNotModifiedException("Not modified", generateEtag(request, objectName, id), realModifiedDateStringInHttpFormat);
			}
		}
	}
	
	public void addETagLastModifiedToResponse(HttpServletRequest request, HttpServletResponse response, JMdict.Entry dictionaryEntry2) {
		addETagLastModifiedToResponse(request, response, dictionaryEntry2.getClass().getSimpleName(), dictionaryEntry2.getEntryId(), dictionaryEntry2.getMisc().getLastModified());
	}
	
	public void addETagLastModifiedToResponse(HttpServletRequest request, HttpServletResponse response, JMnedict.Entry nameDictionaryEntry2) {
		addETagLastModifiedToResponse(request, response, nameDictionaryEntry2.getClass().getSimpleName(), nameDictionaryEntry2.getEntryId(), nameDictionaryEntry2.getMisc().getLastModified());
	}
	
	public void addETagLastModifiedToResponse(HttpServletRequest request, HttpServletResponse response, KanjiCharacterInfo kanjiCharacterInfo) {
		addETagLastModifiedToResponse(request, response, kanjiCharacterInfo.getClass().getSimpleName(), kanjiCharacterInfo.getId(), kanjiCharacterInfo.getMisc2().getLastModified());
	}
	
	private void addETagLastModifiedToResponse(HttpServletRequest request, HttpServletResponse response, String objectName, Integer id, String modifiedDateString) {

		// ETag
		String etag = generateEtag(request, objectName, id);
		
		response.addHeader(HttpHeaders.ETAG, etag);
		
		// Last Modified
		if (modifiedDateString != null) {
			String lastModifiedAsString = generateLastModified(modifiedDateString);

			response.addHeader(HttpHeaders.LAST_MODIFIED, lastModifiedAsString);
		}
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
	
	private String generateLastModified(String dateAsString) {
		
		SimpleDateFormat lastModifiedSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		
		SimpleDateFormat httpFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		httpFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		try {
			Date realModifiedDateAsDate = lastModifiedSDF.parse(dateAsString);
			
			return httpFormat.format(realModifiedDateAsDate);	
			
		} catch (ParseException e) {
			throw new RuntimeException("Can't create last modified field", e);
		}			
	}	
}
