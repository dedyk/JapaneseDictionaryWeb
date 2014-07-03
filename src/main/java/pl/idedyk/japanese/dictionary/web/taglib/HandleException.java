package pl.idedyk.japanese.dictionary.web.taglib;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.ErrorData;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.GeneralExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.LoggerModelCommon;

public class HandleException extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(HandleException.class);
	
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
		
		logger.error("Bład podczas wyświetlania strony", throwable);
		
		LoggerModelCommon loggerModelCommon = LoggerModelCommon.createLoggerModelCommon(
				sessionId, remoteIp, userAgent, requestURI, refererURL);
		
		GeneralExceptionLoggerModel generalExceptionLoggerModel = new GeneralExceptionLoggerModel(loggerModelCommon, statusCode, throwable);

		loggerSender.sendLog(generalExceptionLoggerModel);
		
		return SKIP_BODY;
	}

}
