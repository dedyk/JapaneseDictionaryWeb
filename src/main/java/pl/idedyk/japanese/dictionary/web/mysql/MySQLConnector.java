package pl.idedyk.japanese.dictionary.web.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import pl.idedyk.japanese.dictionary.web.mysql.model.DailyLogProcessedMinMaxIds;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyReportSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetectLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryRadicalsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.SuggestionSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordKanjiSearchNoFoundStat;
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
	
	public void insertGenericLog(GenericLog genericLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into generic_log(timestamp, session_id, user_agent, remote_ip, remote_host, operation) "
					+ "values(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setTimestamp(1, genericLog.getTimestamp());
			preparedStatement.setString(2, genericLog.getSessionId());
			preparedStatement.setString(3, genericLog.getUserAgent());
			preparedStatement.setString(4, genericLog.getRemoteIp());
			preparedStatement.setString(5, genericLog.getRemoteHost());
			preparedStatement.setString(6, genericLog.getOperation().toString());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			genericLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}
	
	public void insertWordDictionaryAutocompleteLog(WordDictionaryAutocompleteLog wordDictionaryAutocompleteLog) throws SQLException {

		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_autocomplete_log(generic_log_id, term, found_elements) "
					+ "values(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, wordDictionaryAutocompleteLog.getGenericLogId());
			preparedStatement.setString(2, wordDictionaryAutocompleteLog.getTerm());
			preparedStatement.setInt(3, wordDictionaryAutocompleteLog.getFoundElements());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			wordDictionaryAutocompleteLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}
	
	public void insertWordDictionarySearchLog(WordDictionarySearchLog wordDictionarySearchLog) throws SQLException {
				
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_search_log(generic_log_id, find_word_request_word, find_word_request_search_kanji, "
					+ "find_word_request_search_kana, find_word_request_search_romaji, find_word_request_search_translate, find_word_request_search_info, find_word_request_word_place, "
					+ "find_word_request_dictionary_entry_type_list, find_word_result_result_size) "
					+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, wordDictionarySearchLog.getGenericLogId());
			
			preparedStatement.setString(2, wordDictionarySearchLog.getFindWordRequestWord());
			
			preparedStatement.setBoolean(3, wordDictionarySearchLog.getFindWordRequestKanji());
			preparedStatement.setBoolean(4, wordDictionarySearchLog.getFindWordRequestKana());
			preparedStatement.setBoolean(5, wordDictionarySearchLog.getFindWordRequestRomaji());
			preparedStatement.setBoolean(6, wordDictionarySearchLog.getFindWordRequestTranslate());
			preparedStatement.setBoolean(7, wordDictionarySearchLog.getFindWordRequestInfo());
			
			preparedStatement.setString(8, wordDictionarySearchLog.getFindWordRequestWordPlace());
			
			preparedStatement.setString(9, wordDictionarySearchLog.getFindWordRequestDictionaryEntryTypeList());
			
			preparedStatement.setInt(10, wordDictionarySearchLog.getFindWordResultResultSize());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			wordDictionarySearchLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}
		
	public void insertWordDictionaryDetailsLog(WordDictionaryDetailsLog wordDictionaryDetailsLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_details_log(generic_log_id, dictionary_entry_id, "
					+ "dictionary_entry_kanji, dictionary_entry_kanaList, dictionary_entry_romajiList, "
					+ "dictionary_entry_translateList, dictionary_entry_info) "
					+ "values(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, wordDictionaryDetailsLog.getGenericLogId());
			
			preparedStatement.setInt(2, wordDictionaryDetailsLog.getDictionaryEntryId());
			
			preparedStatement.setString(3, wordDictionaryDetailsLog.getDictionaryEntryKanji());
			preparedStatement.setString(4, wordDictionaryDetailsLog.getDictionaryEntryKanaList());
			preparedStatement.setString(5, wordDictionaryDetailsLog.getDictionaryEntryRomajiList());
			preparedStatement.setString(6, wordDictionaryDetailsLog.getDictionaryEntryTranslateList());
			preparedStatement.setString(7, wordDictionaryDetailsLog.getDictionaryEntryInfo());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			wordDictionaryDetailsLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}
	
	public void insertKanjiDictionaryAutocompleteLog(KanjiDictionaryAutocompleteLog kanjiDictionaryAutocompleteLog) throws SQLException {

		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into kanji_dictionary_autocomplete_log(generic_log_id, term, found_elements) "
					+ "values(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, kanjiDictionaryAutocompleteLog.getGenericLogId());
			preparedStatement.setString(2, kanjiDictionaryAutocompleteLog.getTerm());
			preparedStatement.setInt(3, kanjiDictionaryAutocompleteLog.getFoundElements());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			kanjiDictionaryAutocompleteLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}

	public void insertKanjiDictionarySearchLog(KanjiDictionarySearchLog kanjiDictionarySearchLog) throws SQLException {

		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
						
			preparedStatement = connection.prepareStatement( "insert into kanji_dictionary_search_log(generic_log_id, find_kanji_request_word, find_kanji_request_word_place, "
					+ "find_kanji_request_stroke_count_from, find_kanji_request_stroke_count_to, find_kanji_result_result_size) "
					+ "values(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, kanjiDictionarySearchLog.getGenericLogId());
			preparedStatement.setString(2, kanjiDictionarySearchLog.getFindKanjiRequestWord());
			preparedStatement.setString(3, kanjiDictionarySearchLog.getFindKanjiRequestWordPlace());
			
			if (kanjiDictionarySearchLog.getFindKanjiRequestStrokeCountFrom() != null) {
				preparedStatement.setInt(4, kanjiDictionarySearchLog.getFindKanjiRequestStrokeCountFrom());
			} else {
				preparedStatement.setNull(4, Types.INTEGER);
			}
			
			if (kanjiDictionarySearchLog.getFindKanjiRequestStrokeCountTo() != null) {
				preparedStatement.setInt(5, kanjiDictionarySearchLog.getFindKanjiRequestStrokeCountTo());
			} else {
				preparedStatement.setNull(5, Types.INTEGER);
			}			
			
			preparedStatement.setInt(6, kanjiDictionarySearchLog.getFindKanjiResultResultSize());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			kanjiDictionarySearchLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}
	
	public void insertKanjiDictionaryRadicalsLog(KanjiDictionaryRadicalsLog kanjiDictionaryRadicalsLog) throws SQLException {

		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into kanji_dictionary_radicals_log(generic_log_id, radicals, found_elements) "
					+ "values(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, kanjiDictionaryRadicalsLog.getGenericLogId());
			preparedStatement.setString(2, kanjiDictionaryRadicalsLog.getRadicals());
			preparedStatement.setInt(3, kanjiDictionaryRadicalsLog.getFoundElements());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			kanjiDictionaryRadicalsLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}

	public void insertKanjiDictionaryDetectLog(KanjiDictionaryDetectLog kanjiDictionaryDetectLog) throws SQLException {

		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into kanji_dictionary_detect_log(generic_log_id, strokes, detect_kanji_result) "
					+ "values(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, kanjiDictionaryDetectLog.getGenericLogId());
			preparedStatement.setString(2, kanjiDictionaryDetectLog.getStrokes());
			preparedStatement.setString(3, kanjiDictionaryDetectLog.getDetectKanjiResult());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			kanjiDictionaryDetectLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}

	public void insertKanjiDictionaryDetailsLog(KanjiDictionaryDetailsLog kanjiDictionaryDetailsLog) throws SQLException {

		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into kanji_dictionary_details_log(generic_log_id, kanji_entry_id, kanji_entry_kanji, kanji_entry_translateList, kanji_entry_info) "
					+ "values(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, kanjiDictionaryDetailsLog.getGenericLogId());
			preparedStatement.setInt(2, kanjiDictionaryDetailsLog.getKanjiEntryId());
			preparedStatement.setString(3, kanjiDictionaryDetailsLog.getKanjiEntryKanji());
			preparedStatement.setString(4, kanjiDictionaryDetailsLog.getKanjiEntryTranslateList());
			preparedStatement.setString(5, kanjiDictionaryDetailsLog.getKanjiEntryInfo());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			kanjiDictionaryDetailsLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}
	
	public void insertSuggestionSendLoggerModel(SuggestionSendLog suggestionSendLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into suggestion_send_log(generic_log_id, title, sender, body) "
					+ "values(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, suggestionSendLog.getGenericLogId());
			preparedStatement.setString(2, suggestionSendLog.getTitle());
			preparedStatement.setString(3, suggestionSendLog.getSender());
			preparedStatement.setString(4, suggestionSendLog.getBody());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			suggestionSendLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}

	public void insertDailyReportSendLog(DailyReportSendLog dailyReportSendLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into daily_report_log(generic_log_id, title, report) "
					+ "values(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, dailyReportSendLog.getGenericLogId());
			preparedStatement.setString(2, dailyReportSendLog.getTitle());
			preparedStatement.setString(3, dailyReportSendLog.getReport());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			dailyReportSendLog.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}
	
	public DailyLogProcessedMinMaxIds getCurrentDailyLogProcessedMinMaxIds() throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "select min(gl.id), min(gl.timestamp), max(gl.id), max(gl.timestamp) from generic_log gl where gl.id not in (select dlpi.id from daily_log_processed_ids dlpi where dlpi.id = gl.id)");
			
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next() == false) {
				return null;
			}
			
			Long minId = resultSet.getLong(1);
			Date minDate = resultSet.getTimestamp(2);
			
			Long maxId = resultSet.getLong(3);
			Date maxDate = resultSet.getTimestamp(4);
			
			if (minId == null || minId == 0 || maxId == null || maxId == 0) {
				return null;
			}
			
			DailyLogProcessedMinMaxIds result = new DailyLogProcessedMinMaxIds();
			
			result.setMinId(minId);
			result.setMinDate(minDate);
			
			result.setMaxId(maxId);
			result.setMaxDate(maxDate);
			
			return result;
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}
	}
	
	public List<GenericLogOperationStat> getGenericLogOperationStat(long startId, long endId) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		try {
			List<GenericLogOperationStat> result = new ArrayList<GenericLogOperationStat>();
			
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "select operation, count(*) from generic_log where id >= ? and id <= ? group by operation order by 2 desc");
			
			preparedStatement.setLong(1, startId);
			preparedStatement.setLong(2, endId);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {
				
				GenericLogOperationStat genericLogOperationStat = new GenericLogOperationStat();
				
				genericLogOperationStat.setOperation(GenericLogOperationEnum.valueOf(resultSet.getString(1)));
				genericLogOperationStat.setStat(resultSet.getLong(2));
				
				result.add(genericLogOperationStat);
			}
			
			return result;
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}

	public List<WordKanjiSearchNoFoundStat> getWordDictionarySearchNoFoundStat(long startId, long endId) throws SQLException {
	
		return getGenericSearchNoFoundStat("select find_word_request_word, count(*) from word_dictionary_search_log "
				+ "where find_word_result_result_size = 0 and generic_log_id >= ? and generic_log_id <= ? group by find_word_request_word order by 2 desc, 1", startId, endId);		
	}
	
	public List<WordKanjiSearchNoFoundStat> getWordDictionaryAutocompleteNoFoundStat(long startId, long endId) throws SQLException {
		
		return getGenericSearchNoFoundStat("select term, count(*) from word_dictionary_autocomplete_log where found_elements = 0 "
				+ "and generic_log_id >= ? and generic_log_id <= ? group by term order by 2 desc, 1 desc", startId, endId);
	}

	public List<WordKanjiSearchNoFoundStat> getKanjiDictionarySearchNoFoundStat(long startId, long endId) throws SQLException {
		
		return getGenericSearchNoFoundStat("select find_kanji_request_word, count(*) from kanji_dictionary_search_log "
				+ "where find_kanji_result_result_size = 0 and generic_log_id >= ? and generic_log_id <= ? group by find_kanji_request_word order by 2 desc, 1", startId, endId);		
	}
	
	public List<WordKanjiSearchNoFoundStat> getKanjiDictionaryAutocompleteNoFoundStat(long startId, long endId) throws SQLException {
		
		return getGenericSearchNoFoundStat("select term, count(*) from kanji_dictionary_autocomplete_log where found_elements = 0 "
				+ "and generic_log_id >= ? and generic_log_id <= ? group by term order by 2 desc, 1 desc", startId, endId);
	}
	
	public void blockDailyLogProcessedIds(long startId, long endId) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
				
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement("insert into daily_log_processed_ids select id from generic_log where id >= ? and id <= ?");
			
			preparedStatement.setLong(1, startId);
			preparedStatement.setLong(2, endId);
			
			preparedStatement.executeUpdate();
			
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}		
	}

	private List<WordKanjiSearchNoFoundStat> getGenericSearchNoFoundStat(String sql, long startId, long endId) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		try {
			List<WordKanjiSearchNoFoundStat> result = new ArrayList<WordKanjiSearchNoFoundStat>();
			
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement(sql);
			
			preparedStatement.setLong(1, startId);
			preparedStatement.setLong(2, endId);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {
				
				WordKanjiSearchNoFoundStat wordKanjiSearchNoFoundStat = new WordKanjiSearchNoFoundStat();
				
				wordKanjiSearchNoFoundStat.setWord(resultSet.getString(1));
				wordKanjiSearchNoFoundStat.setStat(resultSet.getLong(2));
				
				result.add(wordKanjiSearchNoFoundStat);
			}
			
			return result;
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
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
