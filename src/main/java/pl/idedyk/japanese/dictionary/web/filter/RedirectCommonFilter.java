package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;

import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.PageNoFoundExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RedirectLoggerModel;

class RedirectCommonFilter {
	
	protected void redirectToUrl(WebApplicationContext webApplicationContext, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String redirectUrl) throws IOException {
		
		// przekierowanie		
		LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);

        // dodanie do logowania
		RedirectLoggerModel redirectLoggerModel = new RedirectLoggerModel(Utils.createLoggerModelCommon(httpServletRequest), redirectUrl);
		
		loggerSender.sendLog(redirectLoggerModel);

		httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		httpServletResponse.setHeader("Location", redirectUrl);
		
        // zrobienie commit'a
		httpServletResponse.flushBuffer();
	}
	
	protected void send404(WebApplicationContext webApplicationContext, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
		// przekierowanie		
		LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);

        // dodanie do logowania
		PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(httpServletRequest));
		
		loggerSender.sendLog(pageNoFoundExceptionLoggerModel);

		// wyslanie 404
		httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
		
        // zrobienie commit'a
		httpServletResponse.flushBuffer();
	}
}
