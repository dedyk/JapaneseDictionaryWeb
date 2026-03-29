package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.common.Utils.ThemeType;

@Controller
public class ThemeController {
	
	private static final Logger logger = LogManager.getLogger(ThemeController.class);
		
	@RequestMapping(value = "/setTheme/{theme}", method = RequestMethod.GET)
	public @ResponseBody String setTheme(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("theme") String themeValue) {
		
		// zamiana na wybrany motyw
		ThemeType themeType = Arrays.asList(ThemeType.values()).stream().filter(f -> f.getThemeValue().equals(themeValue)).findFirst().orElse(Utils.getTheme(request));
						
		Utils.setTheme(response, session, themeType);
		
		logger.info("Ustawienie motywu na: " + themeType);
		
		return "ok";
	}
}
