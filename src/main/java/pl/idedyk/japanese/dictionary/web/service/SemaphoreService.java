package pl.idedyk.japanese.dictionary.web.service;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;

@Service
public class SemaphoreService {
	
	// private static final Logger logger = Logger.getLogger(SemaphoreService.class);

	@Autowired
	private MySQLConnector mySQLConnector;

	public boolean canDoOperation(String semaphoreName, int lockLengthInSeconds) throws SQLException {		
		return mySQLConnector.canDoOperation(semaphoreName, lockLengthInSeconds);		
	}
}
