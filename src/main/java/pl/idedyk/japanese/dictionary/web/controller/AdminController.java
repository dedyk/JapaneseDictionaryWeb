package pl.idedyk.japanese.dictionary.web.controller;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
	private LoggerSender loggerSender;

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
