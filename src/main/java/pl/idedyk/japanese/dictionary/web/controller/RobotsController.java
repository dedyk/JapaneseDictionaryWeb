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
import pl.idedyk.japanese.dictionary.web.logger.model.RobotsGenerateLoggerModel;

@Controller
public class RobotsController {

	private static final Logger logger = Logger.getLogger(RobotsController.class);
	
	@Value("${base.server}")
	private String baseServer;
	
	@Autowired
	private LoggerSender loggerSender;

    @RequestMapping(value = "/robots.txt", method = RequestMethod.GET)
    public void getRobots(HttpServletRequest request, HttpSession session, Writer writer) throws IOException {
    	        
		logger.info("Generowanie pliku robots.txt");
		
		// logowanie
		loggerSender.sendLog(new RobotsGenerateLoggerModel(Utils.createLoggerModelCommon(request)));

		StringBuffer robotsBody = new StringBuffer();
    	
		robotsBody.append("User-agent: *\n");
		robotsBody.append("Sitemap: " + baseServer + request.getContextPath() + "/sitemap.xml\n");
		
		writer.append(robotsBody.toString());
    }
}
