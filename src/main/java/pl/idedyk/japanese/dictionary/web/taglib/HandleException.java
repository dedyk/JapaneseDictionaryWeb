package pl.idedyk.japanese.dictionary.web.taglib;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.ErrorData;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;

public class HandleException extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LogManager.getLogger(HandleException.class);
	
	@Override
	public int doStartTag() throws JspException {
				
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);
		
		// pobranie bledu
		ErrorData errorData = pageContext.getErrorData();

		// pobranie danych
		String sessionId = pageContext.getSession().getId();
		
		ServletRequest request = pageContext.getRequest();
		
		String remoteIp = null;
		String userAgent = null;
		String refererURL = null;
		
		if (request instanceof HttpServletRequest) {
			
			HttpServletRequest httpServletRequest = (HttpServletRequest)request;
			
			remoteIp = Utils.getRemoteIp(httpServletRequest);
			userAgent = httpServletRequest.getHeader("User-Agent");
			refererURL = httpServletRequest.getHeader("Referer");
		}
		
		String requestURI = errorData.getRequestURI();
		int statusCode = errorData.getStatusCode();
		Throwable throwable = errorData.getThrowable();
		
		if (requestURI != null && requestURI.contains("/autocomple") == true) { // bledy autocomplete sa ignorowane, klient zrezygnowal z polaczenia
			logger.error("Błędy 'autocomplete' są ignorowane!");
			
			return SKIP_BODY;
		}
		
		if (throwable != null && throwable instanceof RequestRejectedException == true) { // bledy typu RequestRejectedException sa ignorowane
			logger.error("Błędy 'RequestRejectedException' są ignorowane!");
			
			if (pageContext.getResponse() instanceof HttpServletResponse) {
				
				HttpServletResponse httpServletResponse = (HttpServletResponse)pageContext.getResponse();
				
				httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
						
			return SKIP_BODY;
		}
		
		// wyjątek: class java.lang.IllegalStateException:IllegalStateException: getOutputStream() has already been called for this response jest ignorowany
		if (throwable.getClass().equals(IllegalStateException.class) == true && ExceptionUtils.getMessage(throwable).indexOf("getOutputStream() has already been called for this response") != -1) {
			logger.error("Błędy 'class java.lang.IllegalStateException:IllegalStateException: getOutputStream() has already been called for this response' są ignorowane!");
			
			return SKIP_BODY;
		}
		
		// wyjątek: Wyjątek: class org.apache.catalina.connector.ClientAbortException:ClientAbortException: java.io.IOException: Broken pipe jest ignorowany
		if (throwable.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException") && ExceptionUtils.getMessage(throwable).indexOf("java.io.IOException: Broken pipe") != -1) {
			logger.error("Błędy 'class org.apache.catalina.connector.ClientAbortException:ClientAbortException: java.io.IOException: Broken pipe' są ignorowane!");
			
			return SKIP_BODY;
		}
		
		logger.error("Bład podczas wyświetlania strony", throwable);
		
		LoggerModelCommon loggerModelCommon = LoggerModelCommon.createLoggerModelCommon(
				sessionId, remoteIp, userAgent, requestURI, refererURL);
		
		GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(loggerModelCommon, statusCode, throwable);

		loggerSender.sendLog(generalExceptionLoggerModel);
		
		return SKIP_BODY;
	}

}
