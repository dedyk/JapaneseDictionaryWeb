package pl.idedyk.japanese.dictionary.web.service;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import pl.idedyk.japanese.dictionary.web.service.dto.UserAgentInfo;

@Service
public class UserAgentService {
	
	private static final Logger logger = LogManager.getLogger(UserAgentService.class);

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
	
	/*
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
				
				/ *
				for (String fieldName: userAgent.getAvailableFieldNamesSorted()) {
					System.out.println(fieldName + " = " + userAgent.getValue(fieldName));
				}
				* /
			}				
		}

		return result.toString();
	}
	*/
	
	public synchronized UserAgentInfo getUserAgentInfo(String userAgentString) {
		
		if (userAgentString == null || userAgentString.equals("-") == true) {
			return new UserAgentInfo(UserAgentInfo.Type.NULL);
		}
				
		//
		
		if (userAgentString.startsWith("JapaneseAndroidLearnHelper") == true) {	// czy typ to slownik w postaci aplikacji na telefon
			
			String[] userAgentStringSplited = userAgentString.split("/");
			
			UserAgentInfo userAgentInfo = new UserAgentInfo(UserAgentInfo.Type.JAPANESE_ANDROID_LEARNER_HELPER);
			
			UserAgentInfo.JapaneseAndroidLearnerHelperInfo.SubType subType = userAgentString.startsWith("JapaneseAndroidLearnHelper/") == true ?
					UserAgentInfo.JapaneseAndroidLearnerHelperInfo.SubType.FULL : UserAgentInfo.JapaneseAndroidLearnerHelperInfo.SubType.SLIM;
			
			userAgentInfo.setJapaneseAndroidLearnerHelperInfo(new UserAgentInfo.JapaneseAndroidLearnerHelperInfo(Integer.parseInt(userAgentStringSplited[1]), userAgentStringSplited[2], subType));
			
			return userAgentInfo;
		}
		
		//
		
		UserAgent userAgent = null;
		
		try {
			userAgent = userAgentAnalyzer.parse(userAgentString);
			
		} catch (Exception e) {
			
			logger.error("Błąd podczas parsowania user agent", e);
			
			UserAgentInfo userAgentInfo = new UserAgentInfo(UserAgentInfo.Type.OTHER);
			
			userAgentInfo.setOtherInfo(new UserAgentInfo.OtherInfo(userAgentString));
			
			return userAgentInfo;
		}
		
		//
		
		String deviceClass = userAgent.getValue(UserAgent.DEVICE_CLASS);
		
		if (deviceClass == null) {
			deviceClass = "Unknown";
		}

		//
		
		if (deviceClass.equalsIgnoreCase("Hacker") == true) { // czy typ to jakis nieznany typ (hacker)
			
			UserAgentInfo userAgentInfo = new UserAgentInfo(UserAgentInfo.Type.OTHER);
			
			userAgentInfo.setOtherInfo(new UserAgentInfo.OtherInfo(userAgentString));
			
			return userAgentInfo;
			
		} else if (deviceClass.equalsIgnoreCase("Desktop") == true) { // komputer
			
			UserAgentInfo userAgentInfo = new UserAgentInfo(UserAgentInfo.Type.DESKTOP);
			
			String computerType = userAgent.getValue(UserAgent.DEVICE_NAME); // typ systemu operacyjnego, np. Linux
			
			if (computerType == null) {
				computerType = "Unknown";
			}
			
			if (computerType.equals("Desktop") == true) {
				computerType = "Windows Desktop";
			}
			
			String operationSystemNameVersion = userAgent.getValue("OperatingSystemNameVersion"); // nazwa i wersja systemu operacyjnego
			
			if (operationSystemNameVersion == null) {
				operationSystemNameVersion = "Unknown";
			}
			
			String agentNameVersionMajor = userAgent.getValue("AgentNameVersionMajor"); // nazwa i wersja glowna przegladarki

			if (agentNameVersionMajor == null) {
				agentNameVersionMajor = "Unknown";
			}
			
			//

			userAgentInfo.setDesktopInfo(new UserAgentInfo.DesktopInfo(computerType, operationSystemNameVersion, agentNameVersionMajor));
			
			return userAgentInfo;
			
		} else if (deviceClass.equalsIgnoreCase("Phone") == true || deviceClass.equalsIgnoreCase("Mobile") == true || deviceClass.equalsIgnoreCase("Tablet") == true) { // telefon lub tablet
			
			UserAgentInfo userAgentInfo = new UserAgentInfo(deviceClass.equalsIgnoreCase("Tablet") == true ? UserAgentInfo.Type.TABLET : UserAgentInfo.Type.PHONE);
			
			String deviceName = userAgent.getValue(UserAgent.DEVICE_NAME); // rodzaj urzadzenia
			
			if (deviceName == null) {
				deviceName = "Unknown";
			}
						
			String operationSystemNameVersion = userAgent.getValue("OperatingSystemNameVersion"); // nazwa i wersja systemu operacyjnego
			
			if (operationSystemNameVersion == null) {
				operationSystemNameVersion = "Unknown";
			}
			
			String agentNameVersionMajor = userAgent.getValue("AgentNameVersionMajor"); // nazwa i wersja glowna przegladarki

			if (agentNameVersionMajor == null) {
				agentNameVersionMajor = "Unknown";
			}
			
			//

			userAgentInfo.setPhoneTabletInfo(new UserAgentInfo.PhoneTabletInfo(deviceName, operationSystemNameVersion, agentNameVersionMajor));
			
			return userAgentInfo;
			
		} else if (deviceClass.startsWith("Robot") == true) { // robot
			
			UserAgentInfo userAgentInfo = new UserAgentInfo(UserAgentInfo.Type.ROBOT);
			
			String deviceName = userAgent.getValue(UserAgent.DEVICE_NAME); // nazwa robota (I czesc)
			
			if (deviceName == null) {
				deviceName = "Unknown";
			}
						
			String agentNameVersionMajor = userAgent.getValue("AgentNameVersionMajor"); // nazwa robota (II czesc)
			
			if (agentNameVersionMajor == null) {
				agentNameVersionMajor = "Unknown";
			}
			
			String agentInformationUrl = userAgent.getValue("AgentInformationUrl"); // adres do strony informacyjnej robota

			if (agentInformationUrl == null) {
				agentInformationUrl = "Unknown";
			}
			
			//
			
			userAgentInfo.setRobotInfo(new UserAgentInfo.RobotInfo(deviceName + " / " + agentNameVersionMajor, agentInformationUrl));

			return userAgentInfo;
			
		} else { // inny
			
			UserAgentInfo userAgentInfo = new UserAgentInfo(UserAgentInfo.Type.OTHER);
			
			userAgentInfo.setOtherInfo(new UserAgentInfo.OtherInfo(userAgentString));
			
			return userAgentInfo;
		}
	}
	
	/*
	public static void main(String[] args) throws Exception {

		UserAgentService userAgentService = new UserAgentService();
		
		userAgentService.init();
		
		// test
		
		/ *		
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
		* /
		
		MySQLConnector mySQLConnection = new MySQLConnector();
		
		List<GenericTextStat> userAgentClientStat = mySQLConnection.getUserAgentClientStat(0, 1);
		
		for (GenericTextStat genericTextStat : userAgentClientStat) {
			
			String userAgentString = genericTextStat.getText();
			
			UserAgentInfo userAgentInfo = userAgentService.getUserAgentInfo(userAgentString);
			
			if (userAgentInfo != null) {
				
				if (userAgentInfo.getType() == UserAgentInfo.Type.JAPANESE_ANDROID_LEARNER_HELPER) {
					
					// System.out.println(userAgentInfo.getJapaneseAndroidLearnerHelperInfo().getCode() + " - " + userAgentInfo.getJapaneseAndroidLearnerHelperInfo().getCodeName());
				}
				
				if (userAgentInfo.getType() == UserAgentInfo.Type.DESKTOP) {
					
//					System.out.println(userAgentInfo.getDesktopInfo().getDesktopType());
//					System.out.println(userAgentInfo.getDesktopInfo().getOperationSystem());
//					System.out.println(userAgentInfo.getDesktopInfo().getBrowserType());
//					System.out.println("---");
				}

				if (userAgentInfo.getType() == UserAgentInfo.Type.PHONE || userAgentInfo.getType() == UserAgentInfo.Type.TABLET) {
					
//					System.out.println(userAgentInfo.getType());
//					System.out.println(userAgentInfo.getPhoneTabletInfo().getDeviceName());
//					System.out.println(userAgentInfo.getPhoneTabletInfo().getOperationSystem());
//					System.out.println(userAgentInfo.getPhoneTabletInfo().getBrowserType());
//					System.out.println("---");
				}
				
				if (userAgentInfo.getType() == UserAgentInfo.Type.ROBOT) {
					
//					System.out.println(userAgentInfo.getRobotInfo().getRobotName());
//					System.out.println(userAgentInfo.getRobotInfo().getRobotUrl());
//					System.out.println("---");
				}
				
				if (userAgentInfo.getType() == UserAgentInfo.Type.OTHER) {
					
					System.out.println(userAgentInfo.getOtherInfo().getUserAgent());
					System.out.println("---");
					
				}
			}			
		}
	}
	*/
}
