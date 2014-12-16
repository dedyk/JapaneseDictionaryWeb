package pl.idedyk.japanese.dictionary.web.controller;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.BingSiteAuthGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RobotsGenerateLoggerModel;

@Controller
public class RobotsController {

	private static final Logger logger = Logger.getLogger(RobotsController.class);
	
	@Value("${base.server}")
	private String baseServer;

	@Value("${bing.site.auth}")
	private String bingSiteAuth;
	
	@Autowired
	private LoggerSender loggerSender;

    @RequestMapping(value = "/robots.txt", method = RequestMethod.GET)
    public void getRobots(HttpServletRequest request, HttpSession session, Writer writer) throws IOException {
    	        
		logger.info("Generowanie pliku robots.txt");
		
		// logowanie
		loggerSender.sendLog(new RobotsGenerateLoggerModel(Utils.createLoggerModelCommon(request)));

		StringBuffer robotsBody = new StringBuffer();
    	
		robotsBody.append("User-agent: *\n");
		
		robotsBody.append("Disallow: /wordDictionary/autocomplete\n");
		
		robotsBody.append("Disallow: /kanjiDictionary/saveCurrectTab\n");
		robotsBody.append("Disallow: /kanjiDictionary/autocomplete\n");
		robotsBody.append("Disallow: /kanjiDictionary/showAvailableRadicals\n");
		robotsBody.append("Disallow: /kanjiDictionaryDetectSearch\n");
		
		robotsBody.append("Disallow: /suggestion/sendSuggestion\n\n");
		
		// robotsBody.append("User-agent: MJ12bot\n");
		
		// robotsBody.append("Disallow: /\n\n");
		
		// robotsBody.append("User-agent: AhrefsBot\n");
		
		// robotsBody.append("Disallow: /\n\n");
		
		/*
		robotsBody.append("User-agent: Googlebot\n");
		
		robotsBody.append("Disallow: /wordDictionary/autocomplete\n");
		
		robotsBody.append("Disallow: /kanjiDictionary/saveCurrectTab\n");
		robotsBody.append("Disallow: /kanjiDictionary/autocomplete\n");
		robotsBody.append("Disallow: /kanjiDictionary/showAvailableRadicals\n");
		robotsBody.append("Disallow: /kanjiDictionaryDetectSearch\n");
		
		robotsBody.append("Disallow: /suggestion/sendSuggestion\n");
		
		robotsBody.append("Disallow: /wordDictionaryCatalog\n");
		robotsBody.append("Disallow: /kanjiDictionaryCatalog\n\n");
		*/
		
		robotsBody.append("Sitemap: " + baseServer + request.getContextPath() + "/sitemap.xml\n");
		
		writer.append(robotsBody.toString());
    }

    @RequestMapping(value = "/BingSiteAuth.xml", method = RequestMethod.GET)
    public void getBingSiteAuth(HttpServletRequest request, HttpSession session, Writer writer) throws IOException {
    	        
		logger.info("Generowanie pliku BingSiteAuth.xml");
		
		// logowanie
		loggerSender.sendLog(new BingSiteAuthGenerateLoggerModel(Utils.createLoggerModelCommon(request)));

		StringBuffer robotsBody = new StringBuffer();
    	
		robotsBody.append("<?xml version=\"1.0\"?>\n");
		
		robotsBody.append("<users>\n");
		robotsBody.append("\t<user>" + bingSiteAuth + "</user>\n");
		robotsBody.append("</users>\n");
				
		writer.append(robotsBody.toString());
    }    
}
