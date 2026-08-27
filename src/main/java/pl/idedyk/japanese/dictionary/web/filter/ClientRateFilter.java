package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.map.PassiveExpiringMap.ExpirationPolicy;
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
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Firewall.ClientRateExceeded;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.ClientBlockLoggerModel;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;
import pl.idedyk.japanese.dictionary.web.service.ConfigService.ConfigWrapper;
import pl.idedyk.japanese.dictionary.web.taglib.utils.PassiveExpiringMapWithAutoClearExpiredObjects;

public class ClientRateFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(ClientRateFilter.class);
	
	// parametry do sprawdzania limitu wywolan - przeniesione do konfiguracji
	// private static final int CLIENT_RATE_REMEMBER_SECONDS = 180;
	// private static final int CLIENT_RATE_REMEMBER_CALLS_SECONDS = 20;
	// private static final int CLIENT_RATE_MIN_ENLISTMENT_TIME = 10;
	// private static final float CLIENT_RATE_THRESHOLD = 5.0f;
	
	private static final String[] CLIENT_RATE_URL_FILTER = new String[] {
			"/android/", "/wordDictionary/autocomplete", "/kanjiDictionary/autocomplete"
	};
	
	// mapa do liczenia liczby jednoczesnych polaczen dla wybranego adresu ip, aby wykrywac i blokowac ataki DDOS
	// private PassiveExpiringMapWithAutoClearExpiredObjects<String, ClientIP> clientRateMemoryMap = new PassiveExpiringMapWithAutoClearExpiredObjects<>(CLIENT_RATE_REMEMBER_SECONDS, TimeUnit.SECONDS);
	private PassiveExpiringMapWithAutoClearExpiredObjects<String, ClientIP> clientRateMemoryMap = new PassiveExpiringMapWithAutoClearExpiredObjects<>(new ExpirationPolicy<String, ClientIP>() {
		
		@Override
		public long expirationTime(String key, ClientIP clientIP) {
			return System.currentTimeMillis() + clientIP.clientRateRemeberSeconds * 1000;			
		}		
	});

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;
		
		ConfigService configService = getConfigService(httpServletRequest);
		
		// pobranie konfiguracji
		ConfigWrapper configWrapper = configService.getConfig();
		
		// pobranie informacji o kliencie (tutaj zawsze cos musi byc)
		ClientInfo clientInfo = (ClientInfo)request.getAttribute(ClientInfo.REQUEST_ATTRIBUTE);
		
		// sprawdzanie, czy ten klient nie przekracza limitu jednoczesnych wywolan
		IsClientRateExceededResult isClientRateExceededResult = isClientRateExceeded(configWrapper.getConfig().getFirewall().getClientRateExceeded(),
				clientInfo.ip, clientInfo.hostName, clientInfo.url);
				
		if (isClientRateExceededResult.isClientRateExceeded == true) { // przekroczono liczbe wywolan
			logger.info("Przekroczono liczbę jednoczesnych wywolan, ip {}, host name: {}, user agent: {}, url: {}, call rate: {} ",
					clientInfo.ip, clientInfo.hostName, clientInfo.userAgent, clientInfo.url, isClientRateExceededResult.callRate);
			
	        // logger
			if (configWrapper.getConfig().getFirewall().getClientRateExceeded().isClientRateExceededSendToLoggerListener() == true) {
		        ServletContext servletContext = request.getServletContext();
				WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
				
				LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);
				
				ClientBlockLoggerModel clientBlockLoggerModel = new ClientBlockLoggerModel(Utils.createLoggerModelCommon(httpServletRequest));
				
				loggerSender.sendLog(clientBlockLoggerModel);				
			}			
			
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
	
	private ConfigService getConfigService(ServletRequest request) {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		
		ConfigService configService = webApplicationContext.getBean(ConfigService.class);
		
		return configService;		
	}

	private IsClientRateExceededResult isClientRateExceeded(ClientRateExceeded clientRateExceededConfig, String ip, String hostName, String url) {
		
		ClientIP clientIP;
		
		synchronized (clientRateMemoryMap) {
			// szukamy klienta
			clientIP = clientRateMemoryMap.get(ip);
			
			// nie znaleziono, wiec tworzymy
			if (clientIP == null) {
				clientIP = new ClientIP(ip, hostName);
			}
			
			clientIP.setClientRateRemeberSeconds(clientRateExceededConfig.getClientRateRememberSeconds().intValue());
			
			// dodajemy raz jeszcze, aby zaktualizowac date wygasniecia
			clientRateMemoryMap.put(ip, clientIP);
		}
		
		// dodajemy do klienta to wywolanie i sprawdzamy, czy nie przekroczylismy limitu		
		return clientIP.addClientCall(clientRateExceededConfig, url);
	}
	
	private static class ClientIP {
		@SuppressWarnings("unused")
		private String ip;
		
		@SuppressWarnings("unused")
		private String hostName;
				
		private int clientRateRemeberSeconds; // parametr z konfiguracji
				
		private List<CallInfo> callList = new LinkedList<>();
		
		public ClientIP(String ip, String hostName) {
			this.ip = ip;
			this.hostName = hostName;
		}
		
		public void setClientRateRemeberSeconds(int clientRateRemeberSeconds) {
			this.clientRateRemeberSeconds = clientRateRemeberSeconds;
		}
		
		public synchronized IsClientRateExceededResult addClientCall(ClientRateExceeded clientRateExceededConfig, String url) {
			
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
				
				if (callTimestamp.plusSeconds(clientRateExceededConfig.getClientRateRememberCallsSeconds().intValue()).isBefore(LocalDateTime.now()) == true) { // usuwamy stare wpisy
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
			
			if (secondsBetweenStartAndLastDateTime < clientRateExceededConfig.getClientRateMinEnlistmentTimeSeconds().intValue()) { // sprawdzamy, czy mamy minimalny czas pozyskiwania danych
				return new IsClientRateExceededResult(false, Float.NaN);
			}
			
			// wyliczamy liczbe wywolan na sekunde
			float callRate = (float)callNumbers / (float)secondsBetweenStartAndLastDateTime;
						
			// czy przekroczono limit
			if (callRate >= clientRateExceededConfig.getClientRateThreashold()) {
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
