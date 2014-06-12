package pl.idedyk.japanese.dictionary.web.logger;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

@Service
public class LoggerErrorHandler implements ErrorHandler {

	private static final Logger logger = Logger.getLogger(LoggerErrorHandler.class);
	
	@Override
	public void handleError(Throwable throwable) {
		
		logger.error("Blad podczas obslugi logowania", throwable);		
	}
}
