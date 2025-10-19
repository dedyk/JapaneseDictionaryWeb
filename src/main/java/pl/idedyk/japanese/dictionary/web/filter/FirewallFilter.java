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
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock.Address;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock.AddressList;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock.Country;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlock.CountryList;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;
import pl.idedyk.japanese.dictionary.web.service.ConfigService.ConfigWrapper;
import pl.idedyk.japanese.dictionary.web.service.GeoIPService;

public class FirewallFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(FirewallFilter.class);
		
	// zmienne dla blokowania wzorcow wywolan
	private File fullUrlBlockFile = null;
	private Long fullUrlBlockFileLastModified = null;
	
	private List<String> fullUrlBlockRegexList = null;	
	
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
	
	private synchronized GeoIPService getGeoIPService(ServletRequest request) {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		
		GeoIPService geoIPService = webApplicationContext.getBean(GeoIPService.class);
		
		return geoIPService;		
	}
	
	private synchronized ConfigService getConfigService(ServletRequest request) {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		
		ConfigService configService = webApplicationContext.getBean(ConfigService.class);
		
		return configService;		
	}
	
	private synchronized void isIpHostBlocked(ConfigWrapper configWrapper, ClientInfo clientInfo) {
		
		HostBlock hostBlock = configWrapper.getConfig().getFirewall().getHostBlock();
				
		try {			
			// pobranie listy blokad adresow ip i host name
			List<Address> hostBlockAddressList = hostBlock.getAddressList();

			// sprawdzamy, czy adres ip lub nazwa hosta jest na tej liscie
			for (Address address : hostBlockAddressList) {
				if (	(clientInfo.ip != null && clientInfo.ip.matches(address.getValue()) == true) ||
						(clientInfo.hostName != null && clientInfo.hostName.matches(address.getValue()) == true)) {
					
					clientInfo.doBlock = true;
					clientInfo.sendRandomData = address.isRandomDataSend();
					
					return;
				}
			}
			
			// pobranie listy blokowanych krajow
			List<Country> countryList = hostBlock.getCountryList();
			
			for (Country countryToCheck : countryList) {
				// sprawdzenie, czy nalezy blokowac dany kraj
				if (clientInfo.country != null && countryToCheck.getValue().equals(clientInfo.country) == true) {
					
					clientInfo.doBlock = true;
					clientInfo.sendRandomData = countryToCheck.isRandomDataSend();
					
					return;
				}
			}			
			
		} catch (Exception e) {
			logger.error("Błąd podczas sprawdzania adresu ip lub nazwy hosta", e);
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
		
		// FM_FIXME: user agent w konfiguracji
		if (doBlock == false && userAgent != null) {
			// sprawdzamy, czy zalezy zablokowac tego user agenta
			if (userAgent.contains("AspiegelBot") == true || userAgent.contains("RecordedFuture") == true) { // RecordedFuture-ASI
				doBlock = true;
			}
			
			// sprawdzenie, czy mamy do czynienia z robotem, ktory pobiera dane w bardzo agresywny sposob
			// wszystkie te roboty uzywaja user agent Chrome od 60 do 79, np.
			// Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0
			// "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.2759.69 Safari/537.36"
			// "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3403.143 Safari/537.36"
			if (userAgent.matches("^Mozilla\\/5.0 \\(Windows NT \\d+\\.\\d; Win64; x64\\) AppleWebKit\\/537.36 \\(KHTML, like Gecko\\) Chrome\\/[6-7][0-9].*$") == true) {
				doBlock = true;
				doBlockSendRandomData = true;
			}
		}
				
		// sprawdzanie, czy nalezy zablokowac ip/host
		if (clientInfo.doBlock == false) {
			isIpHostBlocked(configWrapper, clientInfo);
		}
						
		// sprawdzenie, czy dany fullURL nalezy zablokowac
		if (clientInfo.doBlock == false) {
			doBlock = isFullUrlBlocked(fullUrl);			
		}
		
		// dodatkowe sprawdzenie, czy wywolanie nie pochodzi z aplikacji na Androida, jesli tak to pozwalamy na nie
		if (clientInfo.doBlock == true && httpMethod != null && httpMethod.equals("POST") == true && url.startsWith("/android/") == true && userAgent != null && userAgent.startsWith("JapaneseAndroidLearnHelper/") == true) {
			doBlock = false;
		}

		// dostep do pliku robots.txt jest dozwolony
		if (clientInfo.doBlock == true && httpMethod != null && httpMethod.equals("GET") == true && url.equals("/robots.txt") == true) {
			doBlock = false;
		}
		
		if (doBlock == true) { // blokowanie
			
			if (doBlockSendRandomData == false) { // zwykla blokada
				logger.info("Blokowanie ip/host/user agent/url: " + ip + " (" + country + ") / " + hostName + " / " + userAgent + " / " + fullUrl);
				
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
				logger.info("Blokowanie ip/host/user agent/url i wysylanie losowych danych: " + ip + " (" + country + ") / " + hostName + " / " + userAgent + " / " + fullUrl);
				
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
	
	private synchronized void checkAndReloadFullUrlBlockFile() {
		
		if (fullUrlBlockFile == null) {
			fullUrlBlockFile = new File(ConfigService.getCatalinaConfDirStatic(), "configService.fullUrlBlock");
		}
		
		// nie ma pliku lub nie mozna go przeczytac
		if (fullUrlBlockFile.exists() == false || fullUrlBlockFile.canRead() == false) {
			
			fullUrlBlockFileLastModified = null;
			fullUrlBlockRegexList = null;
			
			return;
		}
		
		// plik nie zmienil sie
		if (fullUrlBlockFileLastModified != null && fullUrlBlockFileLastModified.longValue() == fullUrlBlockFile.lastModified()) {
			return;
		}
		
		// probujemy wczytac plik
		logger.info("Wczytywanie pliku: " + fullUrlBlockFile);
		
		fullUrlBlockRegexList = new ArrayList<>();
				
		try {
			Scanner scanner = new Scanner(fullUrlBlockFile);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if (line.startsWith("#") == true) {
					continue;
				}
				
				fullUrlBlockRegexList.add(line);
			}
			
			scanner.close();
			
			fullUrlBlockFileLastModified = fullUrlBlockFile.lastModified();
						
		} catch (Exception e) {
			
			logger.error("Błąd podczas wczytywania pliku: " + fullUrlBlockFile, e);
			
			fullUrlBlockFileLastModified = null;
			fullUrlBlockRegexList = null;
			
			return;			
		}
	}

	
	private boolean isFullUrlBlocked(String fullUrl) {
		// sprawdzenie, czy zmienila sie konfiguracja blokowania ip lub nazwy hosta
		checkAndReloadFullUrlBlockFile();

		// sprawdzamy, czy adres ip lub nzwa hosta jest na tej liscie
		if (fullUrlBlockRegexList != null) {
			for (String currentFullUrlMatcher : fullUrlBlockRegexList) {
				
				try {
					if (fullUrl != null && fullUrl.matches(currentFullUrlMatcher) == true) {
						return true;
					}					
				} catch (Exception e) {
					logger.error("Błąd podczas sprawdzania blokady full url", e);
				}
			}
		}
		
		return false;
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
		private boolean sendRandomData;
		
		
		
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
