package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.map.PassiveExpiringMap.ExpirationPolicy;
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
import pl.idedyk.japanese.dictionary.web.logger.model.ClientBlockInfoOnlyLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.ClientBlockLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.RedirectToCatchaLoggerModel;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;
import pl.idedyk.japanese.dictionary.web.service.ConfigService.ConfigWrapper;
import pl.idedyk.japanese.dictionary.web.taglib.utils.PassiveExpiringMapWithAutoClearExpiredObjects;

public class FirewallFilter implements Filter {
	
	private static final Logger logger = LogManager.getLogger(FirewallFilter.class);
		
	// gablota, tych klientow nie obslugujemy (w momencie, gdy przy blokadzie byl ustawiony czas)
	private PassiveExpiringMapWithAutoClearExpiredObjects<String, ClientInfo> temporaryBlockMap = new PassiveExpiringMapWithAutoClearExpiredObjects<>(new ExpirationPolicy<String, ClientInfo>() {

		@Override
		public long expirationTime(String key, ClientInfo value) {
			
			if (value.hostBlockTime != null) { // tutaj czas jest w sekundach
				return System.currentTimeMillis() + value.hostBlockTime * 1000;
			}
			
			// to nigdy nie powinno zdarzyc sie
			return 0;
		}
		
	});
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}
		
	private ConfigService getConfigService(ServletRequest request) {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		
		ConfigService configService = webApplicationContext.getBean(ConfigService.class);
		
		return configService;		
	}
		
	private void isClientBlocked(ConfigWrapper configWrapper, ClientInfo clientInfo, ClientInfo blockOldClientInfo) {

		// czy jest stara istniejaca blokada czasowa
		if (blockOldClientInfo != null && blockOldClientInfo.hostBlockOperation == HostBlockOperation.BLOCK) { // tego klienta tymczasowo nie obslugujemy
			clientInfo.hostBlockOperation = blockOldClientInfo.hostBlockOperation;
			clientInfo.doSendToLoggerListener = blockOldClientInfo.doSendToLoggerListener;
			
			return;
		}
		
		List<HostBlockList.HostBlock> hostBlockListList = configWrapper.getConfig().getFirewall().getHostBlockList().getHostBlock();		
		List<HostBlockList.HostBlock> matchedHostBlockList = new ArrayList<>(); // lista dopasowanych konfiguracji
		
		for (HostBlockList.HostBlock hostBlock : hostBlockListList) {

			// jezeli w danym host block wystepuje dany typ warunku to wszystkie one musza byc spelnione
			
			int numberOfCheckedConditions = 0; // liczba wykonanych sprawdzen	| aba warunki musza
			int numberOfSatisfiedConditions = 0; // liczba spelnionych warunkow	| byc wieksze od siebie i byc rowne sobie
			
			// sprawdzenie, czy adres ip jest na czarnej liscie
			HostBlock.BlackList blackListElemnt = hostBlock.getBlackList();
			
			if (blackListElemnt != null) {
				numberOfCheckedConditions++;
				
				if (clientInfo.ip != null && clientInfo.blackListLevel != null && clientInfo.blackListLevel.intValue() >= blackListElemnt.getMinLevel()) {
					numberOfSatisfiedConditions++;
				}
			}
			
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
			
			// sprawdzenie listy wykluczonych krajow
			List<HostBlockList.HostBlock.NotCountry> notCountryList = hostBlock.getNotCountry();
			
			if (notCountryList.size() > 0) {
				numberOfCheckedConditions++;
				
				int notCountrySatisfiedConditions = 0;
				
				for (HostBlockList.HostBlock.NotCountry notCountry : notCountryList) {
					
					if (clientInfo.country != null && notCountry.getValue().equals(clientInfo.country) == false) {						
						notCountrySatisfiedConditions++;
					}
				}
				
				if (notCountrySatisfiedConditions == notCountryList.size()) { // czy warunek spelniony, nic nie moze pasowac
					numberOfSatisfiedConditions++;
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
			
			// sprawdzenie listy wykluczonych ASN
			List<HostBlockList.HostBlock.NotAsn> notAsnList = hostBlock.getNotAsn();
			
			if (notAsnList.size() > 0) {
				numberOfCheckedConditions++;
				
				int notAsnSatisfiedConditions = 0;
				
				for (HostBlockList.HostBlock.NotAsn notAsn : notAsnList) {

					if (clientInfo.autonomousSystemNumber != null && notAsn.getValue().equals(clientInfo.autonomousSystemNumber) == false) {						
						notAsnSatisfiedConditions++;
					}
				}
				
				if (notAsnSatisfiedConditions == notAsnList.size()) { // czy warunek spelniony, nic nie moze pasowac
					numberOfSatisfiedConditions++;
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
			
			// sprawdzenie listy wykluczonych user agent
			List<HostBlockList.HostBlock.NotUserAgent> notUserAgentList = hostBlock.getNotUserAgent();
			
			if (notUserAgentList.size() > 0) {
				numberOfCheckedConditions++;
				
				int notUserAgentSatisfiedConditions = 0;
				
				for (HostBlockList.HostBlock.NotUserAgent notUserAgent : notUserAgentList) {
					
					if (clientInfo.userAgent.matches(notUserAgent.getValue()) == false) {						
						notUserAgentSatisfiedConditions++;
					}
				}
				
				if (notUserAgentSatisfiedConditions == notUserAgentList.size()) { // czy warunek spelniony, nic nie moze pasowac
					numberOfSatisfiedConditions++;
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
			
			// sprawdzenie listy wykluczonych adresow wywolania
			List<HostBlockList.HostBlock.NotFullUrl> notFullUrlList = hostBlock.getNotFullUrl();
			
			if (notFullUrlList.size() > 0) {
				numberOfCheckedConditions++;
				
				int notFullUrlSatisfiedConditions = 0;
				
				for (HostBlockList.HostBlock.NotFullUrl notFullUrl : notFullUrlList) {
					
					if (clientInfo.fullUrl.matches(notFullUrl.getValue()) == false) {						
						notFullUrlSatisfiedConditions++;
					}
				}
				
				if (notFullUrlSatisfiedConditions == notFullUrlList.size()) { // czy warunek spelniony, nic nie moze pasowac
					numberOfSatisfiedConditions++;
				}
			}
			
			//
			
			// sprawdzenie, czy wszystkie ustawione warunki zostaly spelnione
			if (numberOfCheckedConditions > 0 && numberOfCheckedConditions == numberOfSatisfiedConditions) {
				matchedHostBlockList.add(hostBlock);
			}
		}
		
		if (matchedHostBlockList.size() > 0) { // mamy cos dopasowane
			
			// sprawdzenie, ktore typy operacji wystepuja
			HostBlock infoHostBlock = matchedHostBlockList.stream().filter(f -> f.getOperation() == HostBlockOperation.INFO).findFirst().orElse(null);
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
				
			} else if (infoHostBlock != null) {
				hostBlockToUse = infoHostBlock;
			}
			
			if (hostBlockToUse != null) {
				clientInfo.hostBlockOperation = hostBlockToUse.getOperation();
				clientInfo.hostBlockTime = hostBlockToUse.getBlockTime() != null ? hostBlockToUse.getBlockTime().intValue() : null;
				clientInfo.doSendToLoggerListener = hostBlockToUse.isSendToLoggerListener();
			}			
		}
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
		
		// sprawdzenie, czy tego klienta nie obslugujemy, gdyz mial ustawiony czas blokady
		ClientInfo blockOldClientInfo = null;
		
		if (clientInfo.ip != null) {
			synchronized (temporaryBlockMap) {			
				blockOldClientInfo = temporaryBlockMap.get(clientInfo.ip);
			}
		}
				
		// sprawdzenie, czy zablokowac danego clientInfo
		isClientBlocked(configWrapper, clientInfo, blockOldClientInfo);
				
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
		
		// sprawdzenie, czy to tylko rejestrowanie faktu
		if (clientInfo.hostBlockOperation == HostBlockOperation.INFO) {			
			// zapis do logow
			ServletContext servletContext = request.getServletContext();
			WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			
			LoggerSender loggerSender = webApplicationContext.getBean(LoggerSender.class);
			
			ClientBlockInfoOnlyLoggerModel clientBlockInfoOnlyLoggerModel = new ClientBlockInfoOnlyLoggerModel(Utils.createLoggerModelCommon(httpServletRequest));
			
			loggerSender.sendLog(clientBlockInfoOnlyLoggerModel);
			
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
				logger.info("Blokowanie ip/host/user agent/url: " + clientInfo.ip + 
						" (" + clientInfo.autonomousSystemNumber + ", " + clientInfo.country + ") / " + clientInfo.hostName + " / " + clientInfo.userAgent + " / " + clientInfo.fullUrl + 
						(clientInfo.hostBlockTime != null ? (" - tymczasowa blokana na " + clientInfo.hostBlockTime + " sekund") : ""));
				
				if (clientInfo.hostBlockTime != null) { // istnieje wskazanie blokady czasowej, wiec dodajemy klienta do gabloty: tych klientow nie obslugujemy (tymczasowo)	
					if (clientInfo.ip != null) {
						synchronized (temporaryBlockMap) {			
							temporaryBlockMap.put(clientInfo.ip, clientInfo);
						}
					}
				}
				
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
				
		// normalne wywolanie		
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// noop
	}
}
