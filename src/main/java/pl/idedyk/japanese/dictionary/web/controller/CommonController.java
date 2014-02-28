package pl.idedyk.japanese.dictionary.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public class CommonController implements HandlerExceptionResolver {

	private static final Logger logger = Logger.getLogger(CommonController.class);
	
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object obj, Exception exc) {
		
		logger.error("Blad podczas dzialania kontrolera", exc);

		Map<String, Object> model = new HashMap<String, Object>();

		model.put("exception", exc);
		
		return new ModelAndView("applicationError", model);
	}
}
