package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.config.xsd.InfoPage.Page;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.InfoLoggerModel;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;
import pl.idedyk.japanese.dictionary.web.service.ConfigService.ConfigWrapper;

@Controller
public class InfoController {
	
	private static final Logger logger = LogManager.getLogger(InfoController.class);
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired
	private ConfigService configService;
	
	@Value("${base.server}")
	private String baseServer;
		
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {
		
		logger.info("Wyswietlanie strony informacyjnej");
		
		// logowanie
		loggerSender.sendLog(new InfoLoggerModel(Utils.createLoggerModelCommon(request)));
		
		model.put("selectedMenu", "info");
		model.put("canonicalUrl", baseServer + "/info");
				
		return "info";
	}
	
	@RequestMapping(value = "/info/{code}", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("code") String code, Map<String, Object> model) throws NoResourceFoundException {
		
		// pobranie konfiguracji
		ConfigWrapper config = configService.getConfig();

		if (code == null) { // to nigdy nie powinno zdarzyc sie
			throw new NoResourceFoundException(HttpMethod.GET, "Not found");
		}
		
		// szukamy strony w konfiguracji
		Page page = config.getConfig().getInfoPage().getPage().stream().filter(f -> f.getCode().equals(code)).findFirst().orElse(null);
		
		if (page == null) { // nie znaleziono takiej strony
			throw new NoResourceFoundException(HttpMethod.GET, "Not found");
		}
		
		// pokazanie tej strony
		
		// logowanie
		loggerSender.sendLog(new InfoLoggerModel(Utils.createLoggerModelCommon(request)));
		
		model.put("selectedMenu", "info");
		model.put("canonicalUrl", baseServer + "/info/" + code);
		
		model.put("pageTitle", page.getTitle());
		model.put("pageDescription", page.getPageDescription());
		model.put("pageContent", page.getContent());
		
		return "infoPage";
	}
}
