package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock.Address;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock.Country;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock.FullUrl;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock.UserAgent;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;
import pl.idedyk.japanese.dictionary.web.service.ConfigService.ConfigWrapper;
import pl.idedyk.japanese.dictionary.web.service.GeoIPService;

public class FirewallFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(FirewallFilter.class);
		
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
	
	private GeoIPService getGeoIPService(ServletRequest request) {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		
		GeoIPService geoIPService = webApplicationContext.getBean(GeoIPService.class);
		
		return geoIPService;		
	}
	
	private ConfigService getConfigService(ServletRequest request) {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		
		ConfigService configService = webApplicationContext.getBean(ConfigService.class);
		
		return configService;		
	}
	
	private void isIpHostBlocked(ConfigWrapper configWrapper, ClientInfo clientInfo) {
		
		HostBlock hostBlock = configWrapper.getConfig().getFirewall().getHostBlock();
				
		try {			
			// pobranie listy blokad adresow ip i host name
			List<Address> hostBlockAddressList = hostBlock.getAddressList();

			// sprawdzamy, czy adres ip lub nazwa hosta jest na tej liscie
			for (Address address : hostBlockAddressList) {
				if (	(clientInfo.ip != null && clientInfo.ip.matches(address.getValue()) == true) ||
						(clientInfo.hostName != null && clientInfo.hostName.matches(address.getValue()) == true)) {
					
					clientInfo.doBlock = true;
					clientInfo.doBlockSendRandomData = address.isRandomDataSend();
					
					return;
				}
			}
			
			// pobranie listy blokowanych krajow
			List<Country> countryList = hostBlock.getCountryList();
			
			for (Country country : countryList) {
				// sprawdzenie, czy nalezy blokowac dany kraj
				if (clientInfo.country != null && country.getValue().equals(clientInfo.country) == true) {
					
					clientInfo.doBlock = true;
					clientInfo.doBlockSendRandomData = country.isRandomDataSend();
					
					return;
				}
			}			
			
		} catch (Exception e) {
			logger.error("Błąd podczas sprawdzania adresu ip lub nazwy hosta", e);
		}
	}
	
	private void isUserAgentBlocked(ConfigWrapper configWrapper, ClientInfo clientInfo) {
		
		if (clientInfo.userAgent == null) {
			return;
		}
		
		HostBlock hostBlock = configWrapper.getConfig().getFirewall().getHostBlock();
				
		try {			
			// pobranie listy blokowanych user agent
			List<UserAgent> hostBlockUserAgentList = hostBlock.getUserAgentList();

			// sprawdzamy, czy user agent jest na tej liscie
			for (UserAgent userAgent : hostBlockUserAgentList) {
				
				if (userAgent.getValue().matches(clientInfo.userAgent) == true) {
					
					clientInfo.doBlock = true;
					clientInfo.doBlockSendRandomData = userAgent.isRandomDataSend();
					
					return;						
				}
			}
						
		} catch (Exception e) {
			logger.error("Błąd podczas sprawdzania adresu user agent-a", e);
		}
	}
	
	private void isFullUrlBlocked(ConfigWrapper configWrapper, ClientInfo clientInfo) {
		
		if (clientInfo.fullUrl == null) {
			return;
		}
		
		HostBlock hostBlock = configWrapper.getConfig().getFirewall().getHostBlock();
		
		try {
			List<FullUrl> fullUrlList = hostBlock.getFullUrlList();
			
			// sprawdzanie, czy dane wywolanie jest zablokowane
			for (FullUrl fullUrl : fullUrlList) {
				
				if (fullUrl.getValue().matches(clientInfo.fullUrl) == true) {
					
					clientInfo.doBlock = true;
					clientInfo.doBlockSendRandomData = fullUrl.isRandomDataSend();
					
					return;
				}
			}
			
		} catch (Exception e) {
			logger.error("Błąd podczas sprawdzania blokady full url", e);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
				
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;
		
		GeoIPService geoIPService = getGeoIPService(request);
		ConfigService configService = getConfigService(httpServletRequest);
		
		// pobranie konfiguracji
		ConfigWrapper configWrapper = configService.getConfig();
		
		// utworzenie informacji o kliencie
		ClientInfo clientInfo = new ClientInfo();
		
		clientInfo.ip = Utils.getRemoteIp(httpServletRequest);
		clientInfo.hostName = Utils.getHostname(clientInfo.ip);
		clientInfo.userAgent = httpServletRequest.getHeader("User-Agent");	
		clientInfo.url = httpServletRequest.getRequestURI();
		clientInfo.httpMethod = httpServletRequest.getMethod();
		
		clientInfo.fullUrl = Utils.getRequestURL(httpServletRequest);
		
		clientInfo.country = null;
		
		try {
			// pobranie kraju na podstawie adresu ip
			if (geoIPService != null && clientInfo.ip != null) {
				clientInfo.country = geoIPService.getCountry(clientInfo.ip);
			}
		} catch (Exception e) {
			logger.error("Błąd podczas pobierania nazwy kraju z adresu ip", e);
		}
						
		// sprawdzanie, czy nalezy zablokowac ip/host
		if (clientInfo.doBlock == false) {
			isIpHostBlocked(configWrapper, clientInfo);
		}
		
		// sprawdzenie, czy nalezy zablokowac po userAgent
		if (clientInfo.doBlock == false) {
			isUserAgentBlocked(configWrapper, clientInfo);
		}
						
		// sprawdzenie, czy dany fullURL nalezy zablokowac
		if (clientInfo.doBlock == false) {
			isFullUrlBlocked(configWrapper, clientInfo);			
		}
		
		// dodatkowe sprawdzenie, czy wywolanie nie pochodzi z aplikacji na Androida, jesli tak to pozwalamy na nie
		if (	clientInfo.doBlock == true && clientInfo.httpMethod != null && clientInfo.httpMethod.equals("POST") == true && 
				clientInfo.url.startsWith("/android/") == true && clientInfo.userAgent != null && clientInfo.userAgent.startsWith("JapaneseAndroidLearnHelper/") == true) {
			clientInfo.doBlock = false;
		}

		// dostep do pliku robots.txt jest dozwolony
		if (clientInfo.doBlock == true && clientInfo.httpMethod != null && clientInfo.httpMethod.equals("GET") == true && clientInfo.url.equals("/robots.txt") == true) {
			clientInfo.doBlock = false;
		}
		
		if (clientInfo.doBlock == true) { // blokowanie
			
			if (clientInfo.doBlockSendRandomData == false) { // zwykla blokada
				logger.info("Blokowanie ip/host/user agent/url: " + clientInfo.ip + " (" + clientInfo.country + ") / " + clientInfo.hostName + " / " + clientInfo.userAgent + " / " + clientInfo.fullUrl);
				
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
				
			} else { // wysylanie losowych danych
				logger.info("Blokowanie ip/host/user agent/url i wysylanie losowych danych: " + clientInfo.ip + " (" + clientInfo.country + ") / " + clientInfo.hostName + " / " + clientInfo.userAgent + " / " + clientInfo.fullUrl);
				
				// tworzenie generatora losowych stringow
				RandomStringGenerator generator = new RandomStringGenerator.Builder()
					     .withinRange('a', 'z').build();
				
				final String template =	"<!doctype html>\n"
						+ "<html>\n"
						+ "  <head>\n"
						+ "    <title>%s %s %s</title>\n"
						+ "  </head>\n"
						+ "  <body>\n"
						+ "    <p>%s %s %s.</p>\n"
						+ "  </body>\n"
						+ "</html>";
				
				String randomHtmlDoc = String.format(template, generator.generate(10), generator.generate(12), generator.generate(14),
						generator.generate(8), generator.generate(16), generator.generate(18));
				
				httpServletResponse.setStatus(HttpServletResponse.SC_OK);
				httpServletResponse.setHeader("Content-Type", "text/html;charset=UTF-8");
								
				httpServletResponse.getOutputStream().write(randomHtmlDoc.getBytes());
			}
		        
	        // zrobienie commit'a
	        response.flushBuffer();
	        
	        return;
		}
		
		// sprawdzanie, czy ten klient nie przekracza limitu jednoczesnych wywolan
		IsClientRateExceededResult isClientRateExceededResult = isClientRateExceeded(clientInfo.ip, clientInfo.hostName, clientInfo.url);
				
		if (isClientRateExceededResult.isClientRateExceeded == true) { // przekroczono liczbe wywolan
			logger.info("Przekroczono liczbę jednoczesnych wywolan, ip {}, host name: {}, user agent: {}, url: {}, call rate: {} ",
					clientInfo.ip, clientInfo.hostName, clientInfo.userAgent, clientInfo.url, isClientRateExceededResult.callRate);
			
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
	
	private static class ClientInfo {
		private String ip;
		private String hostName;
		private String userAgent;	
		private String url;
		private String httpMethod;
		
		private String fullUrl;
		
		private String country;

		private boolean doBlock;
		private boolean doBlockSendRandomData;
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
