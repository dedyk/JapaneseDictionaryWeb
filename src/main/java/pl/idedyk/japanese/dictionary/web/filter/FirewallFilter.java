package pl.idedyk.japanese.dictionary.web.filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
import pl.idedyk.japanese.dictionary.web.service.ConfigService;

public class FirewallFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(FirewallFilter.class);
		
	private File hostBlockFile = null;
	private Long hostBlockFileLastModified = null;

	private List<String> hostBlockRegexList = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}
	
	private synchronized void checkAndReloadHostBlockFile() {
		
		if (hostBlockFile == null) {
			hostBlockFile = new File(ConfigService.getCatalinaConfDirStatic(), "configService.hostBlock");
		}
		
		// nie ma pliku lub nie mozna go przeczytac
		if (hostBlockFile.exists() == false || hostBlockFile.canRead() == false) {
			
			hostBlockFileLastModified = null;
			hostBlockRegexList = null;
			
			return;
		}
		
		// plik nie zmienil sie
		if (hostBlockFileLastModified != null && hostBlockFileLastModified.longValue() == hostBlockFile.lastModified()) {
			return;
		}
		
		// probujemy wczytac plik
		logger.info("Wczytywanie pliku: " + hostBlockFile);
		
		hostBlockRegexList = new ArrayList<>();
				
		try {
			Scanner scanner = new Scanner(hostBlockFile);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if (line.startsWith("#") == true) {
					continue;
				}
				
				hostBlockRegexList.add(line);
			}
			
			scanner.close();
						
		} catch (Exception e) {
			
			logger.error("Błąd podczas wczytywania pliku: " + hostBlockFile, e);
			
			hostBlockFileLastModified = null;
			hostBlockRegexList = null;
			
			return;			
		}
	}
	
	private synchronized boolean isIpHostBlocked(String ip, String hostName) {
		// sprawdzenie, czy zmienila sie konfiguracja blokowania ip lub nazwy hosta
		checkAndReloadHostBlockFile();

		// sprawdzamy, czy adres ip lub nzwa hosta jest na tej liscie
		if (hostBlockRegexList != null) {
			for (String currentHostBlockMatcher : hostBlockRegexList) {
				
				try {
					if ((ip != null && ip.matches(currentHostBlockMatcher) == true) || (hostName != null && hostName.matches(currentHostBlockMatcher) == true)) {
						return true;
					}					
				} catch (Exception e) {
					logger.error("Błąd podczas sprawdzania adresu ip lub nazwy hosta", e);
				}
			}
		}
		
		return false;
	}


	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		boolean doBlock = false;
		
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;
		
		String ip = Utils.getRemoteIp(httpServletRequest);
		String hostName = Utils.getHostname(ip);
		String userAgent = httpServletRequest.getHeader("User-Agent");	
		
		// sprawdzanie, czy nalezy zablokowac ip/host
		doBlock = isIpHostBlocked(ip, hostName);
		
		if (userAgent != null) {
			
			// sprawdzamy, czy zalezy zablokowac tego user agenta
			if (userAgent.contains("AspiegelBot") == true) {
				doBlock = true;
			}	
		} 
		
		if (doBlock == false) { // normalne wywolanie
			chain.doFilter(request, response);
			
		} else { // blokowanie
			logger.info("Blokowanie ip/host/user agent: " + ip + " / " + hostName + " / " + userAgent);
			
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
