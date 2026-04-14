package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RedirectFilter extends RedirectCommonFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(RedirectFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		ServletContext servletContext = request.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

		Properties applicationProperties = (Properties)webApplicationContext.getBean("applicationProperties");
				
		String newServerName = applicationProperties.getProperty("new.server.name");
		
		if (newServerName == null || newServerName.trim().equals("") == true) { // gdy nazwa nie ustawiona, idziemy dalej
			chain.doFilter(request, response);
			
			return;
		}
		
		//
		
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;
		
		// wywolan z androida nie przekierowujemy
		String requestURI = httpServletRequest.getRequestURI();
		
		if (requestURI != null && requestURI.startsWith("/android/") == true) { 
			chain.doFilter(request, response);
			
			return;
		}
		
		// pobranie server name
		String serverName = httpServletRequest.getServerName();
		
		if (serverName.equals(newServerName) == true) { // ta sama nazwa, nic nie robimy
			chain.doFilter(request, response);
			
			return;
		}

		// przekierowanie na nowy adres serwera
		String redirectUrl = getRedirectNewServerUrl(httpServletRequest, httpServletResponse, newServerName);

		redirectToUrl(webApplicationContext, httpServletRequest, httpServletResponse, redirectUrl);		
	}

	@Override
	public void destroy() {
		// noop		
	}
		
	private String getRedirectNewServerUrl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String newServerName) {
		
        String queryString = httpServletRequest.getQueryString();
        String redirectUrl = httpServletRequest.getRequestURI() + ((queryString == null) ? "" : ("?" + queryString));
        
        int serverPort = httpServletRequest.getServerPort();
        
        //
        
		String scheme = httpServletRequest.getScheme();
		
    	String xForwardedProto = httpServletRequest.getHeader("x-forwarded-proto");
    	String xForwardedPort = httpServletRequest.getHeader("x-forwarded-port");
    	
    	if (xForwardedProto != null) {
    		scheme = xForwardedProto;
    	}
    	
    	if (xForwardedPort != null) {
    		try {
    			serverPort = Integer.parseInt(xForwardedPort);
    			
    		} catch (NumberFormatException e) {
    			logger.error("Can't parse x-forwarded-port", e);
    		}
    	}
    	
        String fullRedirectUrl = scheme + "://" + newServerName + (serverPort != 80 && serverPort != 443 ? (":" + serverPort) : "") + redirectUrl;	
		
		return httpServletResponse.encodeRedirectURL(fullRedirectUrl);
	}
}
