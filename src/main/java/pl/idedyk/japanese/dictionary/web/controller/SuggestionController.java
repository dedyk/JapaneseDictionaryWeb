package pl.idedyk.japanese.dictionary.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.SuggestionSendLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.SuggestionStartLoggerModel;

@Controller
public class SuggestionController extends CommonController {
	
	private static final Logger logger = Logger.getLogger(WordDictionaryController.class);
	
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private LoggerSender loggerSender;

	@RequestMapping(value = "/suggestion", method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpSession session, Map<String, Object> model) {
		
		// logowanie
		logger.info("SuggestionController: start");
		
		loggerSender.sendLog(new SuggestionStartLoggerModel(session.getId(), Utils.getRemoteIp(request), request.getHeader("User-Agent")));
	
		model.put("selectedMenu", "suggestion");
		
		return "suggestion";
	}
	
	@RequestMapping(produces = "application/json;charset=UTF-8", 
			value = "/suggestion/sendSuggestion", method = RequestMethod.POST) // INFO: jesli to zmieniasz, zmien rowniez w LinkGenerator
	
	public @ResponseBody String sendSuggestion(HttpServletRequest request, HttpSession session,
			@RequestParam(value="suggestion[title]", required=true) String title,
			@RequestParam(value="suggestion[sender]", required=true) String sender,
			@RequestParam(value="suggestion[body]", required=true) String body) {
		
		JSONObject jsonObject = new JSONObject();
		
		// logowanie
		logger.info("Wysyłanie treści sugestii:\n\tTytul: " + title + "\n\tNadawca: " + sender + "\n\tTresc: " + body);
		
		// wysylanie do logger'a i wysylacza mail'i
		loggerSender.sendLog(new SuggestionSendLoggerModel(session.getId(), Utils.getRemoteIp(request), request.getHeader("User-Agent"), title, sender, body));
		
		return jsonObject.toString();
	}
}
