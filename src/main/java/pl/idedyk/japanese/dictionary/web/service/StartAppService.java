package pl.idedyk.japanese.dictionary.web.service;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.controller.StartController;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.StartAppLoggerModel;

@Service
public class StartAppService {
	
	private static final Logger logger = LogManager.getLogger(StartController.class);

	@Autowired
	private LoggerSender loggerSender;
	
	@PostConstruct
	public void init() {
		
		logger.info("Wysyłanie info o starcie aplikacji");
		
		// logowanie
		loggerSender.sendLog(new StartAppLoggerModel(null));		
	}
}
