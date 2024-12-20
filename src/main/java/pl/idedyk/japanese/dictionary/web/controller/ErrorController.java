package pl.idedyk.japanese.dictionary.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.MethodNotAllowedExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.PageNoFoundExceptionLoggerModel;

@ControllerAdvice
public class ErrorController {

	private static final Logger logger = LogManager.getLogger(ErrorController.class);
	
	@Autowired
	private LoggerSender loggerSender;
	
	@ExceptionHandler(Exception.class)
	public ModelAndView handleAllException(HttpServletRequest request, HttpServletResponse response, HttpSession session, Exception ex) {
		
		// przygotowanie info do logger'a
		GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(
				Utils.createLoggerModelCommon(request), -1, ex);

		String requestURL = generalExceptionLoggerModel.getRequestURL();
		
		if (requestURL != null && requestURL.contains("/autocomple") == true) { // bledy autocomplete sa ignorowane, klient zrezygnowal z polaczenia
			logger.error("Błędy 'autocomplete' są ignorowane!");
			
			return new ModelAndView();
		}
		
		// wyjątek: class java.lang.IllegalStateException:IllegalStateException: getOutputStream() has already been called for this response jest ignorowany
		if (ex.getClass().equals(IllegalStateException.class) == true && ExceptionUtils.getMessage(ex).indexOf("getOutputStream() has already been called for this response") != -1) {
			logger.error("Błędy 'class java.lang.IllegalStateException:IllegalStateException: getOutputStream() has already been called for this response' są ignorowane!");
			
			return new ModelAndView();
		}
		
		// wyjątek: Wyjątek: class org.apache.catalina.connector.ClientAbortException:ClientAbortException: java.io.IOException: Broken pipe jest ignorowany
		if (ex.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException") && ExceptionUtils.getMessage(ex).indexOf("java.io.IOException: Broken pipe") != -1) {
			logger.error("Błędy 'class org.apache.catalina.connector.ClientAbortException:ClientAbortException: java.io.IOException: Broken pipe' są ignorowane!");
			
			return new ModelAndView();
		}

		// wyjątek: Wyjątek: class org.apache.catalina.connector.ClientAbortException:ClientAbortException: java.io.IOException: Connection reset by peer jest ignorowany
		if (ex.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException") && ExceptionUtils.getMessage(ex).indexOf("java.io.IOException: Connection reset by peer") != -1) {
			logger.error("Błędy 'class org.apache.catalina.connector.ClientAbortException:ClientAbortException: java.io.IOException: Connection reset by peer' są ignorowane!");
			
			return new ModelAndView();
		}
		
		logger.error("Błąd podczas działania kontrolera", ex);
		
		// wyslanie do logger'a
		loggerSender.sendLog(generalExceptionLoggerModel);

		// przygotowanie odpowiedzi
		Map<String, Object> model = new HashMap<String, Object>();
		
		return new ModelAndView("applicationError", model);
	}
		
	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public String handleNotFound(HttpServletRequest request, HttpServletResponse response, HttpSession session, Exception ex) {

		logger.error("Nie znaleziono strony: " + Utils.getRequestURL(request));
		
		// wysylanie do logger'a
		PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
		
		loggerSender.sendLog(pageNoFoundExceptionLoggerModel);

		return "page404";
	}
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
	public String handleMethodNotAllowed(HttpServletRequest request, HttpServletResponse response, HttpSession session, Exception ex) {
		
		HttpRequestMethodNotSupportedException httpRequestMethodNotSupportedException = (HttpRequestMethodNotSupportedException)ex;
		
		logger.error("Niedozwolona operacja: " + httpRequestMethodNotSupportedException.getMethod() + " dla: " + Utils.getRequestURL(request));
		
		// wysylanie do logger'a
		MethodNotAllowedExceptionLoggerModel methodNotAllowedExceptionLoggerModel = new MethodNotAllowedExceptionLoggerModel(Utils.createLoggerModelCommon(request));
		
		loggerSender.sendLog(methodNotAllowedExceptionLoggerModel);

		return "page405";
	}
}
