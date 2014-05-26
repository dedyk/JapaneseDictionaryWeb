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

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.SuggestionStartLoggerModel;

@Controller
public class SuggestionController extends CommonController {
	
	private static final Logger logger = Logger.getLogger(WordDictionaryController.class);
	
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private LoggerSender loggerSender;

	@RequestMapping(value = "/suggestion", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {

		// logowanie
		loggerSender.sendLog(new SuggestionStartLoggerModel(session.getId(), Utils.getRemoteIp(request)));
	
		model.put("selectedMenu", "suggestion");
		
		return "suggestion";
	}
}
