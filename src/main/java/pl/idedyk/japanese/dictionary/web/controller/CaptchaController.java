package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class CaptchaController {
	
	public static final String CAPTCH_URL_PREFIX = "/captcha/";
	public static final String CAPTCH_URL_CHECK = CAPTCH_URL_PREFIX + "check";
	
	private static final Logger logger = LogManager.getLogger(CaptchaController.class);
	
	@RequestMapping(value = CAPTCH_URL_CHECK, method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpServletResponse response, HttpSession session, Map<String, Object> model) {

		
		
		return null;
	}
}
