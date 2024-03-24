package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.InfoLoggerModel;

@Controller
public class InfoController {
	
	private static final Logger logger = LogManager.getLogger(InfoController.class);
	
	@Autowired
	private LoggerSender loggerSender;
		
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {
		
		logger.info("Wyswietlanie strony informacyjnej");
		
		// logowanie
		loggerSender.sendLog(new InfoLoggerModel(Utils.createLoggerModelCommon(request)));
		
		model.put("selectedMenu", "info");
		
		return "info";
	}
}
