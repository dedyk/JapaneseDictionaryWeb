package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.ClientBlockLoggerModel;

public class FirewallFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(FirewallFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		boolean doBlock = false;
		
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;
		
		String userAgent = httpServletRequest.getHeader("User-Agent");	
		
		if (userAgent != null) {
			
			// sprawdzamy, czy zalezy zablokowac tego user agenta
			if (userAgent.contains("AspiegelBot") == true) {
				doBlock = true;
			}	
		} 
		
		if (doBlock == false) { // normalne wywolanie
			chain.doFilter(request, response);
			
		} else { // blokowanie
			
			logger.info("Blokowanie user agent: " + userAgent);
			
			ServletContext servletContext = request.getServletContext();
			
			WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			
			// wysylacz
			LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);
			
			ClientBlockLoggerModel clientBlockLoggerModel = new ClientBlockLoggerModel(Utils.createLoggerModelCommon(httpServletRequest));
			
			loggerSender.sendLog(clientBlockLoggerModel);
			
			//
			
			// wysylamy brak dostepu
			httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
	        
	        // zrobienie commit'a
	        response.flushBuffer();
		}
	}

	@Override
	public void destroy() {
		// noop
	}
}
