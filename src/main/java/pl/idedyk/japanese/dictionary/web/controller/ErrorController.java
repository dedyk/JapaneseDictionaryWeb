package pl.idedyk.japanese.dictionary.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;

@ControllerAdvice
public class ErrorController {

	private static final Logger logger = Logger.getLogger(ErrorController.class);
	
	@Autowired
	private LoggerSender loggerSender;
	
	@ExceptionHandler(Exception.class)
	public ModelAndView handleAllException(HttpServletRequest request, HttpServletResponse response, HttpSession session, Exception ex) {
 
		logger.error("Błąd podczas działania kontrolera", ex);

		// wyslanie do logger'a
		GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(session.getId(), Utils.getRemoteIp(request), request.getHeader("User-Agent"), 
				request.getRequestURI(), -1, ex);

		loggerSender.sendLog(generalExceptionLoggerModel);

		// przygotowanie odpowiedzi
		Map<String, Object> model = new HashMap<String, Object>();
		
		return new ModelAndView("applicationError", model);
	}
}
