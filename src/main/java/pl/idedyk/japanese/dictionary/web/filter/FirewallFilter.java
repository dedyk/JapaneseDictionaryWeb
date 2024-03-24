package pl.idedyk.japanese.dictionary.web.filter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;

public class FirewallFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(FirewallFilter.class);
		
	private File hostBlockFile = null;
	private Long hostBlockFileLastModified = null;

	private List<String> hostBlockRegexList = null;
	
	// parametry do sprawdzania limitu wywolan
	private static final int CLIENT_RATE_REMEMBER_SECONDS = 180;
	private static final int CLIENT_RATE_REMEMBER_CALLS_SECONDS = 20;
	private static final int CLIENT_RATE_MIN_ENLISTMENT_TIME = 10;
	private static final float CLIENT_RATE_THRESHOLD = 5.0f;
	private static final String[] CLIENT_RATE_URL_FILTER = new String[] {
			"/android/", "/wordDictionary/autocomplete", "/kanjiDictionary/autocomplete"
	};
	
	private PassiveExpiringMap<String, ClientIP> clientRateMemoryMap = new PassiveExpiringMap<>(CLIENT_RATE_REMEMBER_SECONDS, TimeUnit.SECONDS);

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
			
			hostBlockFileLastModified = hostBlockFile.lastModified();
						
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
		String url = httpServletRequest.getRequestURI();
		
		// sprawdzanie, czy nalezy zablokowac ip/host
		doBlock = isIpHostBlocked(ip, hostName);
		
		if (userAgent != null) {
			
			// sprawdzamy, czy zalezy zablokowac tego user agenta
			if (userAgent.contains("AspiegelBot") == true) {
				doBlock = true;
			}	
		} 
		
		if (doBlock == true) { // blokowanie
			logger.info("Blokowanie ip/host/user agent: " + ip + " / " + hostName + " / " + userAgent);
			
			// ServletContext servletContext = request.getServletContext();
			
			// WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			
			// wysylacz
			// LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);
			
			// nie bedziemy wysylac zdarzen, gdyz moze to powodowac bardzo duza liczbe zdarzen, ktore pozniej musza byc przetworzone
			// ClientBlockLoggerModel clientBlockLoggerModel = new ClientBlockLoggerModel(Utils.createLoggerModelCommon(httpServletRequest));
			// loggerSender.sendLog(clientBlockLoggerModel);
			
			//
			
			// wysylamy brak dostepu
			httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
	        
	        // zrobienie commit'a
	        response.flushBuffer();
	        
	        return;
		}
		
		// sprawdzanie, czy ten klient nie przekracza limitu jednoczesnych wywolan
		IsClientRateExceededResult isClientRateExceededResult = isClientRateExceeded(ip, hostName, url);
				
		if (isClientRateExceededResult.isClientRateExceeded == true) { // przekroczono liczbe wywolan
			logger.info("Przekroczono liczbę jednoczesnych wywolan, ip {}, host name: {}, user agent: {}, url: {}, call rate: {} ",
					ip, hostName, userAgent, url, isClientRateExceededResult.callRate);
			
			// wysylamy brak dostepu
			httpServletResponse.setStatus(429);
	        
	        // zrobienie commit'a
	        response.flushBuffer();
	        
	        return;
	        
		} else {
			//logger.info("Lczbę jednoczesnych wywolan, ip {}, host name: {}, user agent: {}, url: {}, call rate: {} ",
			//		ip, hostName, userAgent, url, isClientRateExceededResult.callRate);
		}
		
		// normalne wywolanie		
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// noop
	}
	
	private IsClientRateExceededResult isClientRateExceeded(String ip, String hostName, String url) {
				
		ClientIP clientIP;
		
		synchronized (clientRateMemoryMap) {
			// szukamy klienta
			clientIP = clientRateMemoryMap.get(ip);
			
			// nie znaleziono, wiec tworzymy
			if (clientIP == null) {
				clientIP = new ClientIP(ip, hostName);
			}
			
			// dodajemy raz jeszcze, aby zaktualizowac date wygasniecia
			clientRateMemoryMap.put(ip, clientIP);
		}
		
		// dodajemy do klienta to wywolanie i sprawdzamy, czy nie przekroczylismy limitu		
		return clientIP.addClientCall(url);
	}
	
	private static class ClientIP {
		@SuppressWarnings("unused")
		private String ip;
		
		@SuppressWarnings("unused")
		private String hostName;
				
		private List<CallInfo> callList = new LinkedList<>();
		
		public ClientIP(String ip, String hostName) {
			this.ip = ip;
			this.hostName = hostName;
		}
		
		public synchronized IsClientRateExceededResult addClientCall(String url) {
			
			// czy wywolanie jest filtrowane
			boolean isFiltered = false;
			
			for (String currentUrlFilter : CLIENT_RATE_URL_FILTER) {
				if (url.startsWith(currentUrlFilter) == true) {
					isFiltered = true;
				}
			}
			
			// dodajemy to wywolanie
			callList.add(new CallInfo(url, LocalDateTime.now(), isFiltered));
			
			Iterator<CallInfo> callListIterator = callList.iterator();
			
			int callNumbers = 0;
			
			LocalDateTime firstLocalDateTime = null;
			LocalDateTime lastLocalDateTime = null;
			
			while (callListIterator.hasNext()) {
				CallInfo callInfo = callListIterator.next();
				
				LocalDateTime callTimestamp = callInfo.timestamp;
				
				if (callTimestamp.plusSeconds(CLIENT_RATE_REMEMBER_CALLS_SECONDS).isBefore(LocalDateTime.now()) == true) { // usuwamy stare wpisy
					callListIterator.remove();
					continue;
				}
				
				if (callInfo.isFiltered == true) {
					continue;
				}
				
				callNumbers++;
				
				if (firstLocalDateTime == null) {
					firstLocalDateTime = callTimestamp;
				}
				
				lastLocalDateTime = callTimestamp;
			}
			
			// za malo danych lub jest tylko jeden wpis
			if (firstLocalDateTime == null || lastLocalDateTime == null || firstLocalDateTime == lastLocalDateTime) {
				return new IsClientRateExceededResult(false, Float.NaN);
			}
			
			// wyliczamy liczbe sekund miedzy pierwszym, a ostatnim wywolaniem
			long secondsBetweenStartAndLastDateTime = ChronoUnit.SECONDS.between(firstLocalDateTime, lastLocalDateTime);
			
			if (secondsBetweenStartAndLastDateTime < CLIENT_RATE_MIN_ENLISTMENT_TIME) { // sprawdzamy, czy mamy minimalny czas pozyskiwania danych
				return new IsClientRateExceededResult(false, Float.NaN);
			}
			
			// wyliczamy liczbe wywolan na sekunde
			float callRate = (float)callNumbers / (float)secondsBetweenStartAndLastDateTime;
						
			// czy przekroczono limit
			if (callRate >= CLIENT_RATE_THRESHOLD) {
				return new IsClientRateExceededResult(true, callRate);
				
			} else {
				return new IsClientRateExceededResult(false, callRate);
			}
		}
	}
	
	private static class CallInfo {
		@SuppressWarnings("unused")
		private String url;
		private LocalDateTime timestamp;
		private boolean isFiltered;
		
		public CallInfo(String url, LocalDateTime timestamp, boolean isFiltered) {
			this.url = url;
			this.timestamp = timestamp;
			this.isFiltered = isFiltered;
		}
	}
	
	private static class IsClientRateExceededResult {
		private boolean isClientRateExceeded;
		private float callRate;
		
		public IsClientRateExceededResult(boolean isClientRateExceeded, float callRate) {
			this.isClientRateExceeded = isClientRateExceeded;
			this.callRate = callRate;
		}
	}
}
