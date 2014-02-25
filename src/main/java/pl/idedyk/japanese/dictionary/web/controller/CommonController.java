package pl.idedyk.japanese.dictionary.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public class CommonController implements HandlerExceptionResolver {

	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object obj, Exception exc) {

		Map<String, Object> model = new HashMap<String, Object>();

		model.put("exception", exc);
		
		return new ModelAndView("applicationError", model);
	}
}
