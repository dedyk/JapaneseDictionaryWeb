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
import pl.idedyk.japanese.dictionary.web.logger.model.StartLoggerModel;
import pl.idedyk.japanese.dictionary.web.schedule.ScheduleTask;

@Controller
public class StartController extends CommonController {
	
	private static final Logger logger = Logger.getLogger(StartController.class);
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired
	private ScheduleTask scheduleTask;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {
		
		logger.info("Wyswietlanie glownej strony");
		
		// logowanie
		loggerSender.sendLog(new StartLoggerModel(session.getId(), Utils.getRemoteIp(request), request.getHeader("User-Agent")));
				
		return "start";
	}
}
