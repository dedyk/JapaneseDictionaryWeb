package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.idedyk.japanese.dictionary.web.common.ClientInfo;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlockList;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.HostBlockList.HostBlock;
import pl.idedyk.japanese.dictionary.web.config.xsd.HostBlockOperation;
import pl.idedyk.japanese.dictionary.web.controller.CaptchaController;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.ClientBlockLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RedirectToCatchaLoggerModel;
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
	
	private void isClientBlocked(ConfigWrapper configWrapper, ClientInfo clientInfo) {
		
		List<HostBlockList.HostBlock> hostBlockListList = configWrapper.getConfig().getFirewall().getHostBlockList().getHostBlock();		
		List<HostBlockList.HostBlock> matchedHostBlockList = new ArrayList<>(); // lista dopasowanych konfiguracji
		
		for (HostBlockList.HostBlock hostBlock : hostBlockListList) {

			// jezeli w danym host block wystepuje dany typ warunku to wszystkie one musza byc spelnione
			
			int numberOfCheckedConditions = 0; // liczba wykonanych sprawdzen	| aba warunki musza
			int numberOfSatisfiedConditions = 0; // liczba spelnionych warunkow	| byc wieksze od siebie i byc rowne sobie
			
			// sprawdzanie kraju
			List<HostBlockList.HostBlock.Country> countryList = hostBlock.getCountry();
			
			if (countryList.size() > 0) {
				numberOfCheckedConditions++;
								
				for (HostBlockList.HostBlock.Country country : countryList) {
					
					if (clientInfo.country != null && country.getValue().equals(clientInfo.country) == true) {
						numberOfSatisfiedConditions++;
						break;
					}
				}	
			}
			
			// sprawdzenie ASN
			List<HostBlockList.HostBlock.Asn> asnList = hostBlock.getAsn();
			
			if (asnList.size() > 0) {
				numberOfCheckedConditions++;
				
				for (HostBlockList.HostBlock.Asn asn : asnList) {
					
					if (clientInfo.autonomousSystemNumber != null && asn.getValue().equals(clientInfo.autonomousSystemNumber) == true) {
						numberOfSatisfiedConditions++;
						break;
					}
				}
			}
			
			// sprawdzenie adresu i host name
			List<HostBlockList.HostBlock.Address> addressList = hostBlock.getAddress();
			
			if (addressList.size() > 0) {
				numberOfCheckedConditions++;
				
				for (HostBlockList.HostBlock.Address address : addressList) {
					
					if (	(clientInfo.ip != null && clientInfo.ip.matches(address.getValue()) == true) ||
							(clientInfo.hostName != null && clientInfo.hostName.matches(address.getValue()) == true)) {
						
						numberOfSatisfiedConditions++;
						break;
					}
				}
			}
			
			// sprawdzenie, czy adres i host name nie jest jednym z (wszystkie warunki musza byc spelnione)
			List<HostBlockList.HostBlock.NotAddress> notAddressList = hostBlock.getNotAddress();
			
			if (notAddressList.size() > 0) {
				numberOfCheckedConditions++;
				
				int notAddressSatisfiedConditions = 0;
				
				for (HostBlockList.HostBlock.NotAddress notAddress : notAddressList) {
					
					if (	(clientInfo.ip != null && clientInfo.ip.matches(notAddress.getValue()) == false) &&
							(clientInfo.hostName != null && clientInfo.hostName.matches(notAddress.getValue()) == false)) {
						
						notAddressSatisfiedConditions++;
					}
				}
				
				if (notAddressSatisfiedConditions == notAddressList.size()) { // czy warunek spelniony, nic nie moze pasowac
					numberOfSatisfiedConditions++;
				}
			}			
			
			// sprawdzenie user agent
			List<HostBlockList.HostBlock.UserAgent> userAgentList = hostBlock.getUserAgent();
			
			if (userAgentList.size() > 0) {
				numberOfCheckedConditions++;
				
				if (clientInfo.userAgent != null) {					
					for (HostBlockList.HostBlock.UserAgent userAgent : userAgentList) {
						
						if (clientInfo.userAgent.matches(userAgent.getValue()) == true) {							
							numberOfSatisfiedConditions++;
							break;						
						}
					}
				}
			}
			
			// sprawdzenie adresu wywolania
			List<HostBlockList.HostBlock.FullUrl> fullUrlList = hostBlock.getFullUrl();
			
			if (fullUrlList.size() > 0) {
				numberOfCheckedConditions++;
				
				for (HostBlockList.HostBlock.FullUrl fullUrl : fullUrlList) {
					
					if (clientInfo.fullUrl.matches(fullUrl.getValue()) == true) {
						numberOfSatisfiedConditions++;
						break;
					}
				}
			}
			
			// sprawdzenie, czy wszystkie ustawione warunki zostaly spelnione
			if (numberOfCheckedConditions > 0 && numberOfCheckedConditions == numberOfSatisfiedConditions) {
				matchedHostBlockList.add(hostBlock);
			}
		}
		
		if (matchedHostBlockList.size() > 0) { // mamy cos dopasowane
			
			// sprawdzenie, ktore typy operacji wystepuja
			HostBlock blockHostBlock = matchedHostBlockList.stream().filter(f -> f.getOperation() == HostBlockOperation.BLOCK).findFirst().orElse(null);
			HostBlock sendRandomDataHostBlock = matchedHostBlockList.stream().filter(f -> f.getOperation() == HostBlockOperation.SEND_RANDOM_DATA).findFirst().orElse(null);
			HostBlock redirectToCaptchaHostBlock = matchedHostBlockList.stream().filter(f -> f.getOperation() == HostBlockOperation.REDIRECT_TO_CAPTCHA).findFirst().orElse(null);
			
			HostBlock hostBlockToUse = null;
			
			if (blockHostBlock != null) {
				hostBlockToUse = blockHostBlock;
				
			} else if (sendRandomDataHostBlock != null) {
				hostBlockToUse = sendRandomDataHostBlock;
				
			} else if (redirectToCaptchaHostBlock != null) {
				hostBlockToUse = redirectToCaptchaHostBlock;
				
			}
			
			if (hostBlockToUse != null) {
				clientInfo.hostBlockOperation = hostBlockToUse.getOperation();
				clientInfo.doSendToLoggerListener = hostBlockToUse.isSendToLoggerListener();
			}			
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
		clientInfo.autonomousSystemNumber = null;
		
		try {
			// pobranie kraju na podstawie adresu ip
			if (geoIPService != null && clientInfo.ip != null) {
				clientInfo.country = geoIPService.getCountry(clientInfo.ip);
				clientInfo.autonomousSystemNumber = geoIPService.getAutonomousSystemNumber(clientInfo.ip);
				clientInfo.autonomousSystemOrganization = geoIPService.getAutonomousSystemOrganization(clientInfo.ip);
			}
		} catch (Exception e) {
			logger.error("Błąd podczas pobierania nazwy kraju z adresu ip", e);
		}
		
		// zapisanie clientInfo do request-a
		request.setAttribute(ClientInfo.REQUEST_ATTRIBUTE, clientInfo);
		
		// sprawdzenie, czy zablokowac danego clientInfo
		isClientBlocked(configWrapper, clientInfo);
				
		// dodatkowe sprawdzenie, czy wywolanie nie pochodzi z aplikacji na Androida, jesli tak to pozwalamy na nie
		if (	clientInfo.hostBlockOperation != null && clientInfo.httpMethod != null && clientInfo.httpMethod.equals("POST") == true && 
				clientInfo.url.startsWith("/android/") == true && clientInfo.userAgent != null && clientInfo.userAgent.startsWith("JapaneseAndroidLearnHelper/") == true) {
			clientInfo.hostBlockOperation = null;
		}
		
		// sprawdzenie, czy wykonujemy pokazanie captcha
		if (clientInfo.hostBlockOperation == HostBlockOperation.REDIRECT_TO_CAPTCHA && 
				(
						clientInfo.url.startsWith(CaptchaController.CAPTCHA_URL_PREFIX) == true ||
						clientInfo.url.startsWith("/img/") == true ||
						clientInfo.url.startsWith("/css/") == true ||
						clientInfo.url.startsWith("/js/") == true)
				) {
			clientInfo.hostBlockOperation = null;
		}
		
		// sprawdzenie, czy uzytkownik zostal juz zweryfikowany przez captcha
		if (clientInfo.hostBlockOperation == HostBlockOperation.REDIRECT_TO_CAPTCHA &&
				httpServletRequest.getSession().getAttribute(CaptchaController.CAPTCHA_SESSION_CAPTCHA_VERIFIED) != null) {
			
			clientInfo.hostBlockOperation = null;
		}

		/*
		// dostep do pliku robots.txt jest dozwolony
		if (clientInfo.doBlock == true && clientInfo.httpMethod != null && clientInfo.httpMethod.equals("GET") == true && clientInfo.url.equals("/robots.txt") == true) {
			clientInfo.doBlock = false;
		}
		*/
		
		if (clientInfo.hostBlockOperation != null) { // jakas operacja blokowania
			
			// czy wyslac do logow
			if (Arrays.asList(HostBlockOperation.BLOCK, HostBlockOperation.SEND_RANDOM_DATA).contains(clientInfo.hostBlockOperation) == true && clientInfo.doSendToLoggerListener == true) {
				ServletContext servletContext = request.getServletContext();
				WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
				
				LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);
				
				ClientBlockLoggerModel clientBlockLoggerModel = new ClientBlockLoggerModel(Utils.createLoggerModelCommon(httpServletRequest));
				
				loggerSender.sendLog(clientBlockLoggerModel);
				
			} else if (clientInfo.hostBlockOperation == HostBlockOperation.REDIRECT_TO_CAPTCHA && clientInfo.doSendToLoggerListener == true) {
				ServletContext servletContext = request.getServletContext();
				WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
				
				// zapisanie do sesji adresu, na ktory wchodzil uzytkownik
				httpServletRequest.getSession().setAttribute(CaptchaController.CAPTCHA_SESSION_USER_URL, clientInfo.fullUrl);
				
				// logger
				LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);
				
				RedirectToCatchaLoggerModel redirectToCatchaLoggerModel = new RedirectToCatchaLoggerModel(Utils.createLoggerModelCommon(httpServletRequest));
				
				loggerSender.sendLog(redirectToCatchaLoggerModel);	
			}
			
			if (clientInfo.hostBlockOperation == HostBlockOperation.BLOCK) { // zwykla blokada
				logger.info("Blokowanie ip/host/user agent/url: " + clientInfo.ip + " (" + clientInfo.autonomousSystemNumber + ", " + clientInfo.country + ") / " + clientInfo.hostName + " / " + clientInfo.userAgent + " / " + clientInfo.fullUrl);
								
				// wysylamy brak dostepu
				httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
				
			} else if (clientInfo.hostBlockOperation == HostBlockOperation.SEND_RANDOM_DATA) { // wysylanie losowych danych
				logger.info("Blokowanie ip/host/user agent/url i wysylanie losowych danych: " + clientInfo.ip + " (" + clientInfo.autonomousSystemNumber + ", " + clientInfo.country + ") / " + clientInfo.hostName + " / " + clientInfo.userAgent + " / " + clientInfo.fullUrl);
				
				// tworzenie generatora losowych stringow
				@SuppressWarnings("deprecation")
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
				
			} else if (clientInfo.hostBlockOperation == HostBlockOperation.REDIRECT_TO_CAPTCHA) { // przekierowanie do weryfikacji captcha
								
				httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
				httpServletResponse.setHeader("Location", CaptchaController.CAPTCHA_URL_START);
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
