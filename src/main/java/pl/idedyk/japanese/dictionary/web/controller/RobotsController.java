package pl.idedyk.japanese.dictionary.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.BingSiteAuthGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RobotsGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;

@Controller
public class RobotsController {

	private static final Logger logger = LogManager.getLogger(RobotsController.class);
	
	@Value("${base.server}")
	private String baseServer;

	@Value("${bing.site.auth}")
	private String bingSiteAuth;
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired
	private ConfigService configService;

    @RequestMapping(value = "/robots.txt", method = RequestMethod.GET)
    public void getRobots(HttpServletRequest request, HttpSession session, Writer writer) throws IOException {
    	        
		logger.info("Generowanie pliku robots.txt");
		
		// logowanie
		loggerSender.sendLog(new RobotsGenerateLoggerModel(Utils.createLoggerModelCommon(request)));
		
		String robotsBody;
		
		// pobranie katalogu z konfiguracja
		File catalinaConfDir = configService.getCatalinaConfDir();
		
		if (catalinaConfDir == null) {
			robotsBody = getRobotDefaultBody(request);
			
		} else {			
			// sprawdzamy, czy istnieje plik z zawartoscia robots.txt
			File robotsTxtFile = new File(catalinaConfDir, "robots.txt");

			if (robotsTxtFile.exists() == false) {
				robotsBody = getRobotDefaultBody(request);
				
			} else {
				// wczytujemy plik robots.txt
				robotsBody = new String(Files.readAllBytes(Paths.get(robotsTxtFile.getAbsolutePath())), StandardCharsets.UTF_8);
			}
		}
		
		writer.append(robotsBody.toString());
    }
    
    private String getRobotDefaultBody(HttpServletRequest request) {
    	
		StringBuffer robotsBody = new StringBuffer();
    	
		robotsBody.append("User-agent: *\n");
		
		robotsBody.append("Disallow: /wordDictionary/autocomplete\n");
		
		robotsBody.append("Disallow: /kanjiDictionary/saveCurrectTab\n");
		robotsBody.append("Disallow: /kanjiDictionary/autocomplete\n");
		robotsBody.append("Disallow: /kanjiDictionary/showAvailableRadicals\n");
		robotsBody.append("Disallow: /kanjiDictionaryDetectSearch\n");
		
		robotsBody.append("Disallow: /suggestion/sendSuggestion\n\n");
		
		robotsBody.append("User-agent: Yandex\n");
		
		robotsBody.append("Disallow: /wordDictionaryNameDetails/\n\n");
		
		//robotsBody.append("User-agent: MJ12bot\n");
		
		//robotsBody.append("Disallow: /\n\n");
		
		//robotsBody.append("User-agent: AhrefsBot\n");
		
		//robotsBody.append("Disallow: /\n\n");

		//robotsBody.append("User-agent: SEOkicks-Robot\n");
		
		//robotsBody.append("Disallow: /\n\n");

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
		
		return robotsBody.toString();
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
