package pl.idedyk.japanese.dictionary.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.MethodNotAllowedExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.PageNoFoundExceptionLoggerModel;

@ControllerAdvice
public class ErrorController {

	private static final Logger logger = Logger.getLogger(ErrorController.class);
	
	@Autowired
	private LoggerSender loggerSender;
	
	@ExceptionHandler(Exception.class)
	public ModelAndView handleAllException(HttpServletRequest request, HttpServletResponse response, HttpSession session, Exception ex) {
 
		logger.error("Błąd podczas działania kontrolera", ex);

		// wyslanie do logger'a
		GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(
				Utils.createLoggerModelCommon(request), -1, ex);

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
