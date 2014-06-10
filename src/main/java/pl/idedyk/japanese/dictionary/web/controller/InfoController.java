package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.InfoLoggerModel;
import pl.idedyk.japanese.dictionary.web.schedule.ScheduleTask;

@Controller
public class InfoController extends CommonController {
	
	private static final Logger logger = Logger.getLogger(InfoController.class);
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired
	private ScheduleTask scheduleTask;
	
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {
		
		logger.info("Wyswietlanie strony informacyjnej");
		
		// logowanie
		loggerSender.sendLog(new InfoLoggerModel(session.getId(), Utils.getRemoteIp(request), request.getHeader("User-Agent")));
		
		model.put("selectedMenu", "info");
		
		return "info";
	}
}
