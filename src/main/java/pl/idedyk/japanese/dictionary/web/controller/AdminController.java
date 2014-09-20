package pl.idedyk.japanese.dictionary.web.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel;
import pl.idedyk.japanese.dictionary.web.schedule.ScheduleTask;

@Controller
public class AdminController {
	
	private static final Logger logger = Logger.getLogger(AdminController.class);

	@Value("${admin.password}")
	private String adminPassword;
	
	@Autowired
	private ScheduleTask scheduleTask;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private LoggerSender loggerSender;

	@RequestMapping(value = "/admlogin", method = RequestMethod.GET)
	public String login(
		@RequestParam(value = "error", required = false) String error,
		HttpServletRequest request, HttpSession session, Map<String, Object> model) {
 
		int fixme = 1;
		// logowanie, start, poprawne logowanie, niepoprawne logowanie
		// strona 403: Access is denied
				
		if (error != null) {			
			String errorMessage = messageSource.getMessage("admin.login.page.login.error", new Object[] { }, Locale.getDefault());
						
			model.put("errorMessage", errorMessage);
		}
		
		return "admlogin";
	}
	
	@RequestMapping(value = "/admAccessDenied", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	public String handleForbidden(HttpServletRequest request, HttpServletResponse response, HttpSession session, Exception ex) {

		logger.error("Brak dostępu do strony: " + Utils.getRequestURL(request));
		
		// wysylanie do logger'a
		int fixme = 1;
		
		//PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
		
		//loggerSender.sendLog(pageNoFoundExceptionLoggerModel);

		return "page403";
	}
	
    @RequestMapping(value = "/adm/generateDailyReport", method = RequestMethod.GET)
    public void generateDailyReport(HttpServletRequest request, HttpSession session, Writer writer,
    		@RequestParam(value="p", required = false) String password /* haslo */) throws IOException {

    	logger.info("Generowanie dziennego raportu na żądanie");
    	
    	// model do logowania
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.GENERATE_DAILY_REPORT);
    	
    	// sprawdzenie hasla
    	boolean checkPasswordResult = checkPassword(password);
    	
    	if (checkPasswordResult == true) {    		
    		
    		adminLoggerModel.setResult(AdminLoggerModel.Result.OK);
    		
    		// logowanie
    		loggerSender.sendLog(adminLoggerModel);    	
    		
    		// generowanie raportu
    		scheduleTask.generateDailyReport();    		
    		
    	} else { // niepoprawne haslo
    		
    		adminLoggerModel.setResult(AdminLoggerModel.Result.INCORRECT_PASSWORD);    		
    		adminLoggerModel.addParam("incorrect_password", password);
    		
    		logger.error("Niepoprawne haslo");
    		
    		// logowanie
    		loggerSender.sendLog(adminLoggerModel);
    	}    	
		
		writer.append(adminLoggerModel.getResult().toString());
    }
    
    private boolean checkPassword(String password) {
    	
    	if (password == null) {
    		return false;
    	}
    	
    	if (adminPassword.equals(password) == true) {
    		return true;
    		
    	} else {
    		return false;
    	}
    }
}
