package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.CaptchaModel;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.CatchaStartLoggerModel;

@Controller
public class CaptchaController {
	
	public static final String CAPTCH_URL_PREFIX = "/captcha/";
	public static final String CAPTCH_URL_START = CAPTCH_URL_PREFIX + "start";
	
	@Autowired
	protected LoggerSender loggerSender;
	
	private static final Logger logger = LogManager.getLogger(CaptchaController.class);
	
	@RequestMapping(value = CAPTCH_URL_START, method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpServletResponse response, HttpSession session, Map<String, Object> model) {

		logger.info("Start captcha");
		
		// utworzenie model weryfikacji Captcha
		CaptchaModel captchaModel = new CaptchaModel();

		// logowanie dla loggera
		CatchaStartLoggerModel redirectToCatchaLoggerModel = new CatchaStartLoggerModel(Utils.createLoggerModelCommon(request));
		
		loggerSender.sendLog(redirectToCatchaLoggerModel);
		
		// wypelnienie modelu z danymi formularza
		model.put("command", captchaModel);
		
		return "captcha";
	}
}
