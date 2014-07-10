package pl.idedyk.japanese.dictionary.web.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.MessageSource;

import pl.idedyk.japanese.dictionary.web.controller.StartController;
import pl.idedyk.japanese.dictionary.web.controller.WordDictionaryController;
import pl.idedyk.japanese.dictionary.web.controller.model.Breadcrumb;

public class BreadcrumbGenerator {
	
	public static List<Breadcrumb> createBreadcrumbList(MessageSource messageSource, String contextPath, Class<?> controllerClass, String targetUrl, String targetText) {
		
		List<Breadcrumb> result = new ArrayList<Breadcrumb>();
				
		// strona glowna
		result.add(new Breadcrumb(messageSource.getMessage("breadcrumb.page.main", null, null), contextPath + "/"));
		
		/*
		ErrorController.java
		FaviconIconController.java
		InfoController.java
		KanjiDictionaryController.java
		RobotsController.java
		SitemapController.java
		SuggestionController.java
		.java
		*/
		
		if (StartController.class.isAssignableFrom(controllerClass) == true) {
			// noop			
			
		} else if (WordDictionaryController.class.isAssignableFrom(controllerClass) == true) {			
			result.add(new Breadcrumb(messageSource.getMessage("breadcrumb.page.wordDictionary", null, null), LinkGenerator.generateWordDictionaryLink(contextPath)));
			
		}
		
		
		return result;
	}
}
