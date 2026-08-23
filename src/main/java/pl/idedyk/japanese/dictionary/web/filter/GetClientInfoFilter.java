package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import pl.idedyk.japanese.dictionary.web.common.ClientInfo;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.service.BlacklistManager;
import pl.idedyk.japanese.dictionary.web.service.GeoIPService;

public class GetClientInfoFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(GetClientInfoFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		
		GeoIPService geoIPService = getGeoIPService(request);
		BlacklistManager blacklistManager = getBlacklistManager(httpServletRequest);
		
		// utworzenie informacji o kliencie
		ClientInfo clientInfo = new ClientInfo();
		
		clientInfo.ip = Utils.getRemoteIp(httpServletRequest);
		clientInfo.hostName = Utils.getHostname(clientInfo.ip);
		clientInfo.userAgent = httpServletRequest.getHeader("User-Agent");	
		clientInfo.url = httpServletRequest.getRequestURI();
		clientInfo.httpMethod = httpServletRequest.getMethod();
		
		clientInfo.fullUrl = Utils.getRequestURL(httpServletRequest);
		
		clientInfo.country = null;
		clientInfo.autonomousSystemNumber = null;
		
		clientInfo.blackListLevel = null;
		
		try {
			// pobranie kraju na podstawie adresu ip
			if (geoIPService != null && clientInfo.ip != null) {
				clientInfo.country = geoIPService.getCountry(clientInfo.ip);
				clientInfo.autonomousSystemNumber = geoIPService.getAutonomousSystemNumber(clientInfo.ip);
				clientInfo.autonomousSystemOrganization = geoIPService.getAutonomousSystemOrganization(clientInfo.ip);
				
				clientInfo.blackListLevel = blacklistManager.getBlackListLevel(clientInfo.ip);
				
				if (clientInfo.blackListLevel != null) {
					logger.info("Znaleziono adres na czarnej liście: " + clientInfo.ip + ", poziom: " + clientInfo.blackListLevel);
				}
			}
		} catch (Exception e) {
			logger.error("Błąd podczas pobierania nazwy kraju z adresu ip", e);
		}
		
		// zapisanie clientInfo do request-a
		request.setAttribute(ClientInfo.REQUEST_ATTRIBUTE, clientInfo);
		
		// idziemy dalej
		chain.doFilter(request, response);
	}
	
	private GeoIPService getGeoIPService(ServletRequest request) {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		
		GeoIPService geoIPService = webApplicationContext.getBean(GeoIPService.class);
		
		return geoIPService;		
	}
	
	private BlacklistManager getBlacklistManager(ServletRequest request) {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		
		BlacklistManager blacklistManager = webApplicationContext.getBean(BlacklistManager.class);
		
		return blacklistManager;		
	}

}
