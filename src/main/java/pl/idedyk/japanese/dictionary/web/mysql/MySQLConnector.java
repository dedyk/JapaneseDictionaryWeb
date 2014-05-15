package pl.idedyk.japanese.dictionary.web.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import snaq.db.ConnectionPool;

public class MySQLConnector {

	private static final Logger logger = Logger.getLogger(MySQLConnector.class);
	
	private String url;
	
	private String user;
	private String password;
	
	private int minPool;
	private int maxPool;
	
	private int maxSize;
	
	private int idleTimeout;
	
	private ConnectionPool connectionPool;

	@PostConstruct
	public void init() {
		
		logger.info("Inicjalizacja MySQLConnector");
		
		try {		
			Class<?> mysqlJdbcClass = Class.forName("com.mysql.jdbc.Driver");
			
			Driver driver = (Driver)mysqlJdbcClass.newInstance();
			DriverManager.registerDriver(driver);
			
			connectionPool = new ConnectionPool( "mysql",  minPool, maxPool, maxSize, idleTimeout, url, user, password);
			
			
		} catch (Exception e) {
			logger.error("Bład inicjalizacji MySQLConnector", e);
			
			throw new RuntimeException(e);			
		}
	}
	
	@PreDestroy
	private void close() throws IOException {

		if (connectionPool != null) {
			connectionPool.release();
		}
	}
	
	public void test() {
		
		int fixme = 1;
		// testy
		
		try {
			Connection connection = connectionPool.getConnection();
			
			PreparedStatement prepareStatement = connection.prepareStatement("select curtime()");
			
			ResultSet resultSet = prepareStatement.executeQuery();
			
			resultSet.first();
			
			Time time = resultSet.getTime(1);
			
			logger.info("Test: " + time);
			
			resultSet.close();
			prepareStatement.close();
			connection.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMinPool() {
		return minPool;
	}

	public void setMinPool(int minPool) {
		this.minPool = minPool;
	}

	public int getMaxPool() {
		return maxPool;
	}

	public void setMaxPool(int maxPool) {
		this.maxPool = maxPool;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}
}

/*
DBPoolDataSource ds = new DBPoolDataSource();
ds.setName("pool-ds");
ds.setDescription("Pooling DataSource");
ds.setDriverClassName("com.mysql.jdbc.Driver");
ds.setUrl("jdbc:mysql://192.168.1.101:3306/ReplicantDB");
ds.setUser("Deckard");
ds.setPassword("TyrellCorp1982");
ds.setMinPool(5);
ds.setMaxPool(10);
ds.setMaxSize(30);
ds.setIdleTimeout(3600);  // Specified in seconds.
ds.setValidationQuery("SELECT COUNT(*) FROM Replicants");
*/