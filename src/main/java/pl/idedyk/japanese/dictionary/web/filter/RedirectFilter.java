package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.RedirectLoggerModel;

public class RedirectFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		ServletContext servletContext = request.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

		Properties applicationProperties = (Properties)webApplicationContext.getBean("applicationProperties");
				
		String configBaseServer = applicationProperties.getProperty("base.server");
		
		//
		
		HttpServletRequest httpServletReuqest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;
		
		String requestBaseServer = getRequestBaseServer(httpServletReuqest);
		
		if (requestBaseServer.equals(configBaseServer) == false) { // mozliwe przekierowanie
			
			String requestURI = httpServletReuqest.getRequestURI();
			
			if (requestURI != null && requestURI.startsWith("/android/") == true) { // wywolan z androida nie przekierowujemy
				chain.doFilter(request, response);
				
				return;
			}			
			
			LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);
			
			String redirectUrl = getRedirectUrl(httpServletReuqest, httpServletResponse, configBaseServer);
			
	        // dodanie do logowania
			RedirectLoggerModel redirectLoggerModel = new RedirectLoggerModel(Utils.createLoggerModelCommon(httpServletReuqest), redirectUrl);
			
			loggerSender.sendLog(redirectLoggerModel);

			httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			httpServletResponse.setHeader("Location", redirectUrl);
	        
	        // zrobienie commit'a
	        response.flushBuffer();
			
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// noop		
	}
	
	private String getRequestBaseServer(HttpServletRequest httpServletReuqest) {
		
		int serverPort = httpServletReuqest.getServerPort();
		
		String requestBaseServer = httpServletReuqest.getScheme() + "://" + httpServletReuqest.getServerName() + (serverPort != 80 && serverPort != 443 ? (":" + serverPort) : "");
		
		return requestBaseServer;
	}
	
	private String getRedirectUrl(HttpServletRequest httpServletReuqest, HttpServletResponse httpServletResponse, String configBaseServer) {
		
        String queryString = httpServletReuqest.getQueryString();
        String redirectUrl = httpServletReuqest.getRequestURI() + ((queryString == null) ? "" : ("?" + queryString));

        String fullRedirectUrl = configBaseServer + redirectUrl;		
		
		return httpServletResponse.encodeRedirectURL(fullRedirectUrl);
	}
}
