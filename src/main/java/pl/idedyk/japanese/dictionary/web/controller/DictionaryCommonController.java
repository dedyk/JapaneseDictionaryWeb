package pl.idedyk.japanese.dictionary.web.controller;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import pl.idedyk.japanese.dictionary.api.exception.DictionaryException;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.dictionary.DirectoryIndexManager;
import pl.idedyk.japanese.dictionary.web.dictionary.DirectoryIndexManager.IndexSectionType;
import pl.idedyk.japanese.dictionary.web.dictionary.DirectoryIndexManager.IndexType;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;
import pl.idedyk.japanese.dictionary.web.service.exception.HttpResourceGoneException;
import pl.idedyk.japanese.dictionary.web.service.exception.HttpServiceUnavailableException;
import pl.idedyk.japanese.dictionary2.dictionaryindex.xsd.SectionIndex;

public abstract class DictionaryCommonController {
	
	@Autowired
	protected MessageSource messageSource;
	
	@Autowired
	protected DirectoryIndexManager directoryIndexManager;
	
	@Autowired
	protected LoggerSender loggerSender;
	
	@Value("${base.server}")
	protected String baseServer;

	protected String processDictionaryCatalog(HttpServletRequest request, IndexType indexType, String sectionType, String sectionName, int pageNo, 
			boolean catalogEnabled, Map<String, Object> model, String pageTitleMessageId, String pageDescriptionMessageId,
			LoggerModelCommon successLoggerObject, String selectedMenu, String catalogPageName) throws DictionaryException, NoResourceFoundException {
		
		// pobieramy rodzaj sekcji
		IndexSectionType indexSectionType = directoryIndexManager.findIndexSectionType(sectionType);
		
		if (indexSectionType == null) { // zly kod
			// wysylamy sygnal 410	
			throw new HttpResourceGoneException("Resource no longer available");
		}
		
		// pobieramy wszystkie nazwy sekcji w ramach tego typu sekcji
		List<String> sectionNamesList = directoryIndexManager.getSectionNamesList(indexType, indexSectionType);
		
		if (sectionNamesList == null) {
			// wysylamy sygnal 410	
			throw new HttpResourceGoneException("Resource no longer available");			
		}
		
		// sprawdzenie, czy sekcja wskazany w adresie znajduje sie na naszej liscie sekcji
		if (sectionNamesList.contains(sectionName) == false) {
			// wysylamy sygnal 410	
			throw new HttpResourceGoneException("Resource no longer available");
		}
		
		// pobranie listy numerow stron
		List<Integer> sectionNamePageNoList = directoryIndexManager.getSectionNamePageList(indexType, indexSectionType, sectionName);
		
		// sprawdzenie, czy wybrana numer strony wystepuje
		if (sectionNamePageNoList == null || sectionNamePageNoList.contains(pageNo) == false) {
			// wysylamy sygnal 410	
			throw new HttpResourceGoneException("Resource no longer available");			
		}
		
		// czy katalog wlaczony
		if (catalogEnabled == false) {
			// wysylamy sygnal 503
			throw new HttpServiceUnavailableException("Service unavailable");
		}
		
		// pobranie zawartosci sekcji
		SectionIndex sectionIndex = directoryIndexManager.getSectionNameEntries(indexType, indexSectionType, sectionName, pageNo);
		
		if (sectionIndex == null) {
			// wysylamy sygnal 410	
			throw new HttpResourceGoneException("Resource no longer available");						
		}
		
		// nazwa i opis strony
		String pageTitle = messageSource.getMessage(pageTitleMessageId, new Object[] { sectionName, pageNo }, Locale.getDefault());		
		String pageDescription = messageSource.getMessage(pageDescriptionMessageId, new Object[] { sectionName, pageNo }, Locale.getDefault());
		
		// logowanie
		loggerSender.sendLog(successLoggerObject);		
						
		model.put("selectedMenu", selectedMenu);
		model.put("pageTitle", pageTitle);
		model.put("pageDescription", pageDescription);
		
		model.put("catalogPageName", catalogPageName);
		
		model.put("selectedSectionType", sectionType);
		model.put("selectedSectionName", sectionName);
		model.put("selectedSectionPageNo", pageNo);
		model.put("sectionNamesList", sectionNamesList);
		model.put("sectionNamePageNoList", sectionNamePageNoList);
		
		model.put("sectionIndex", sectionIndex);
		
		model.put("canonicalUrl", LinkGenerator.createCatalogLink(baseServer, catalogPageName, sectionType, sectionName, Long.valueOf(pageNo)));
		
		return catalogPageName;
	}
}
