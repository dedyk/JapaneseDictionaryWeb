package pl.idedyk.japanese.dictionary.web.service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;

import jakarta.annotation.PostConstruct;

@Service
public class GeoIPService {
	
	private static final Logger logger = LogManager.getLogger(GeoIPService.class);
	
	@Value("${geoip.db.city.path}")
	private String dbCityPath;
	
	private DatabaseReader databaseReader = null;
		
	@PostConstruct
	public void init() throws IOException {
		
		logger.info("Inicjalizacja GeoIPService");
		
		databaseReader = new DatabaseReader.Builder(new File(dbCityPath)).withCache(new CHMCache()).build();
	}
	
	public String getCountry(String ip) {
		
		CityResponse cityResponse = getCityResponse(ip);
		
		if (cityResponse == null) {
			return "Unknown";
		}
		
		Country country = cityResponse.getCountry();
		
		if (country == null) {
			return "Unknown";
		}
		
		String name = country.getName();
		
		if (name == null) {
			return "Unknown";
		}
		
		return name;
	}
	
	public String getCountryAndCity(String ip) {
		
		String countryName = "Unknown";
		String cityName = "Unknown";
		
		CityResponse cityResponse = getCityResponse(ip);
		
		if (cityResponse != null && cityResponse.getCountry() != null && cityResponse.getCountry().getName() != null) {
			countryName = cityResponse.getCountry().getName();
		}
		
		if (cityResponse != null && cityResponse.getCity() != null && cityResponse.getCity().getName() != null) {
			cityName = cityResponse.getCity().getName();
		}
		
		return countryName + " / " + cityName;
	}
	
	private CityResponse getCityResponse(String ip) {
		
		if (ip == null) {
			return null;
		}
		
		// sprawdzanie, czy adres ip jest rozdzielony przecinkiem, jesli tak to pobieramy pierwsza wartosc
		String[] ipSplited = ip.split(",");
		
		if (ipSplited != null && ipSplited.length > 1) {
			ip = ipSplited[0].trim();
		}
		
		try {
			InetAddress inetAddress = InetAddress.getByName(ip);
			
			if (inetAddress.isLoopbackAddress() == true) { // czy to adres loopback
				return null;
			}

			return databaseReader.city(inetAddress);
		
		} catch (Exception e) {
			
			logger.error("Can't get city response", e);
			
			return null;			
		}
	}
}
