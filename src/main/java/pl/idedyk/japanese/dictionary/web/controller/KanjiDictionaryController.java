package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

@Controller
public class KanjiDictionaryController extends CommonController {

	private static final Logger logger = Logger.getLogger(KanjiDictionaryController.class);

	@Autowired
	private DictionaryManager dictionaryManager;

	@RequestMapping(value = "/kanjiDictionary", method = RequestMethod.GET)
	public String start(Map<String, Object> model) {

		
		return "kanjiDictionary";
	}
}
