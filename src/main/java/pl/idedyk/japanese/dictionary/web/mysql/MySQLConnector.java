package pl.idedyk.japanese.dictionary.web.mysql;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import pl.idedyk.japanese.dictionary.web.mysql.model.AdminRequestLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidGetSpellCheckerSuggestionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidQueueEventLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.AndroidSendMissingWordLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyLogProcessedMinMaxIds;
import pl.idedyk.japanese.dictionary.web.mysql.model.DailyReportSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GeneralExceptionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericTextStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryDetectLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionaryRadicalsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.KanjiDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItem;
import pl.idedyk.japanese.dictionary.web.mysql.model.QueueItemStatus;
import pl.idedyk.japanese.dictionary.web.mysql.model.RemoteClientStat;
import pl.idedyk.japanese.dictionary.web.mysql.model.SuggestionSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryAutocompleteLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameCatalogLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryNameDetailsLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionarySearchMissingWordQueue;
import pl.idedyk.japanese.dictionary.web.mysql.model.WordDictionaryUniqueSearch;
import snaq.db.AutoCommitValidator;
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
			
			connectionPool.setValidator(new AutoCommitValidator());
			
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
			
			preparedStatement = connection.prepareStatement( "insert into generic_log(timestamp, session_id, user_agent, request_url, referer_url, remote_ip, remote_host, operation) "
					+ "values(?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setTimestamp(1, genericLog.getTimestamp());
			preparedStatement.setString(2, genericLog.getSessionId());
			preparedStatement.setString(3, genericLog.getUserAgent());
			preparedStatement.setString(4, genericLog.getRequestURL());
			preparedStatement.setString(5, genericLog.getRefererURL());
			preparedStatement.setString(6, genericLog.getRemoteIp());
			preparedStatement.setString(7, genericLog.getRemoteHost());
			preparedStatement.setString(8, genericLog.getOperation().toString());
			
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
	
	public long getGenericLogSize(List<String> genericLogOperationStringList) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		if (genericLogOperationStringList == null || genericLogOperationStringList.size() == 0) {
			return 0;
		}
		
		try {
			connection = connectionPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			
			sql.append("select count(*) from generic_log where operation in (");
			
			for (int idxGenericLogOperationStringList = 0; idxGenericLogOperationStringList < genericLogOperationStringList.size(); ++idxGenericLogOperationStringList) {
				
				sql.append(" ? ");
				
				if (idxGenericLogOperationStringList != genericLogOperationStringList.size() - 1) {
					sql.append(" , ");
				}
			}
			
			sql.append(" ) ");
			
			preparedStatement = connection.prepareStatement(sql.toString());
			
			for (int idxGenericLogOperationStringList = 0; idxGenericLogOperationStringList < genericLogOperationStringList.size(); ++idxGenericLogOperationStringList) {
				preparedStatement.setString(idxGenericLogOperationStringList + 1, genericLogOperationStringList.get(idxGenericLogOperationStringList));
			}
			
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next() == false) {
				throw new RuntimeException();
			}
			
			Long result = resultSet.getLong(1);
						
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
	
	public List<GenericLog> getGenericLogList(long startPos, int size, List<String> genericLogOperationStringList) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		List<GenericLog> result = new ArrayList<GenericLog>();
		
		if (genericLogOperationStringList == null || genericLogOperationStringList.size() == 0) {
			return result;
		}
		
		try {						
			connection = connectionPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			
			sql.append("select id, timestamp, session_id, user_agent, request_url, referer_url, remote_ip, remote_host, operation "
					+ "from generic_log where operation in ( ");
			
			for (int idxGenericLogOperationStringList = 0; idxGenericLogOperationStringList < genericLogOperationStringList.size(); ++idxGenericLogOperationStringList) {
				
				sql.append(" ? ");
				
				if (idxGenericLogOperationStringList != genericLogOperationStringList.size() - 1) {
					sql.append(" , ");
				}
			}
			
			sql.append(" ) order by id desc limit ?, ?");
			
			preparedStatement = connection.prepareStatement(sql.toString());
						
			for (int idxGenericLogOperationStringList = 0; idxGenericLogOperationStringList < genericLogOperationStringList.size(); ++idxGenericLogOperationStringList) {
				preparedStatement.setString(idxGenericLogOperationStringList + 1, genericLogOperationStringList.get(idxGenericLogOperationStringList));
			}
			
			preparedStatement.setLong(genericLogOperationStringList.size() + 1, startPos * size);
			preparedStatement.setLong(genericLogOperationStringList.size() + 2, size);

			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {
				
				GenericLog genericLog = createGenericLogFromResultSet(resultSet);
								
				result.add(genericLog);
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
	
	public GenericLog getGenericLog(long id) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
				
		try {						
			connection = connectionPool.getConnection();
						
			preparedStatement = connection.prepareStatement("select id, timestamp, session_id, user_agent, request_url, referer_url, remote_ip, remote_host, operation "
					+ "from generic_log where id = ?");
						
			preparedStatement.setLong(1, id);
			
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next() == true) {				
				return createGenericLogFromResultSet(resultSet);
				
			} else {
				return null;
			}
						
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
	
	private GenericLog createGenericLogFromResultSet(ResultSet resultSet) throws SQLException {
		
		GenericLog genericLog = new GenericLog();
		
		genericLog.setId(resultSet.getLong("id"));
		genericLog.setTimestamp(resultSet.getTimestamp("timestamp"));
		genericLog.setSessionId(resultSet.getString("session_id"));
		genericLog.setUserAgent(resultSet.getString("user_agent"));
		genericLog.setRequestURL(resultSet.getString("request_url"));
		genericLog.setRefererURL(resultSet.getString("referer_url"));
		genericLog.setRemoteIp(resultSet.getString("remote_ip"));
		genericLog.setRemoteHost(resultSet.getString("remote_host"));
		genericLog.setOperation(GenericLogOperationEnum.valueOf(resultSet.getString("operation")));
		
		return genericLog;
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
	
	public WordDictionaryAutocompleteLog getWordDictionaryAutocompleteLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, term, found_elements "
					+ "from word_dictionary_autocomplete_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createWordDictionaryAutocompleteLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private WordDictionaryAutocompleteLog createWordDictionaryAutocompleteLogFromResultSet(ResultSet resultSet) throws SQLException {

		WordDictionaryAutocompleteLog wordDictionaryAutocompleteLog = new WordDictionaryAutocompleteLog();

		wordDictionaryAutocompleteLog.setId(resultSet.getLong("id"));
		wordDictionaryAutocompleteLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		wordDictionaryAutocompleteLog.setTerm(resultSet.getString("term"));
		wordDictionaryAutocompleteLog.setFoundElements((Integer)resultSet.getObject("found_elements"));
		
		return wordDictionaryAutocompleteLog;
	}
	
	public void insertWordDictionarySearchLog(WordDictionarySearchLog wordDictionarySearchLog) throws SQLException {
				
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_search_log(generic_log_id, find_word_request_word, find_word_request_search_kanji, "
					+ "find_word_request_search_kana, find_word_request_search_romaji, find_word_request_search_translate, find_word_request_search_info, find_word_request_search_only_common_words, "
					+ "find_word_request_word_place, find_word_request_dictionary_entry_type_list, find_word_result_result_size, priority) "
					+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, wordDictionarySearchLog.getGenericLogId());
			
			preparedStatement.setString(2, wordDictionarySearchLog.getFindWordRequestWord());
			
			preparedStatement.setBoolean(3, wordDictionarySearchLog.getFindWordRequestKanji());
			preparedStatement.setBoolean(4, wordDictionarySearchLog.getFindWordRequestKana());
			preparedStatement.setBoolean(5, wordDictionarySearchLog.getFindWordRequestRomaji());
			preparedStatement.setBoolean(6, wordDictionarySearchLog.getFindWordRequestTranslate());
			preparedStatement.setBoolean(7, wordDictionarySearchLog.getFindWordRequestInfo());
			
			preparedStatement.setBoolean(8, wordDictionarySearchLog.getFindWordRequestOnlyCommonWords());
			
			preparedStatement.setString(9, wordDictionarySearchLog.getFindWordRequestWordPlace());
			
			preparedStatement.setString(10, wordDictionarySearchLog.getFindWordRequestDictionaryEntryTypeList());
			
			preparedStatement.setInt(11, wordDictionarySearchLog.getFindWordResultResultSize());
			
			preparedStatement.setInt(12, wordDictionarySearchLog.getPriority());
						
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
	
	public WordDictionarySearchLog getWordDictionarySearchLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, find_word_request_word, find_word_request_search_kanji, "
					+ "find_word_request_search_kana, find_word_request_search_romaji, find_word_request_search_translate, find_word_request_search_info, find_word_request_search_only_common_words, "
					+ "find_word_request_word_place, find_word_request_dictionary_entry_type_list, find_word_result_result_size, priority "
					+ "from word_dictionary_search_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createWordDictionarySearchLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private WordDictionarySearchLog createWordDictionarySearchLogFromResultSet(ResultSet resultSet) throws SQLException {

		WordDictionarySearchLog wordDictionarySearchLog = new WordDictionarySearchLog();

		wordDictionarySearchLog.setId(resultSet.getLong("id"));
		wordDictionarySearchLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		
		wordDictionarySearchLog.setFindWordRequestWord(resultSet.getString("find_word_request_word"));
		wordDictionarySearchLog.setFindWordRequestKanji(resultSet.getBoolean("find_word_request_search_kanji"));
		wordDictionarySearchLog.setFindWordRequestKana(resultSet.getBoolean("find_word_request_search_kana"));
		wordDictionarySearchLog.setFindWordRequestRomaji(resultSet.getBoolean("find_word_request_search_romaji"));
		wordDictionarySearchLog.setFindWordRequestTranslate(resultSet.getBoolean("find_word_request_search_translate"));
		wordDictionarySearchLog.setFindWordRequestInfo(resultSet.getBoolean("find_word_request_search_info"));
		wordDictionarySearchLog.setFindWordRequestOnlyCommonWords(resultSet.getBoolean("find_word_request_search_only_common_words"));
		wordDictionarySearchLog.setFindWordRequestWordPlace(resultSet.getString("find_word_request_word_place"));
		wordDictionarySearchLog.setFindWordRequestDictionaryEntryTypeList(resultSet.getString("find_word_request_dictionary_entry_type_list"));
		wordDictionarySearchLog.setFindWordResultResultSize((Integer)resultSet.getObject("find_word_result_result_size"));
		wordDictionarySearchLog.setPriority(resultSet.getInt("priority"));

		return wordDictionarySearchLog;
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
	
	public WordDictionaryDetailsLog getWordDictionaryDetailsLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, dictionary_entry_id, "
					+ "dictionary_entry_kanji, dictionary_entry_kanaList, dictionary_entry_romajiList, "
					+ "dictionary_entry_translateList, dictionary_entry_info "
					+ "from word_dictionary_details_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createWordDictionaryDetailsLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private WordDictionaryDetailsLog createWordDictionaryDetailsLogFromResultSet(ResultSet resultSet) throws SQLException {

		WordDictionaryDetailsLog wordDictionaryDetailsLog = new WordDictionaryDetailsLog();

		wordDictionaryDetailsLog.setId(resultSet.getLong("id"));
		wordDictionaryDetailsLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		wordDictionaryDetailsLog.setDictionaryEntryId((Integer)resultSet.getObject("dictionary_entry_id"));
		wordDictionaryDetailsLog.setDictionaryEntryKanji(resultSet.getString("dictionary_entry_kanji"));
		wordDictionaryDetailsLog.setDictionaryEntryKanaList(resultSet.getString("dictionary_entry_kanaList"));
		wordDictionaryDetailsLog.setDictionaryEntryRomajiList(resultSet.getString("dictionary_entry_romajiList"));
		wordDictionaryDetailsLog.setDictionaryEntryTranslateList(resultSet.getString("dictionary_entry_translateList"));
		wordDictionaryDetailsLog.setDictionaryEntryInfo(resultSet.getString("dictionary_entry_info"));

		return wordDictionaryDetailsLog;
	}
	
	public void insertWordDictionaryCatalogLog(WordDictionaryCatalogLog wordDictionaryCatalogLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_catalog_log(generic_log_id, page_no) "
					+ "values(?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, wordDictionaryCatalogLog.getGenericLogId());
			preparedStatement.setInt(2, wordDictionaryCatalogLog.getPageNo());
						
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			wordDictionaryCatalogLog.setId(generatedKeys.getLong(1));
			
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
	
	public WordDictionaryCatalogLog getWordDictionaryCatalogLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, page_no "
					+ "from word_dictionary_catalog_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createWordDictionaryCatalogLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private WordDictionaryCatalogLog createWordDictionaryCatalogLogFromResultSet(ResultSet resultSet) throws SQLException {

		WordDictionaryCatalogLog wordDictionaryCatalogLog = new WordDictionaryCatalogLog();

		wordDictionaryCatalogLog.setId(resultSet.getLong("id"));
		wordDictionaryCatalogLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		wordDictionaryCatalogLog.setPageNo((Integer)resultSet.getObject("page_no"));

		return wordDictionaryCatalogLog;
	}
	
	public void insertWordDictionaryNameDetailsLog(WordDictionaryNameDetailsLog wordDictionaryNameDetailsLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_name_details_log(generic_log_id, dictionary_entry_id, "
					+ "dictionary_entry_kanji, dictionary_entry_kanaList, dictionary_entry_romajiList, "
					+ "dictionary_entry_translateList, dictionary_entry_info) "
					+ "values(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, wordDictionaryNameDetailsLog.getGenericLogId());
			
			preparedStatement.setInt(2, wordDictionaryNameDetailsLog.getDictionaryEntryId());
			
			preparedStatement.setString(3, wordDictionaryNameDetailsLog.getDictionaryEntryKanji());
			preparedStatement.setString(4, wordDictionaryNameDetailsLog.getDictionaryEntryKanaList());
			preparedStatement.setString(5, wordDictionaryNameDetailsLog.getDictionaryEntryRomajiList());
			preparedStatement.setString(6, wordDictionaryNameDetailsLog.getDictionaryEntryTranslateList());
			preparedStatement.setString(7, wordDictionaryNameDetailsLog.getDictionaryEntryInfo());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			wordDictionaryNameDetailsLog.setId(generatedKeys.getLong(1));
			
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
	
	public WordDictionaryNameDetailsLog getWordDictionaryNameDetailsLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, dictionary_entry_id, "
					+ "dictionary_entry_kanji, dictionary_entry_kanaList, dictionary_entry_romajiList, "
					+ "dictionary_entry_translateList, dictionary_entry_info "
					+ "from word_dictionary_name_details_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createWordDictionaryNameDetailsLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private WordDictionaryNameDetailsLog createWordDictionaryNameDetailsLogFromResultSet(ResultSet resultSet) throws SQLException {

		WordDictionaryNameDetailsLog wordDictionaryNameDetailsLog = new WordDictionaryNameDetailsLog();

		wordDictionaryNameDetailsLog.setId(resultSet.getLong("id"));
		wordDictionaryNameDetailsLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		wordDictionaryNameDetailsLog.setDictionaryEntryId((Integer)resultSet.getObject("dictionary_entry_id"));
		wordDictionaryNameDetailsLog.setDictionaryEntryKanji(resultSet.getString("dictionary_entry_kanji"));
		wordDictionaryNameDetailsLog.setDictionaryEntryKanaList(resultSet.getString("dictionary_entry_kanaList"));
		wordDictionaryNameDetailsLog.setDictionaryEntryRomajiList(resultSet.getString("dictionary_entry_romajiList"));
		wordDictionaryNameDetailsLog.setDictionaryEntryTranslateList(resultSet.getString("dictionary_entry_translateList"));
		wordDictionaryNameDetailsLog.setDictionaryEntryInfo(resultSet.getString("dictionary_entry_info"));

		return wordDictionaryNameDetailsLog;
	}
	
	public void insertWordDictionaryNameCatalogLog(WordDictionaryNameCatalogLog wordDictionaryNameCatalogLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_name_catalog_log(generic_log_id, page_no) "
					+ "values(?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, wordDictionaryNameCatalogLog.getGenericLogId());
			preparedStatement.setInt(2, wordDictionaryNameCatalogLog.getPageNo());
						
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			wordDictionaryNameCatalogLog.setId(generatedKeys.getLong(1));
			
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
	
	public WordDictionaryNameCatalogLog getWordDictionaryNameCatalogLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, page_no "
					+ "from word_dictionary_name_catalog_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createWordDictionaryNameCatalogLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private WordDictionaryNameCatalogLog createWordDictionaryNameCatalogLogFromResultSet(ResultSet resultSet) throws SQLException {

		WordDictionaryNameCatalogLog wordDictionaryNameCatalogLog = new WordDictionaryNameCatalogLog();

		wordDictionaryNameCatalogLog.setId(resultSet.getLong("id"));
		wordDictionaryNameCatalogLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		wordDictionaryNameCatalogLog.setPageNo((Integer)resultSet.getObject("page_no"));

		return wordDictionaryNameCatalogLog;
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
	
	public KanjiDictionaryAutocompleteLog getKanjiDictionaryAutocompleteLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, term, found_elements "
					+ "from kanji_dictionary_autocomplete_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createKanjiDictionaryAutocompleteLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private KanjiDictionaryAutocompleteLog createKanjiDictionaryAutocompleteLogFromResultSet(ResultSet resultSet) throws SQLException {

		KanjiDictionaryAutocompleteLog kanjiDictionaryAutocompleteLog = new KanjiDictionaryAutocompleteLog();

		kanjiDictionaryAutocompleteLog.setId(resultSet.getLong("id"));
		kanjiDictionaryAutocompleteLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		kanjiDictionaryAutocompleteLog.setTerm(resultSet.getString("term"));
		kanjiDictionaryAutocompleteLog.setFoundElements((Integer)resultSet.getObject("found_elements"));
		
		return kanjiDictionaryAutocompleteLog;
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
	
	public KanjiDictionarySearchLog getKanjiDictionarySearchLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, find_kanji_request_word, find_kanji_request_word_place, "
					+ "find_kanji_request_stroke_count_from, find_kanji_request_stroke_count_to, find_kanji_result_result_size "
					+ "from kanji_dictionary_search_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createKanjiDictionarySearchLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private KanjiDictionarySearchLog createKanjiDictionarySearchLogFromResultSet(ResultSet resultSet) throws SQLException {

		KanjiDictionarySearchLog kanjiDictionarySearchLog = new KanjiDictionarySearchLog();

		kanjiDictionarySearchLog.setId(resultSet.getLong("id"));
		kanjiDictionarySearchLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		kanjiDictionarySearchLog.setFindKanjiRequestWord(resultSet.getString("find_kanji_request_word"));
		kanjiDictionarySearchLog.setFindKanjiRequestWordPlace(resultSet.getString("find_kanji_request_word_place"));
		kanjiDictionarySearchLog.setFindKanjiRequestStrokeCountFrom((Integer)resultSet.getObject("find_kanji_request_stroke_count_from"));
		kanjiDictionarySearchLog.setFindKanjiRequestStrokeCountTo((Integer)resultSet.getObject("find_kanji_request_stroke_count_to"));
		kanjiDictionarySearchLog.setFindKanjiResultResultSize((Integer)resultSet.getObject("find_kanji_result_result_size"));

		return kanjiDictionarySearchLog;
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
	
	public KanjiDictionaryRadicalsLog getKanjiDictionaryRadicalsLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, radicals, found_elements "
					+ "from kanji_dictionary_radicals_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createKanjiDictionaryRadicalsLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private KanjiDictionaryRadicalsLog createKanjiDictionaryRadicalsLogFromResultSet(ResultSet resultSet) throws SQLException {

		KanjiDictionaryRadicalsLog kanjiDictionaryRadicalsLog = new KanjiDictionaryRadicalsLog();

		kanjiDictionaryRadicalsLog.setId(resultSet.getLong("id"));
		kanjiDictionaryRadicalsLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		kanjiDictionaryRadicalsLog.setRadicals(resultSet.getString("radicals"));
		kanjiDictionaryRadicalsLog.setFoundElements((Integer)resultSet.getObject("found_elements"));

		return kanjiDictionaryRadicalsLog;
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
	
	public KanjiDictionaryDetectLog getKanjiDictionaryDetectLogByGenericId(long genericId) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
				
		try {						
			connection = connectionPool.getConnection();
						
			preparedStatement = connection.prepareStatement("select id, generic_log_id, strokes, detect_kanji_result "
					+ "from kanji_dictionary_detect_log where generic_log_id = ?");
						
			preparedStatement.setLong(1, genericId);
			
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next() == true) {				
				return createKanjiDictionaryDetectLogFromResultSet(resultSet);
				
			} else {
				return null;
			}
						
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

	private KanjiDictionaryDetectLog createKanjiDictionaryDetectLogFromResultSet(ResultSet resultSet) throws SQLException {
		
		KanjiDictionaryDetectLog kanjiDictionaryDetectLog = new KanjiDictionaryDetectLog();
		
		kanjiDictionaryDetectLog.setId(resultSet.getLong("id"));
		kanjiDictionaryDetectLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		kanjiDictionaryDetectLog.setStrokes(resultSet.getString("strokes"));
		kanjiDictionaryDetectLog.setDetectKanjiResult(resultSet.getString("detect_kanji_result"));
		
		return kanjiDictionaryDetectLog;
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
	
	public KanjiDictionaryDetailsLog getKanjiDictionaryDetailsLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, kanji_entry_id, kanji_entry_kanji, kanji_entry_translateList, kanji_entry_info "
					+ "from kanji_dictionary_details_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createKanjiDictionaryDetailsLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private KanjiDictionaryDetailsLog createKanjiDictionaryDetailsLogFromResultSet(ResultSet resultSet) throws SQLException {

		KanjiDictionaryDetailsLog kanjiDictionaryDetailsLog = new KanjiDictionaryDetailsLog();

		kanjiDictionaryDetailsLog.setId(resultSet.getLong("id"));
		kanjiDictionaryDetailsLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		kanjiDictionaryDetailsLog.setKanjiEntryId((Integer)resultSet.getObject("kanji_entry_id"));
		kanjiDictionaryDetailsLog.setKanjiEntryKanji(resultSet.getString("kanji_entry_kanji"));
		kanjiDictionaryDetailsLog.setKanjiEntryTranslateList(resultSet.getString("kanji_entry_translateList"));
		kanjiDictionaryDetailsLog.setKanjiEntryInfo(resultSet.getString("kanji_entry_info"));

		return kanjiDictionaryDetailsLog;
	}
	
	public void insertKanjiDictionaryCatalogLog(KanjiDictionaryCatalogLog kanjiDictionaryCatalogLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into kanji_dictionary_catalog_log(generic_log_id, page_no) "
					+ "values(?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, kanjiDictionaryCatalogLog.getGenericLogId());
			preparedStatement.setInt(2, kanjiDictionaryCatalogLog.getPageNo());
						
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			kanjiDictionaryCatalogLog.setId(generatedKeys.getLong(1));
			
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
	
	public KanjiDictionaryCatalogLog getKanjiDictionaryCatalogLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, page_no "
					+ "from kanji_dictionary_catalog_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createKanjiDictionaryCatalogLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private KanjiDictionaryCatalogLog createKanjiDictionaryCatalogLogFromResultSet(ResultSet resultSet) throws SQLException {

		KanjiDictionaryCatalogLog kanjiDictionaryCatalogLog = new KanjiDictionaryCatalogLog();

		kanjiDictionaryCatalogLog.setId(resultSet.getLong("id"));
		kanjiDictionaryCatalogLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		kanjiDictionaryCatalogLog.setPageNo((Integer)resultSet.getObject("page_no"));

		return kanjiDictionaryCatalogLog;
	}
	
	public void insertAndroidSendMissingWordLog(AndroidSendMissingWordLog androidSendMissingWordLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into android_send_missing_word_log(generic_log_id, word, word_place_search) "
					+ "values(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, androidSendMissingWordLog.getGenericLogId());
			preparedStatement.setString(2, androidSendMissingWordLog.getWord());
			preparedStatement.setString(3, androidSendMissingWordLog.getWordPlaceSearch());
						
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			androidSendMissingWordLog.setId(generatedKeys.getLong(1));
			
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
	
	public AndroidSendMissingWordLog getAndroidSendMissingWordLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, word, word_place_search "
					+ "from android_send_missing_word_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createAndroidSendMissingWordLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private AndroidSendMissingWordLog createAndroidSendMissingWordLogFromResultSet(ResultSet resultSet) throws SQLException {

		AndroidSendMissingWordLog androidSendMissingWordLog = new AndroidSendMissingWordLog();

		androidSendMissingWordLog.setId(resultSet.getLong("id"));
		androidSendMissingWordLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		androidSendMissingWordLog.setWord(resultSet.getString("word"));
		androidSendMissingWordLog.setWordPlaceSearch(resultSet.getString("word_place_search"));

		return androidSendMissingWordLog;
	}
	
	public void insertAndroidGetSpellCheckerSuggestionLog(AndroidGetSpellCheckerSuggestionLog androidGetSpellCheckerSuggestionLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into android_get_spell_checker_suggestion_log(generic_log_id, word, type, spell_checker_suggestion_list) "
					+ "values(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, androidGetSpellCheckerSuggestionLog.getGenericLogId());
			preparedStatement.setString(2, androidGetSpellCheckerSuggestionLog.getWord());
			preparedStatement.setString(3, androidGetSpellCheckerSuggestionLog.getType());
			preparedStatement.setString(4, androidGetSpellCheckerSuggestionLog.getSpellCheckerSuggestionList());
						
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			androidGetSpellCheckerSuggestionLog.setId(generatedKeys.getLong(1));
			
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
	
	public AndroidGetSpellCheckerSuggestionLog getAndroidGetSpellCheckerSuggestionLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, word, type, spell_checker_suggestion_list "
					+ "from android_get_spell_checker_suggestion_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createAndroidGetSpellCheckerSuggestionLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private AndroidGetSpellCheckerSuggestionLog createAndroidGetSpellCheckerSuggestionLogFromResultSet(ResultSet resultSet) throws SQLException {

		AndroidGetSpellCheckerSuggestionLog androidGetSpellCheckerSuggestionLog = new AndroidGetSpellCheckerSuggestionLog();

		androidGetSpellCheckerSuggestionLog.setId(resultSet.getLong("id"));
		androidGetSpellCheckerSuggestionLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		androidGetSpellCheckerSuggestionLog.setWord(resultSet.getString("word"));
		androidGetSpellCheckerSuggestionLog.setType(resultSet.getString("type"));
		androidGetSpellCheckerSuggestionLog.setSpellCheckerSuggestionList(resultSet.getString("spell_checker_suggestion_list"));

		return androidGetSpellCheckerSuggestionLog;
	}
	
	public void insertAndroidQueueEventLoggerModel(AndroidQueueEventLog androidQueueEventLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into android_queue_event_log(generic_log_id, user_id, operation, create_date, params) "
					+ "values(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, androidQueueEventLog.getGenericLogId());
			preparedStatement.setString(2, androidQueueEventLog.getUserId());
			preparedStatement.setString(3, androidQueueEventLog.getOperation().toString());
			preparedStatement.setTimestamp(4, androidQueueEventLog.getCreateDate());
			
			if (androidQueueEventLog.getParams() != null) {
				preparedStatement.setString(5, androidQueueEventLog.getParams());
				
			} else {
				preparedStatement.setNull(5, Types.VARCHAR);
			}
									
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			androidQueueEventLog.setId(generatedKeys.getLong(1));
			
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
	
	public SuggestionSendLog getSuggestionSendLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, title, sender, body "
					+ "from suggestion_send_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createSuggestionSendLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private SuggestionSendLog createSuggestionSendLogFromResultSet(ResultSet resultSet) throws SQLException {

		SuggestionSendLog suggestionSendLog = new SuggestionSendLog();

		suggestionSendLog.setId(resultSet.getLong("id"));
		suggestionSendLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		suggestionSendLog.setTitle(resultSet.getString("title"));
		suggestionSendLog.setSender(resultSet.getString("sender"));
		suggestionSendLog.setBody(resultSet.getString("body"));

		return suggestionSendLog;
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
	
	public DailyReportSendLog getDailyReportSendLogByGenericId(Long genericId) throws SQLException {

		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, title, report "
					+ "from daily_report_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createDailyReportSendLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private DailyReportSendLog createDailyReportSendLogFromResultSet(ResultSet resultSet) throws SQLException {

		DailyReportSendLog dailyReportSendLog = new DailyReportSendLog();

		dailyReportSendLog.setId(resultSet.getLong("id"));
		dailyReportSendLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		dailyReportSendLog.setTitle(resultSet.getString("title"));
		dailyReportSendLog.setReport(resultSet.getString("report"));

		return dailyReportSendLog;
	}
	
	public void insertGeneralExceptionLog(GeneralExceptionLog generalExceptionLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into general_exception_log(generic_log_id, status_code, exception) "
					+ "values(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, generalExceptionLog.getGenericLogId());
			preparedStatement.setInt(2, generalExceptionLog.getStatusCode());
			preparedStatement.setString(3, generalExceptionLog.getException());
			
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			generalExceptionLog.setId(generatedKeys.getLong(1));
			
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
	
	public GeneralExceptionLog getGeneralExceptionLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, status_code, exception "
					+ "from general_exception_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createGeneralExceptionLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private GeneralExceptionLog createGeneralExceptionLogFromResultSet(ResultSet resultSet) throws SQLException {

		GeneralExceptionLog generalExceptionLog = new GeneralExceptionLog();

		generalExceptionLog.setId(resultSet.getLong("id"));
		generalExceptionLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		generalExceptionLog.setStatusCode((Integer)resultSet.getObject("status_code"));
		generalExceptionLog.setException(resultSet.getString("exception"));

		return generalExceptionLog;
	}
	
	public DailyLogProcessedMinMaxIds getCurrentDailyLogProcessedMinMaxIds() throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement("select (select max(id) + 1 from daily_log_processed_ids) minId, (select timestamp from generic_log gl where id = (select max(id) + 1 from daily_log_processed_ids)) minTimestamp, "
					+ "(select max(id) from generic_log) maxId, (select timestamp from generic_log where id = (select max(id) from generic_log)) maxTimestamp;");
			
			resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next() == false) {
				return null;
			}
			
			Long minId = resultSet.getLong(1);
			Date minDate = resultSet.getTimestamp(2);
			
			Long maxId = resultSet.getLong(3);
			Date maxDate = resultSet.getTimestamp(4);
						
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

	public List<GenericTextStat> getWordDictionarySearchNoFoundStat(long startId, long endId) throws SQLException {
	
		return getGenericTextStat("select find_word_request_word, count(*) from word_dictionary_search_log "
				+ "where find_word_result_result_size = 0 and generic_log_id >= ? and generic_log_id <= ? group by find_word_request_word order by 2 desc, 1", startId, endId);		
	}
	
	public List<GenericTextStat> getWordDictionarySearchStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select find_word_request_word, count(*) from word_dictionary_search_log "
				+ "where generic_log_id >= ? and generic_log_id <= ? group by find_word_request_word order by 2 desc, 1", startId, endId);		
	}
	
	public List<GenericTextStat> getWordDictionaryAutocompleteNoFoundStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select term, count(*) from word_dictionary_autocomplete_log where found_elements = 0 "
				+ "and generic_log_id >= ? and generic_log_id <= ? group by term order by 2 desc, 1 desc", startId, endId);
	}
	
	public List<GenericTextStat> getWordDictionaryAutocompleteStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select term, count(*) from word_dictionary_autocomplete_log where "
				+ "generic_log_id >= ? and generic_log_id <= ? group by term order by 2 desc, 1 desc", startId, endId);
	}

	public List<GenericTextStat> getKanjiDictionarySearchNoFoundStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select find_kanji_request_word, count(*) from kanji_dictionary_search_log "
				+ "where find_kanji_result_result_size = 0 and generic_log_id >= ? and generic_log_id <= ? group by find_kanji_request_word order by 2 desc, 1", startId, endId);		
	}
	
	public List<GenericTextStat> getKanjiDictionarySearchStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select find_kanji_request_word, count(*) from kanji_dictionary_search_log "
				+ "where generic_log_id >= ? and generic_log_id <= ? group by find_kanji_request_word order by 2 desc, 1", startId, endId);		
	}
	
	public List<GenericTextStat> getKanjiDictionaryAutocompleteNoFoundStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select term, count(*) from kanji_dictionary_autocomplete_log where found_elements = 0 "
				+ "and generic_log_id >= ? and generic_log_id <= ? group by term order by 2 desc, 1 desc", startId, endId);
	}
	
	public List<GenericTextStat> getKanjiDictionaryAutocompleteStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select term, count(*) from kanji_dictionary_autocomplete_log where "
				+ "generic_log_id >= ? and generic_log_id <= ? group by term order by 2 desc, 1 desc", startId, endId);
	}

	public List<GenericTextStat> getRefererStat(long startId, long endId, String baseServer) throws SQLException {
		
		return getGenericTextStat("select substring(referer_url, 1, 150), count(*) from generic_log where "
				+ "id >= ? and id <= ? "
				+ "and referer_url not like '%" + baseServer + "%' "
				+ "group by substring(referer_url, 1, 150) order by 2 desc, 1 desc", startId, endId);
	}

	public List<GenericTextStat> getPageNotFoundStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select request_url, count(*) from generic_log where operation = 'PAGE_NO_FOUND_EXCEPTION' and "
				+ "id >= ? and id <= ? group by request_url order by 2 desc, 1 desc", startId, endId);
	}
	
	public List<GenericTextStat> getUserAgentClientStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select user_agent, count(*) from generic_log where "
				+ "id >= ? and id <= ? group by user_agent order by 2 desc, 1 desc", startId, endId);
	}
	
	public List<GenericTextStat> getAndroidSendMissingWordStat(long startId, long endId) throws SQLException {
		
		return getGenericTextStat("select word, count(*) from android_send_missing_word_log where "
				+ "generic_log_id >= ? and generic_log_id <= ? group by word order by 2 desc, 1 desc", startId, endId);
	}

	public List<RemoteClientStat> getRemoteClientStat(long startId, long endId) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		try {
			List<RemoteClientStat> result = new ArrayList<RemoteClientStat>();
			
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement("select remote_ip, remote_host, count(*) from generic_log where id >= ? and id <= ? group by remote_ip, remote_host order by 3 desc;");
			
			preparedStatement.setLong(1, startId);
			preparedStatement.setLong(2, endId);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {
				
				RemoteClientStat remoteClientStat = new RemoteClientStat();
				
				remoteClientStat.setRemoteIp(resultSet.getString(1));
				remoteClientStat.setRemoteHost(resultSet.getString(2));
				remoteClientStat.setStat(resultSet.getLong(3));
				
				result.add(remoteClientStat);
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

	private List<GenericTextStat> getGenericTextStat(String sql, long startId, long endId) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		try {
			List<GenericTextStat> result = new ArrayList<GenericTextStat>();
			
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement(sql);
			
			preparedStatement.setLong(1, startId);
			preparedStatement.setLong(2, endId);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {
				
				GenericTextStat genericTextStat = new GenericTextStat();
				
				genericTextStat.setText(resultSet.getString(1));
				genericTextStat.setStat(resultSet.getLong(2));
				
				result.add(genericTextStat);
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
	
	public void insertAdminRequestLog(AdminRequestLog adminRequestLog) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into admin_request_log(generic_log_id, type, result, params) "
					+ "values(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setLong(1, adminRequestLog.getGenericLogId());
			preparedStatement.setString(2, adminRequestLog.getType());
			preparedStatement.setString(3, adminRequestLog.getResult());
			preparedStatement.setString(4, adminRequestLog.getParams());
						
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			adminRequestLog.setId(generatedKeys.getLong(1));
			
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
	
	public AdminRequestLog getAdminRequestLogByGenericId(Long genericId) throws SQLException {
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, generic_log_id, type, result, params "
					+ "from admin_request_log where generic_log_id = ?");

			preparedStatement.setLong(1, genericId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createAdminRequestLogFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private AdminRequestLog createAdminRequestLogFromResultSet(ResultSet resultSet) throws SQLException {

		AdminRequestLog adminRequestLog = new AdminRequestLog();

		adminRequestLog.setId(resultSet.getLong("id"));
		adminRequestLog.setGenericLogId(resultSet.getLong("generic_log_id"));
		adminRequestLog.setType(resultSet.getString("type"));
		adminRequestLog.setResult(resultSet.getString("result"));
		adminRequestLog.setParams(resultSet.getString("params"));

		return adminRequestLog;
	}
	
	public void insertQueueItem(QueueItem queueItem) throws SQLException {
				
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		Blob objectBlob = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into queue(name, status, host_name, send_timestamp, delivery_count, next_attempt, object) "
					+ "values(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setString(1, queueItem.getName());
			preparedStatement.setString(2, queueItem.getStatus().toString());
			preparedStatement.setString(3, queueItem.getHostName());
			preparedStatement.setTimestamp(4, queueItem.getSendTimestamp());
			preparedStatement.setInt(5, queueItem.getDeliveryCount());
			preparedStatement.setTimestamp(6, queueItem.getNextAttempt());
			
			objectBlob = connection.createBlob();			
			objectBlob.setBytes(1, queueItem.getObject());
			
			preparedStatement.setBlob(7, objectBlob);
			
			// wstaw
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			queueItem.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (objectBlob != null) {
				objectBlob.free();
			}
			
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
	
	public List<QueueItem> getNextQueueItem(String queueName, String hostName) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		List<QueueItem> result = new ArrayList<QueueItem>();
		
		try {			
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement("select id, name, status, host_name, send_timestamp, delivery_count, next_attempt, object from queue where "
					+ "name = ? and status = ? and next_attempt < current_timestamp and host_name = ? order by next_attempt, send_timestamp, delivery_count desc limit 5"); 
			
			preparedStatement.setString(1, queueName);
			preparedStatement.setString(2, QueueItemStatus.WAITING.toString());
			preparedStatement.setString(3, hostName);
			
			resultSet = preparedStatement.executeQuery();
			
			QueueItem queueItem = null;
			
			while (resultSet.next() == true) {
				
				queueItem = new QueueItem();
				
				queueItem.setId(resultSet.getLong("id"));
				queueItem.setStatus(QueueItemStatus.valueOf(resultSet.getString("status")));
				queueItem.setHostName(resultSet.getString("host_name"));
				queueItem.setSendTimestamp(resultSet.getTimestamp("send_timestamp"));
				queueItem.setDeliveryCount(resultSet.getInt("delivery_count"));
				queueItem.setNextAttempt(resultSet.getTimestamp("next_attempt"));
				
				Blob objectBlob = resultSet.getBlob("object");
				
				byte[] object = objectBlob.getBytes(1, (int)objectBlob.length());
				
				objectBlob.free();
				
				queueItem.setObject(object);
				
				result.add(queueItem);
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
	
	public void updateQueueItem(QueueItem queueItem) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
				
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "update queue set status = ?, delivery_count = ?, next_attempt = ?, send_timestamp = send_timestamp where id = ?");
			
			preparedStatement.setString(1, queueItem.getStatus().toString());
			preparedStatement.setInt(2, queueItem.getDeliveryCount());
			preparedStatement.setTimestamp(3, queueItem.getNextAttempt());
			preparedStatement.setLong(4, queueItem.getId());
						
			// uaktualnij
			preparedStatement.executeUpdate();
			
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
	
	public void deleteOldQueueItems(int ndays) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
				
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement("delete from queue where status = ? and datediff(curdate(), next_attempt) > ?");
			
			preparedStatement.setString(1, QueueItemStatus.DONE.toString());
			preparedStatement.setInt(2, ndays);
						
			// skasuj
			preparedStatement.executeUpdate();
			
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
	
	public WordDictionarySearchMissingWordQueue getWordDictionarySearchMissingWordsQueue(String word) throws SQLException {
		
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, missing_word, counter, first_appearance_timestamp, last_appearance_timestamp, lock_timestamp, priority "
					+ "from word_dictionary_search_missing_words_queue where missing_word = ?");

			preparedStatement.setString(1, word);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createWordDictionarySearchMissingWordQueueFromResultSet(resultSet);

			} else {
				return null;
			}

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

	private WordDictionarySearchMissingWordQueue createWordDictionarySearchMissingWordQueueFromResultSet(ResultSet resultSet) throws SQLException {

		WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue = new WordDictionarySearchMissingWordQueue();

		wordDictionarySearchMissingWordQueue.setId(resultSet.getLong("id"));
		wordDictionarySearchMissingWordQueue.setMissingWord(resultSet.getString("missing_word"));
		wordDictionarySearchMissingWordQueue.setCounter(resultSet.getInt("counter"));
		wordDictionarySearchMissingWordQueue.setFirstAppearanceTimestamp(resultSet.getTimestamp("first_appearance_timestamp"));
		wordDictionarySearchMissingWordQueue.setLastAppearanceTimestamp(resultSet.getTimestamp("last_appearance_timestamp"));
		wordDictionarySearchMissingWordQueue.setLockTimestamp(resultSet.getTimestamp("lock_timestamp"));
		wordDictionarySearchMissingWordQueue.setPriority(resultSet.getInt("priority"));
		
		return wordDictionarySearchMissingWordQueue;
	}
	
	public void insertWordDictionarySearchMissingWordsQueue(WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		Blob objectBlob = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_search_missing_words_queue(missing_word, counter, first_appearance_timestamp, last_appearance_timestamp, lock_timestamp, priority) "
					+ "values(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setString(1, wordDictionarySearchMissingWordQueue.getMissingWord());
			preparedStatement.setInt(2, wordDictionarySearchMissingWordQueue.getCounter());
			preparedStatement.setTimestamp(3, wordDictionarySearchMissingWordQueue.getFirstAppearanceTimestamp());
			preparedStatement.setTimestamp(4, wordDictionarySearchMissingWordQueue.getLastAppearanceTimestamp());
			preparedStatement.setTimestamp(5, wordDictionarySearchMissingWordQueue.getLockTimestamp());
			preparedStatement.setInt(6, wordDictionarySearchMissingWordQueue.getPriority());
						
			// wstaw
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			wordDictionarySearchMissingWordQueue.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (objectBlob != null) {
				objectBlob.free();
			}
			
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
	
	public void updateWordDictionarySearchMissingWordQueue(WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordsQueue) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
				
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "update word_dictionary_search_missing_words_queue set counter = ?, first_appearance_timestamp = ?, "
					+ "last_appearance_timestamp = ?, lock_timestamp = ?, priority = ? where id = ?");
			
			preparedStatement.setInt(1, wordDictionarySearchMissingWordsQueue.getCounter());
			preparedStatement.setTimestamp(2, wordDictionarySearchMissingWordsQueue.getFirstAppearanceTimestamp());
			preparedStatement.setTimestamp(3, wordDictionarySearchMissingWordsQueue.getLastAppearanceTimestamp());
			preparedStatement.setTimestamp(4, wordDictionarySearchMissingWordsQueue.getLockTimestamp());
			preparedStatement.setLong(5, wordDictionarySearchMissingWordsQueue.getPriority());
			preparedStatement.setLong(6, wordDictionarySearchMissingWordsQueue.getId());
						
			// uaktualnij
			preparedStatement.executeUpdate();
			
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
	
	public List<WordDictionarySearchMissingWordQueue> getUnlockedWordDictionarySearchMissingWordQueue(long size) throws SQLException {
		
		// UWAGA: Jesli zmieniasz te metode, zmien rowniez metode getUnlockedWordDictionarySearchMissingWordQueueLength
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		List<WordDictionarySearchMissingWordQueue> result = new ArrayList<WordDictionarySearchMissingWordQueue>();
				
		try {						
			connection = connectionPool.getConnection();
									
			preparedStatement = connection.prepareStatement("select id, missing_word, counter, first_appearance_timestamp, last_appearance_timestamp, lock_timestamp, priority "
					+ "from word_dictionary_search_missing_words_queue where lock_timestamp is null order by priority desc, "
					+ "first_appearance_timestamp, id limit ?");
//					+ "date_format(first_appearance_timestamp, '%Y-%m-%d') != date_format(last_appearance_timestamp, '%Y-%m-%d') desc, first_appearance_timestamp, id limit ?");
						
			preparedStatement.setLong(1, size);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {
				
				WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue = createWordDictionarySearchMissingWordQueueFromResultSet(resultSet);
								
				result.add(wordDictionarySearchMissingWordQueue);
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

	public long getUnlockedWordDictionarySearchMissingWordQueueLength() throws SQLException {
		
		// UWAGA: Jesli zmieniasz te metode, zmien rowniez metode getUnlockedWordDictionarySearchMissingWordQueue
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
						
		try {						
			connection = connectionPool.getConnection();
									
			preparedStatement = connection.prepareStatement("select count(*) "
					+ "from word_dictionary_search_missing_words_queue where lock_timestamp is null order by priority desc, "
					+ "first_appearance_timestamp, id");
//					+ "date_format(first_appearance_timestamp, '%Y-%m-%d') != date_format(last_appearance_timestamp, '%Y-%m-%d') desc, first_appearance_timestamp, id");
									
			resultSet = preparedStatement.executeQuery();
			
			resultSet.next();
			
			return resultSet.getLong(1);
						
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

	
	public List<WordDictionarySearchMissingWordQueue> getUnlockedWordDictionarySearchMissingWordQueue(List<String> wordList) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		List<WordDictionarySearchMissingWordQueue> result = new ArrayList<WordDictionarySearchMissingWordQueue>();
				
		try {						
			connection = connectionPool.getConnection();
			
			StringBuffer sql = new StringBuffer("select id, missing_word, counter, first_appearance_timestamp, last_appearance_timestamp, lock_timestamp, priority "
					+ "from word_dictionary_search_missing_words_queue where lock_timestamp is null and missing_word in ");
			
			sql.append("(");
			
			for (int wordListIdx = 0; wordListIdx < wordList.size(); ++wordListIdx) {
				
				sql.append("?");
				
				if (wordListIdx != wordList.size() - 1) {
					sql.append(",");
				}				
			}
			
			sql.append(")");			
			
			preparedStatement = connection.prepareStatement(sql.toString());
			
			for (int wordListIdx = 0; wordListIdx < wordList.size(); ++wordListIdx) {
				preparedStatement.setString(wordListIdx + 1, wordList.get(wordListIdx));
			}
						
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {
				
				WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue = createWordDictionarySearchMissingWordQueueFromResultSet(resultSet);
								
				result.add(wordDictionarySearchMissingWordQueue);
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
	
	public List<WordDictionarySearchMissingWordQueue> getAllWordDictionarySearchMissingWordQueue() throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		List<WordDictionarySearchMissingWordQueue> result = new ArrayList<WordDictionarySearchMissingWordQueue>();
				
		try {						
			connection = connectionPool.getConnection();
									
			preparedStatement = connection.prepareStatement("select id, missing_word, counter, first_appearance_timestamp, last_appearance_timestamp, lock_timestamp, priority "
					+ "from word_dictionary_search_missing_words_queue order by id");
						
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {
				
				WordDictionarySearchMissingWordQueue wordDictionarySearchMissingWordQueue = createWordDictionarySearchMissingWordQueueFromResultSet(resultSet);
								
				result.add(wordDictionarySearchMissingWordQueue);
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
	
	public boolean canDoOperation(String semaphoreName, int lockLengthInSeconds) throws SQLException {
		
		Connection connection = null;
		
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		try {
			connection = connectionPool.getConnection();
			
			// zakladanie blokady			
			statement = connection.createStatement();
			
			statement.execute("lock table lock_operation write");
						
			// sprawdzenie, czy rekord istnieje w tabelce lock_operation
			preparedStatement = connection.prepareStatement("select count(*) from lock_operation where lock_name = ?");
			
			preparedStatement.setString(1, semaphoreName);
			
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
						
			int lockOperationNameRowCount = resultSet.getInt(1);
			
			preparedStatement.close();
			resultSet.close();
			
			if (lockOperationNameRowCount == 0) { // jesli nie ma rekordu, wstaw rekord i zwroc wynik, ze uzystano dostep
				
				preparedStatement = connection.prepareStatement("insert into lock_operation(lock_name, lock_timestamp) values (?, current_timestamp)");
				
				preparedStatement.setString(1, semaphoreName);
				
				preparedStatement.executeUpdate();
				
				preparedStatement.close();
				
				return true;
				
			} else { // jesli rekord istnieje, sprawdz, czy minal czas od ostatniej blokady
				
				preparedStatement = connection.prepareStatement("select count(*) from lock_operation where lock_name = ? and "
						+ "date_add(lock_timestamp, interval ? second) <= current_timestamp");
				
				preparedStatement.setString(1, semaphoreName);
				preparedStatement.setInt(2, lockLengthInSeconds);
				
				resultSet = preparedStatement.executeQuery();
				resultSet.next();

				lockOperationNameRowCount = resultSet.getInt(1);
				
				preparedStatement.close();
				resultSet.close();

				if (lockOperationNameRowCount == 0) { // ktos juz byl szybszy
					return false;
					
				} else { // my jestesmy jako pierwsi, uaktualniamy blokade
					
					preparedStatement = connection.prepareStatement("update lock_operation set lock_timestamp = current_timestamp where lock_name = ?");
					
					preparedStatement.setString(1, semaphoreName);
					
					preparedStatement.executeUpdate();
					
					preparedStatement.close();
					
					return true;
				}
			}
			
		} finally {
			
			if (statement != null) {
				statement.execute("unlock tables");
			}			
			
			if (resultSet != null) {
				resultSet.close();
			}
			
			if (statement != null) {
				statement.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			
			if (connection != null) {
				connection.close();
			}			
		}	
	}
	
	public Transaction beginTransaction() throws SQLException {
		
		Connection connection = null;
		
		try {
			connection = connectionPool.getConnection();
			
			if (connection == null) {
				throw new SQLException("Brak dostępnych połączeń");
			}
			
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			
			Transaction transaction = new Transaction(connection);
			
			return transaction;
			
		} catch (SQLException e) {
			
			if (connection != null) {
				connection.close();
			}			

			throw e;
		}
	}
	
	public void commitTransaction(Transaction transaction) throws SQLException {
		
		try {
			transaction.connection.commit();
		
		} finally {
			
			if (transaction.connection != null) {
				transaction.connection.close();
			}	
		}
	}

	public void rollbackTransaction(Transaction transaction) throws SQLException {
		
		try {
			transaction.connection.rollback();
		
		} finally {
			
			if (transaction.connection != null) {
				transaction.connection.close();
			}	
		}
	}
	
	public void deleteOldDailyLogProcessedIds(Transaction transaction) throws SQLException {
				
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
				
		try {			
			preparedStatement = transaction.connection.prepareStatement("delete from daily_log_processed_ids where id < ((select * from (select max(id) - 1 from daily_log_processed_ids) as p))");
									
			// skasuj
			preparedStatement.executeUpdate();
			
		} finally {
						
			if (generatedKeys != null) {
				generatedKeys.close();
			}
			
			if (preparedStatement != null) {
				preparedStatement.close();
			}			
		}
	}
	
	public List<String> getOldGenericLogOperationDateList(Transaction transaction, GenericLogOperationEnum operation, int dayOlderThan) throws SQLException {
				
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;
		
		List<String> result = new ArrayList<String>();
		
		//
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);

		calendar.add(Calendar.DAY_OF_YEAR, -dayOlderThan);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String olderThanTimestampString = sdf.format(calendar.getTime());
		
		//
		
		try {						
			preparedStatement = transaction.connection.prepareStatement("select distinct date_format(timestamp, '%Y-%m-%d') from generic_log where operation = ? and timestamp < ?");
			
			preparedStatement.setString(1, operation.toString());
			preparedStatement.setString(2, olderThanTimestampString);
			
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				
				result.add(resultSet.getString(1));				
			}
			
			return result;
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}		
	}
	
	public void processGenericLogRecords(Transaction transaction, GenericLogOperationEnum operation, String dateString, ProcessRecordCallback<GenericLog> processRecordCallback) throws SQLException {
				
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, timestamp, session_id, user_agent, request_url, referer_url, remote_ip, remote_host, operation "
					+ "from generic_log where operation = ? and timestamp >= ? and timestamp <= ?");
			
			preparedStatement.setString(1, operation.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				GenericLog genericLog = createGenericLogFromResultSet(resultSet);
				
				processRecordCallback.callback(genericLog);				
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteGenericLogRecords(Transaction transaction, GenericLogOperationEnum operation, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from generic_log where operation = ? and timestamp >= ? and timestamp <= ?");
			
			preparedStatement.setString(1, operation.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void processWordDictionaryAutocompleteLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<WordDictionaryAutocompleteLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, term, found_elements "
					+ "from word_dictionary_autocomplete_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_AUTOCOMPLETE.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				WordDictionaryAutocompleteLog wordDictionaryAutocompleteLog = createWordDictionaryAutocompleteLogFromResultSet(resultSet);
				
				processRecordCallback.callback(wordDictionaryAutocompleteLog);				
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteWordDictionaryAutocompleteLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from word_dictionary_autocomplete_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_AUTOCOMPLETE.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processWordDictionaryCatalogLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<WordDictionaryCatalogLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, page_no "
					+ "from word_dictionary_catalog_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_CATALOG.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				WordDictionaryCatalogLog wordDictionaryCatalogLog = createWordDictionaryCatalogLogFromResultSet(resultSet);
				
				processRecordCallback.callback(wordDictionaryCatalogLog);				
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteWordDictionaryCatalogLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from word_dictionary_catalog_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_CATALOG.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void processWordDictionaryDetailsLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<WordDictionaryDetailsLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, dictionary_entry_id, "
					+ "dictionary_entry_kanji, dictionary_entry_kanaList, dictionary_entry_romajiList, "
					+ "dictionary_entry_translateList, dictionary_entry_info "
					+ "from word_dictionary_details_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_DETAILS.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				WordDictionaryDetailsLog wordDictionaryDetailsLog = createWordDictionaryDetailsLogFromResultSet(resultSet);
				
				processRecordCallback.callback(wordDictionaryDetailsLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteWordDictionaryDetailsLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from word_dictionary_details_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_DETAILS.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void processWordDictionaryNameCatalogLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<WordDictionaryNameCatalogLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, page_no "
					+ "from word_dictionary_name_catalog_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_NAME_CATALOG.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				WordDictionaryNameCatalogLog wordDictionaryNameCatalogLog = createWordDictionaryNameCatalogLogFromResultSet(resultSet);
				
				processRecordCallback.callback(wordDictionaryNameCatalogLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteWordDictionaryNameCatalogLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from word_dictionary_name_catalog_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_NAME_CATALOG.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processWordDictionaryNameDetailsLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<WordDictionaryNameDetailsLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, dictionary_entry_id, "
					+ "dictionary_entry_kanji, dictionary_entry_kanaList, dictionary_entry_romajiList, "
					+ "dictionary_entry_translateList, dictionary_entry_info "
					+ "from word_dictionary_name_details_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_NAME_DETAILS.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				WordDictionaryNameDetailsLog wordDictionaryNameDetailsLog = createWordDictionaryNameDetailsLogFromResultSet(resultSet);
				
				processRecordCallback.callback(wordDictionaryNameDetailsLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteWordDictionaryNameDetailsLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from word_dictionary_name_details_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_NAME_DETAILS.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processWordDictionarySearchLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<WordDictionarySearchLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, find_word_request_word, find_word_request_search_kanji, "
					+ "find_word_request_search_kana, find_word_request_search_romaji, find_word_request_search_translate, find_word_request_search_info, find_word_request_search_only_common_words, "
					+ "find_word_request_word_place, find_word_request_dictionary_entry_type_list, find_word_result_result_size, priority "
					+ "from word_dictionary_search_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_SEARCH.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				WordDictionarySearchLog wordDictionarySearchLog = createWordDictionarySearchLogFromResultSet(resultSet);
				
				processRecordCallback.callback(wordDictionarySearchLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteWordDictionarySearchLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from word_dictionary_search_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.WORD_DICTIONARY_SEARCH.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processKanjiDictionaryAutocompleteLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<KanjiDictionaryAutocompleteLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, term, found_elements "
					+ "from kanji_dictionary_autocomplete_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_AUTOCOMPLETE.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				KanjiDictionaryAutocompleteLog kanjiDictionaryAutocompleteLog = createKanjiDictionaryAutocompleteLogFromResultSet(resultSet);
				
				processRecordCallback.callback(kanjiDictionaryAutocompleteLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteKanjiDictionaryAutocompleteLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from kanji_dictionary_autocomplete_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_AUTOCOMPLETE.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processKanjiDictionaryCatalogLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<KanjiDictionaryCatalogLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, page_no "
					+ "from kanji_dictionary_catalog_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_CATALOG.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				KanjiDictionaryCatalogLog kanjiDictionaryCatalogLog = createKanjiDictionaryCatalogLogFromResultSet(resultSet);
				
				processRecordCallback.callback(kanjiDictionaryCatalogLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteKanjiDictionaryCatalogLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from kanji_dictionary_catalog_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_CATALOG.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processKanjiDictionaryDetailsLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<KanjiDictionaryDetailsLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, kanji_entry_id, kanji_entry_kanji, kanji_entry_translateList, kanji_entry_info "
					+ "from kanji_dictionary_details_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_DETAILS.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				KanjiDictionaryDetailsLog kanjiDictionaryDetailsLog = createKanjiDictionaryDetailsLogFromResultSet(resultSet);
				
				processRecordCallback.callback(kanjiDictionaryDetailsLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteKanjiDictionaryDetailsLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from kanji_dictionary_details_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_DETAILS.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processKanjiDictionaryDetectLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<KanjiDictionaryDetectLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, strokes, detect_kanji_result "
					+ "from kanji_dictionary_detect_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_DETECT.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				KanjiDictionaryDetectLog kanjiDictionaryDetectLog = createKanjiDictionaryDetectLogFromResultSet(resultSet);
				
				processRecordCallback.callback(kanjiDictionaryDetectLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteKanjiDictionaryDetectLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from kanji_dictionary_detect_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_DETECT.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processKanjiDictionaryRadicalsLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<KanjiDictionaryRadicalsLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, radicals, found_elements "
					+ "from kanji_dictionary_radicals_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_RADICALS.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				KanjiDictionaryRadicalsLog kanjiDictionaryRadicalsLog = createKanjiDictionaryRadicalsLogFromResultSet(resultSet);
				
				processRecordCallback.callback(kanjiDictionaryRadicalsLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteKanjiDictionaryRadicalsLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from kanji_dictionary_radicals_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_RADICALS.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processKanjiDictionarySearchLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<KanjiDictionarySearchLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, find_kanji_request_word, find_kanji_request_word_place, "
					+ "find_kanji_request_stroke_count_from, find_kanji_request_stroke_count_to, find_kanji_result_result_size "
					+ "from kanji_dictionary_search_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_SEARCH.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				KanjiDictionarySearchLog kanjiDictionarySearchLog = createKanjiDictionarySearchLogFromResultSet(resultSet);
				
				processRecordCallback.callback(kanjiDictionarySearchLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteKanjiDictionarySearchLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from kanji_dictionary_search_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.KANJI_DICTIONARY_SEARCH.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processAndroidGetSpellCheckerSuggestionLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<AndroidGetSpellCheckerSuggestionLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, word, type, spell_checker_suggestion_list "
					+ "from android_get_spell_checker_suggestion_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.ANDROID_GET_SPELL_CHECKER_SUGGESTION.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				AndroidGetSpellCheckerSuggestionLog androidGetSpellCheckerSuggestionLog = createAndroidGetSpellCheckerSuggestionLogFromResultSet(resultSet);
				
				processRecordCallback.callback(androidGetSpellCheckerSuggestionLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteAndroidGetSpellCheckerSuggestionLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from android_get_spell_checker_suggestion_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.ANDROID_GET_SPELL_CHECKER_SUGGESTION.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void processAndroidSendMissingWordLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<AndroidSendMissingWordLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, word, word_place_search "
					+ "from android_send_missing_word_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.ANDROID_SEND_MISSING_WORD.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				AndroidSendMissingWordLog androidSendMissingWordLog = createAndroidSendMissingWordLogFromResultSet(resultSet);
				
				processRecordCallback.callback(androidSendMissingWordLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	public void deleteAndroidSendMissingWordLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from android_send_missing_word_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.ANDROID_SEND_MISSING_WORD.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	private String getWhereGenericLogIdGenericLogIdSql() {		
		return " where generic_log_id in (select id from generic_log where operation = ? and timestamp >= ? and timestamp <= ?)";
	}
	
	public WordDictionaryUniqueSearch getWordDictionaryUniqueSearch(String word) throws SQLException {
		
		Connection connection = null;

		PreparedStatement preparedStatement = null;

		ResultSet resultSet = null;

		try {						
			connection = connectionPool.getConnection();

			preparedStatement = connection.prepareStatement("select id, word, counter, first_appearance_timestamp, last_appearance_timestamp "
					+ "from word_dictionary_unique_search_log where word = ?");

			preparedStatement.setString(1, word);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next() == true) {				
				return createWordDictionaryUniqueSearchFromResultSet(resultSet);

			} else {
				return null;
			}

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
	
	private WordDictionaryUniqueSearch createWordDictionaryUniqueSearchFromResultSet(ResultSet resultSet) throws SQLException {

		WordDictionaryUniqueSearch wordDictionaryUniqueSearch = new WordDictionaryUniqueSearch();

		wordDictionaryUniqueSearch.setId(resultSet.getLong("id"));
		wordDictionaryUniqueSearch.setWord(resultSet.getString("word"));
		wordDictionaryUniqueSearch.setCounter(resultSet.getInt("counter"));
		wordDictionaryUniqueSearch.setFirstAppearanceTimestamp(resultSet.getTimestamp("first_appearance_timestamp"));
		wordDictionaryUniqueSearch.setLastAppearanceTimestamp(resultSet.getTimestamp("last_appearance_timestamp"));
		
		return wordDictionaryUniqueSearch;
	}
	
	public void insertWordDictionaryUniqueSearch(WordDictionaryUniqueSearch wordDictionaryUniqueSearch) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
		
		Blob objectBlob = null;
		
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "insert into word_dictionary_unique_search_log(word, counter, first_appearance_timestamp, last_appearance_timestamp) "
					+ "values(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setString(1, wordDictionaryUniqueSearch.getWord());
			preparedStatement.setInt(2, wordDictionaryUniqueSearch.getCounter());
			preparedStatement.setTimestamp(3, wordDictionaryUniqueSearch.getFirstAppearanceTimestamp());
			preparedStatement.setTimestamp(4, wordDictionaryUniqueSearch.getLastAppearanceTimestamp());
						
			// wstaw
			preparedStatement.executeUpdate();
			
			generatedKeys = preparedStatement.getGeneratedKeys();
			
			if (generatedKeys.next() == false) {
				throw new SQLException("Bład pobrania wygenerowanego klucza tabeli");
			}
			
			wordDictionaryUniqueSearch.setId(generatedKeys.getLong(1));
			
		} finally {
			
			if (objectBlob != null) {
				objectBlob.free();
			}
			
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
	
	public void updateWordDictionaryUniqueSearch(WordDictionaryUniqueSearch wordDictionaryUniqueSearch) throws SQLException {
		
		Connection connection = null;
		
		PreparedStatement preparedStatement = null;
		
		ResultSet generatedKeys = null;
				
		try {
			connection = connectionPool.getConnection();
			
			preparedStatement = connection.prepareStatement( "update word_dictionary_unique_search_log set counter = ?, first_appearance_timestamp = ?, "
					+ "last_appearance_timestamp = ? where id = ?");
			
			preparedStatement.setInt(1, wordDictionaryUniqueSearch.getCounter());
			preparedStatement.setTimestamp(2, wordDictionaryUniqueSearch.getFirstAppearanceTimestamp());
			preparedStatement.setTimestamp(3, wordDictionaryUniqueSearch.getLastAppearanceTimestamp());
			preparedStatement.setLong(4, wordDictionaryUniqueSearch.getId());
						
			// uaktualnij
			preparedStatement.executeUpdate();
			
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
	
	public void processDailyReportSendLogRecords(Transaction transaction, String dateString, ProcessRecordCallback<DailyReportSendLog> processRecordCallback) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		ResultSet resultSet = null;

		try {
			
			preparedStatement = transaction.connection.prepareStatement("select id, generic_log_id, title, report "
					+ "from daily_report_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.DAILY_REPORT.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next() == true) {				

				DailyReportSendLog dailyReportSendLog = createDailyReportSendLogFromResultSet(resultSet);
				
				processRecordCallback.callback(dailyReportSendLog);
			}
			
		} finally {
			
			if (resultSet != null) {
				resultSet.close();
			}
						
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
	
	
	public void deleteDailyReportSendLogRecords(Transaction transaction, String dateString) throws SQLException {
		
		PreparedStatement preparedStatement = null;
		
		try {
			
			preparedStatement = transaction.connection.prepareStatement("delete from daily_report_log " + getWhereGenericLogIdGenericLogIdSql());
			
			preparedStatement.setString(1, GenericLogOperationEnum.DAILY_REPORT.toString());
			preparedStatement.setString(2, dateString + " 00:00:00");
			preparedStatement.setString(3, dateString + " 23:59:59");

			preparedStatement.executeUpdate();
						
		} finally {
									
			if (preparedStatement != null) {
				preparedStatement.close();
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
	
	public static class Transaction {
		
		private Connection connection;

		private Transaction(Connection connection) {
			this.connection = connection;
		}
	}
	
	public static interface ProcessRecordCallback<T> {		
		public void callback(T genericLog);		
	}
}
