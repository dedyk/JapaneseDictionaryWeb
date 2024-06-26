package pl.idedyk.japanese.dictionary.web.controller;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.AdminPanelMissingWordsQueueModel;
import pl.idedyk.japanese.dictionary.web.controller.model.AdminPanelModel;
import pl.idedyk.japanese.dictionary.web.controller.validator.AdminPanelMissingWordsQueueModelValidator;
import pl.idedyk.japanese.dictionary.web.controller.validator.AdminPanelModelValidator;
import pl.idedyk.japanese.dictionary.web.dictionary.DictionaryManager;
import pl.idedyk.japanese.dictionary.web.dictionary.ZinniaManager;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel.Result;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchMissingWordQueue;
import pl.idedyk.japanese.dictionary.web.report.ReportGenerator;
import pl.idedyk.japanese.dictionary.web.report.ReportGenerator.Report;
import pl.idedyk.japanese.dictionary.web.schedule.ScheduleTask;
import pl.idedyk.japanese.dictionary.web.sitemap.SitemapManager;

@Controller
public class AdminController {
	
	private static final Logger logger = LogManager.getLogger(AdminController.class);
	
	private static final int GENERIC_LOG_SIZE = 100;

	@Value("${admin.password}")
	private String adminPassword;
	
	@Autowired
	private ScheduleTask scheduleTask;
	
	@Autowired
	private MySQLConnector mySQLConnector;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Autowired  
	private AdminPanelModelValidator adminPanelModelValidator;

	@Autowired  
	private AdminPanelMissingWordsQueueModelValidator adminPanelMissingWordsQueueModelValidator;
	
	@Autowired
	private DictionaryManager dictionaryManager;
	
	@Autowired
	private ZinniaManager zinniaManager;
	
	@Autowired
	private SitemapManager sitemapManager;
	
	@Autowired
	private ReportGenerator reportGenerator;
	
	@InitBinder(value = { "command" })
	private void initBinder(WebDataBinder binder) {  
		binder.setValidator(adminPanelModelValidator);  
	}

	@InitBinder(value = { "command2" })
	private void initBinder2(WebDataBinder binder) {  
		binder.setValidator(adminPanelMissingWordsQueueModelValidator);  
	}
	
	@RequestMapping(value = "/admlogin", method = RequestMethod.GET)
	public String login(
		@RequestParam(value = "error", required = false) String error,
		HttpServletRequest request, HttpSession session, Map<String, Object> model) {
 				
		if (error != null) {			
			String errorMessage = messageSource.getMessage("admin.login.page.login.error", new Object[] { }, Locale.getDefault());
						
			model.put("errorMessage", errorMessage);
			
		} else {
	    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
	    	
	    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_LOGIN_START);
	    	adminLoggerModel.setResult(Result.OK);
			
    		// logowanie
    		loggerSender.sendLog(adminLoggerModel);
		}
		
		return "admlogin";
	}
	
	@RequestMapping(value = "/accessDenied", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	public String handleForbidden(HttpServletRequest request, HttpServletResponse response, HttpSession session, Exception ex) {
		return "page403";
	}
	
    @RequestMapping(value = "/adm/panel", method = RequestMethod.GET)
    public String showPanel(HttpServletRequest request, HttpSession session, Writer writer, Map<String, Object> model) throws IOException, SQLException {
    	    	
		// stworzenie modelu
		AdminPanelModel adminPanelModel = new AdminPanelModel();
		
		// ustawienie filtra na operacje

		for (GenericLogOperationEnum genericLogOperationEnum : GenericLogOperationEnum.values()) {
			adminPanelModel.addGenericLogOperationEnum(genericLogOperationEnum);
		}
		
		fillModelForPanel(session, true, adminPanelModel, model, true);
		
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_PANEL);
    	adminLoggerModel.setResult(Result.OK);
    	
    	adminLoggerModel.addParam("pageNo", adminPanelModel.getPageNo());
    	adminLoggerModel.addParam("genericLogOperationStringList", adminPanelModel.getGenericLogOperationStringList() == null ? "" : adminPanelModel.getGenericLogOperationStringList().toString());
		
		// logowanie
		loggerSender.sendLog(adminLoggerModel);
		
    	return "admpanel";
    }
    
    private void fillModelForPanel(HttpSession session, boolean restoreFromSession, AdminPanelModel adminPanelModel, Map<String, Object> model, boolean validationResult) throws SQLException {

    	// przywrocenie danych z sesji (jesli sa)
    	AdminPanelModel adminPanelModelFromSession = (AdminPanelModel)session.getAttribute("adminPanelModel");
    	
    	if (restoreFromSession == true && adminPanelModelFromSession != null) {
    		adminPanelModel.setPageNo(adminPanelModelFromSession.getPageNo());
    		adminPanelModel.setGenericLogOperationStringList(adminPanelModelFromSession.getGenericLogOperationStringList());
    	}

    	session.setAttribute("adminPanelModel", adminPanelModel);
    	
		// pobranie ilosci operacji
		long genericLogSize = Integer.MAX_VALUE; // mySQLConnector.getGenericLogSize(adminPanelModel.getGenericLogOperationStringList());
    	
		model.put("selectedMenu", "panel");
		
    	model.put("command", adminPanelModel);
    	model.put("maxPageSize", (genericLogSize / GENERIC_LOG_SIZE) + (genericLogSize % GENERIC_LOG_SIZE > 0 ? 1 : 0));
    	
    	model.put("genericLogOperationEnumList", GenericLogOperationEnum.getSortedValues());
    	
    	if (validationResult == true) {
    		
    		// pobranie listy operacji
    		List<GenericLog> genericLogList = mySQLConnector.getGenericLogList(Long.parseLong(adminPanelModel.getPageNo()) - 1, GENERIC_LOG_SIZE, adminPanelModel.getGenericLogOperationStringList());    		
    		
    		model.put("genericLogList", genericLogList);
    		session.setAttribute("genericLogList", genericLogList);
    		
    		model.put("currentPage", adminPanelModel.getPageNo());
    	}		
    }

    @RequestMapping(value = "/adm/panelSearch", method = RequestMethod.GET)
    public String searchPanel(HttpServletRequest request, HttpSession session, Writer writer, @ModelAttribute("command") @Valid AdminPanelModel adminPanelModel,
			BindingResult result, Map<String, Object> model) throws IOException, SQLException {
    	
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_PANEL_SEARCH);
		    	
    	if (result.hasErrors() == true) {
    		    		
    		fillModelForPanel(session, false, adminPanelModel, model, false);
    		
    		adminLoggerModel.setResult(Result.ERROR);
    		
    		adminLoggerModel.addParam("pageNo", adminPanelModel.getPageNo());
    		adminLoggerModel.addParam("genericLogOperationStringList", adminPanelModel.getGenericLogOperationStringList() == null ? "" : adminPanelModel.getGenericLogOperationStringList().toString());
    		
    	} else {
    		
    		fillModelForPanel(session, false, adminPanelModel, model, true);
    		
    		adminLoggerModel.setResult(Result.OK);
    		
    		adminLoggerModel.addParam("pageNo", adminPanelModel.getPageNo());
    		adminLoggerModel.addParam("genericLogOperationStringList", adminPanelModel.getGenericLogOperationStringList() == null ? "" : adminPanelModel.getGenericLogOperationStringList().toString());
    	}
    	
		// logowanie
		loggerSender.sendLog(adminLoggerModel);    	
    	    	
    	return "admpanel";
    }
    
	@RequestMapping(value = "/adm/showGenericLog/{id}", method = RequestMethod.GET)
	public String showWordDictionaryDetails(HttpServletRequest request, HttpSession session, @PathVariable("id") long id, Map<String, Object> model) throws SQLException {
		
		// UWAGA: Gdy bedziesz zmieniac pamietaj o LinkGenerator
		
		GenericLog genericLog = mySQLConnector.getGenericLog(id);
		
		// logowanie
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_SHOW_GENERIC_LOG);
    	adminLoggerModel.setResult(genericLog != null ? Result.OK : Result.ERROR);
		
    	adminLoggerModel.addParam("id", String.valueOf(id));
    	
    	loggerSender.sendLog(adminLoggerModel);
		
    	// tworzenie modelu
		model.put("genericLog", genericLog);
		
		model.put("selectedMenu", "panel");
		
		model.put("pageTitle", messageSource.getMessage("admin.panel.genericLogDetails.page.genericLog.title", new Object[] { }, Locale.getDefault()));
		model.put("pageDescription", "");
		
		return "admShowGenericLogDetails";
	}
    
    @RequestMapping(value = "/adm/generateDailyReport", method = RequestMethod.GET)
    public String generateDailyReport(HttpServletRequest request, HttpSession session) throws IOException {

    	logger.info("Generowanie dziennego raportu na żądanie");
    	
    	// model do logowania
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.GENERATE_DAILY_REPORT);    		
		adminLoggerModel.setResult(AdminLoggerModel.Result.OK);
		
		// logowanie
		loggerSender.sendLog(adminLoggerModel);    	
		
		// generowanie raportu
		scheduleTask.generateDailyReport(false);	
		
		return "redirect:/adm/panel";
    }
    
    @RequestMapping(value = "/adm/showCurrentDailyReport", method = RequestMethod.GET)
    public String showCurrentDailyReport(HttpServletRequest request, HttpSession session, Map<String, Object> model) throws Exception {

    	logger.info("Pokazywanie obecnej postaci dziennego raportu");

    	// model do logowania
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.SHOW_CURRENT_DAILY_REPORT);    		
		adminLoggerModel.setResult(AdminLoggerModel.Result.OK);
		
		// logowanie
		loggerSender.sendLog(adminLoggerModel);
		
		ReportGenerator.DailyReport generateDailyReportBody = reportGenerator.generateDailyReportBody();

		model.put("selectedMenu", "showCurrentDailyReport");
		
		model.put("dailyReportBody", generateDailyReportBody != null ? generateDailyReportBody.body : "");
    	
		model.put("pageTitle", messageSource.getMessage("admin.panel.showCurrentDailyReport.title", new Object[] { }, Locale.getDefault()));
		model.put("pageDescription", "");
		
    	return "admShowCurrentDailyReport";
    }
    
    @RequestMapping(value = "/adm/reloadDatabase", method = RequestMethod.GET)
    public String reloadDatabase(HttpServletRequest request, HttpSession session) throws Exception {

    	logger.info("Przeładowanie bazy danych");
    	
    	// model do logowania
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.RELOAD_DATABASE);    		
		adminLoggerModel.setResult(AdminLoggerModel.Result.OK);
		
		// logowanie
		loggerSender.sendLog(adminLoggerModel);    	
		
		// przeladowanie bazy danych
		
		Thread dictionaryManagerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				dictionaryManager.reload();
			}
		});

		Thread zinniaManagerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				zinniaManager.reload();
			}
		});
		
		dictionaryManagerThread.start();
		zinniaManagerThread.start();
		
		dictionaryManagerThread.join();
		zinniaManagerThread.join();
		
		sitemapManager.reload();
		
		return "redirect:/adm/panel";
    }
    
    @RequestMapping(value = "/adm/showMissingWordsQueuePanel", method = RequestMethod.GET)
    public String showMissingWordsQueuePanel(HttpServletRequest request, HttpSession session, Writer writer, Map<String, Object> model) throws Exception {
    	    	
		// stworzenie modelu
    	AdminPanelMissingWordsQueueModel adminPanelMissingWordsQueueModel = new AdminPanelMissingWordsQueueModel();
				
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_SHOW_MISSING_WORDS_QUEUE_PANEL);
    	adminLoggerModel.setResult(Result.OK);
    	
    	adminLoggerModel.addParam("size", adminPanelMissingWordsQueueModel.getSize());
    	adminLoggerModel.addParam("showMaxSize", adminPanelMissingWordsQueueModel.getShowMaxSize());
    	adminLoggerModel.addParam("wordList", adminPanelMissingWordsQueueModel.getWordList());
    	adminLoggerModel.addParam("lock", Boolean.valueOf(adminPanelMissingWordsQueueModel.isLock()));
		
		model.put("selectedMenu", "missingWordsQueuePanel");		
    	model.put("command2", adminPanelMissingWordsQueueModel);
    	
		// logowanie
		loggerSender.sendLog(adminLoggerModel);
		
    	return "admMissingWordsQueuePanel";
    }
    
    @RequestMapping(value = "/adm/getMissingWordsQueue", method = RequestMethod.POST)
    public String searchPanel(HttpServletRequest request, HttpSession session, Writer writer, @ModelAttribute("command2") @Valid AdminPanelMissingWordsQueueModel adminPanelMissingWordsQueueModel,
			BindingResult result, Map<String, Object> model) throws Exception {

    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_GET_MISSING_WORDS_QUEUE);
    	
    	model.put("selectedMenu", "missingWordsQueuePanel");
    	model.put("command2", adminPanelMissingWordsQueueModel);
    	
    	if (result.hasErrors() == true) {
    		    		    		
    		adminLoggerModel.setResult(Result.ERROR);
    		
    		adminLoggerModel.addParam("size", adminPanelMissingWordsQueueModel.getSize());
    		adminLoggerModel.addParam("showMaxSize", adminPanelMissingWordsQueueModel.getShowMaxSize());
    		adminLoggerModel.addParam("wordList", adminPanelMissingWordsQueueModel.getWordList());
    		adminLoggerModel.addParam("lock", Boolean.valueOf(adminPanelMissingWordsQueueModel.isLock()));
    		
    		// logowanie
    		loggerSender.sendLog(adminLoggerModel);
    		
    		return "admMissingWordsQueuePanel";
    		
    	} else {
    		
    		Timestamp lockTimestamp = new Timestamp(new Date().getTime());
    		
    		adminLoggerModel.setResult(Result.OK);
    		
    		boolean isLock = adminPanelMissingWordsQueueModel.isLock();
    		
    		adminLoggerModel.addParam("size", adminPanelMissingWordsQueueModel.getSize());
    		adminLoggerModel.addParam("showMaxSize", adminPanelMissingWordsQueueModel.getShowMaxSize());
    		adminLoggerModel.addParam("wordList", adminPanelMissingWordsQueueModel.getWordList());
    		adminLoggerModel.addParam("lock", Boolean.valueOf(isLock));
    		
    		if (isLock == true) {
    			adminLoggerModel.addParam("lockTimestamp", lockTimestamp);
    		}
    		
    		// pobieranie brakujacych slow
    		List<String> wordListAsList = adminPanelMissingWordsQueueModel.getWordListAsList();
    		
    		// !!! uwaga !!! jesli cos tu zmieniasz to zmien rowniez w klasie LoggerListener, obsluga ADMIN_REQUEST
    		List<WordDictionarySearchMissingWordQueue> unlockedWordDictionarySearchMissingWordQueue = null;
    		
    		if (wordListAsList.size() == 0) { // pobieranie listy slow wedlug liczby
    			
        		unlockedWordDictionarySearchMissingWordQueue = 
        				mySQLConnector.getUnlockedWordDictionarySearchMissingWordQueue(Integer.parseInt(adminPanelMissingWordsQueueModel.getSize()));    			
    			
    		} else {
    			
        		unlockedWordDictionarySearchMissingWordQueue = 
        				mySQLConnector.getUnlockedWordDictionarySearchMissingWordQueue(wordListAsList);    			
    			
    		}    
    		
    		adminLoggerModel.addParam("unlockedWordDictionarySearchMissingWordQueue", new AdminLoggerModel.ObjectWrapper("<WordDictionarySearchMissingWordQueue list> ", unlockedWordDictionarySearchMissingWordQueue));
    		
    		//
    		
    		Report generateMissingWordsQueueReportBody = reportGenerator.generateMissingWordsQueueReportBody(unlockedWordDictionarySearchMissingWordQueue, Long.parseLong(adminPanelMissingWordsQueueModel.getShowMaxSize()));
    		
    		model.put("reportBody", generateMissingWordsQueueReportBody.body);

    		model.put("pageTitle", messageSource.getMessage("admin.panel.showMissingWordsQueueReport.title", new Object[] { }, Locale.getDefault()));
    		model.put("pageDescription", "");
    		    		    		
    		// logowanie
    		loggerSender.sendLog(adminLoggerModel);
    		
    		return "admShowMissingWordsQueueReport";
    	}
    }
}
