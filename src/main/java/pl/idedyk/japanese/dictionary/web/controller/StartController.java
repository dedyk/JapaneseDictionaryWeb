package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.web.common.BreadcrumbGenerator;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.StartLoggerModel;

@Controller
public class StartController {
	
	private static final Logger logger = Logger.getLogger(StartController.class);
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired
	private MessageSource messageSource;
		
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {
		
		logger.info("Wyswietlanie glownej strony");
				
		// logowanie
		loggerSender.sendLog(new StartLoggerModel(Utils.createLoggerModelCommon(request)));
		
		model.put("breadcrumb", BreadcrumbGenerator.createBreadcrumbList(messageSource, request.getContextPath(), StartController.class,
				null, null));
		
		return "start";
	}
}
