package pl.idedyk.japanese.dictionary.web.service;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

@Service
public class UserAgentService {
	
	private static final Logger logger = Logger.getLogger(UserAgentService.class);

	private UserAgentAnalyzer userAgentAnalyzer;
	
	@PostConstruct
	public void init() {
		
		logger.info("Inicjalizacja user agent service");
		
		userAgentAnalyzer = UserAgentAnalyzer
				.newBuilder()
				.hideMatcherLoadStats()
				.withCache(10000)
				.build();
	}
	
	public synchronized boolean isRobot(String userAgentString) {
		
		UserAgent userAgent = null;
		
		try {
			userAgent = userAgentAnalyzer.parse(userAgentString);
			
		} catch (Exception e) {
			
			logger.error("Błąd podczas parsowania user agent", e);
			
			return false;
		}
		
		String deviceClass = userAgent.getValue(UserAgent.DEVICE_CLASS);
		
		if (deviceClass == null) {
			return false;
		}
		
		return deviceClass.equalsIgnoreCase("Robot") == true;		
	}
	
	public synchronized String getUserAgentInPrintableForm(String userAgentString) {
		
		if (userAgentString == null) {
			return "-";
		}
		
		UserAgent userAgent = null;
		
		try {
			userAgent = userAgentAnalyzer.parse(userAgentString);
			
		} catch (Exception e) {
			
			logger.error("Błąd podczas parsowania user agent", e);
			
			return userAgentString;
		}
		
		StringBuffer result = new StringBuffer();
		
		if (userAgentString.startsWith("JapaneseAndroidLearnHelper") == true) {	// czy typ to slownik w postaci aplikacji na telefon
			
			result.append(userAgentString);
			
		} else { // parsujemy agenta

			String deviceClass = userAgent.getValue(UserAgent.DEVICE_CLASS);
			
			if (deviceClass == null) {
				deviceClass = "Unknown";
			}

			if (deviceClass.equalsIgnoreCase("Hacker") == true) { // czy typ to jakis nieznany typ (hacker)
				result.append(userAgentString);
				
			} else { // cos znanego, wyciagamy informacje
				
				result.append(deviceClass); // typ urzadzenia, np. Desktop, Phone, Robot
				
				String deviceName = userAgent.getValue(UserAgent.DEVICE_NAME); // nazwa urzadzenia
				
				if (deviceName == null) {
					deviceName = "Unknown";
				}
				
				result.append(" / ").append(deviceName);
				
				String operationSystemClass = userAgent.getValue(UserAgent.OPERATING_SYSTEM_CLASS); // klasa systemu operacyjnego
				
				if (operationSystemClass == null) {
					operationSystemClass = "Unknown";
				}
				
				result.append(" / ").append(operationSystemClass);
				
				String operationSystemNameVersion = userAgent.getValue("OperatingSystemNameVersion"); // nazwa i wersja systemu operacyjnego
				
				if (operationSystemNameVersion == null) {
					operationSystemNameVersion = "Unknown";
				}

				result.append(" / ").append(operationSystemNameVersion);
				
				String agentClass = userAgent.getValue(UserAgent.AGENT_CLASS); // klasa agenta

				if (agentClass == null) {
					agentClass = "Unknown";
				}
				
				result.append(" / ").append(agentClass);

				String agentNameVersionMajor = userAgent.getValue("AgentNameVersionMajor"); // nazwa i wersja glowna agenta

				if (agentNameVersionMajor == null) {
					agentNameVersionMajor = "Unknown";
				}
				
				result.append(" / ").append(agentNameVersionMajor);

				String agentInformationUrl = userAgent.getValue("AgentInformationUrl"); // adres do strony informacyjnej agenta (robota)

				if (agentInformationUrl == null) {
					agentInformationUrl = "Unknown";
				}
				
				result.append(" / ").append(agentInformationUrl);
				
				/*
				for (String fieldName: userAgent.getAvailableFieldNamesSorted()) {
					System.out.println(fieldName + " = " + userAgent.getValue(fieldName));
				}
				*/
			}				
		}

		return result.toString();
	}
	
	/*
	public static void main(String[] args) {

		// test
		
		UserAgentService userAgentService = new UserAgentService();
		
		userAgentService.init();
		
		System.out.println("----");

		String[] uas = new String[] {
			"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36",
			"Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
			"Mozilla/5.0 (compatible; DotBot/1.1; http://www.opensiteexplorer.org/dotbot, help@moz.com)",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134",
			"Mozilla/5.0 (iPhone; CPU iPhone OS 12_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",
			"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
			"JapaneseAndroidLearnHelper/527/20170115",
			"asdasdsa",
			"Mozilla/5.0 (Linux; Android 8.0.0; VTR-L29) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.99 Mobile Safari/537.36",
			"curl/7.54.1"
		};
		
		for (String userAgentString : uas) {			
			System.out.println(userAgentString + " = " + userAgentService.getUserAgentInPrintableForm(userAgentString));
		}
	}
	*/
}
