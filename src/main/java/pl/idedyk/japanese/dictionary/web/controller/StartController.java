package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class StartController {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String start(Map<String, Object> model) {

		model.put("message", "JapaneseDictionaryWeb - start");
		
		return "start";
	}
}
