package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.web.sitemap.SitemapManager;

@Controller
public class StartController extends CommonController {
	
	@Autowired
	private SitemapManager sitemapManager;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String start(HttpServletRequest request, Map<String, Object> model) {
		
		int fixme2 = 1; // wysylanie logow
		
		int fixme = 1;
		
		// testy !!!!!
		
		
		
		System.out.println(sitemapManager.generateSitemap(request.getContextPath()));
		
		
		return "start";
	}
}
