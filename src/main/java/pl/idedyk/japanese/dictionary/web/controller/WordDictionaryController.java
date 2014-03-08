package pl.idedyk.japanese.dictionary.web.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.idedyk.japanese.dictionary.web.controller.model.WordDictionarySearchModel;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;

@Controller
public class WordDictionaryController extends CommonController {
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Autowired
	private ServletContext servletContext;
	
	@RequestMapping(value = "/wordDictionary", method = RequestMethod.GET)
	public String start(Map<String, Object> model) {
		
		model.put("wordDictionaryActionSearchPath", servletContext.getContextPath() + "/wordDictionary/search");
		model.put("wordDictionaryAutocompletePath", servletContext.getContextPath() + "/wordDictionary/autocomplete");
		
		model.put("command", new WordDictionarySearchModel());
		
		return "wordDictionary";
	}
	
	@RequestMapping(value = "/wordDictionary/search", method = RequestMethod.POST)
	public String search(@ModelAttribute("wordDictionarySearchModel") WordDictionarySearchModel searchModel,
			Map<String, Object> model) {
		
		System.out.println("AAAAA: " + searchModel.getWord());
		
		// FIXME
		model.put("command", new WordDictionarySearchModel());
		
		return "wordDictionary";
	}
	
	@RequestMapping(produces = "application/json;charset=UTF-8", 
			value = "/wordDictionary/autocomplete", method = RequestMethod.GET)
	public @ResponseBody String autocomplete(@RequestParam(value="term", required=true) String term) {

		int fixme = 1;
		
		try {
			List<String> wordAutocomplete = dictionaryManager.getWordAutocomplete(term, 5);
					
			JSONArray jsonArray = new JSONArray();
			
			for (String currentWordAutocomplete : wordAutocomplete) {
				
				JSONObject jsonObject = new JSONObject();
				
				jsonObject.put("label", currentWordAutocomplete);
				jsonObject.put("value", currentWordAutocomplete);
	
				jsonArray.put(jsonObject);
			}
	
			return jsonArray.toString();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
}
