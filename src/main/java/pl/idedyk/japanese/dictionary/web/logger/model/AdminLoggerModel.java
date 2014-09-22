package pl.idedyk.japanese.dictionary.web.logger.model;

import java.util.Map;
import java.util.TreeMap;

public class AdminLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
	
	private Type type;
	
	private Result result;
	
	private Map<String, String> params = new TreeMap<String, String>();
		
	public AdminLoggerModel(LoggerModelCommon loggerModelCommon) {		
		super(loggerModelCommon);
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	public void addParam(String key, String value) {
		if (params == null) {
			params = new TreeMap<String, String>();
		}
		
		params.put(key, value);
	}
	
	public static enum Type {
		
		ADMIN_LOGIN_START,
		
		ADMIN_LOGIN_FAILURE,
		
		ADMIN_LOGIN_SUCCESS,
		
		ADMIN_ACCESS_ERROR,
		
		ADMIN_LOGOUT_SUCCESS,
		
		ADMIN_PANEL,
		
		ADMIN_PANEL_SEARCH,
		
		GENERATE_DAILY_REPORT;
		
	}

	public static enum Result {
		
		OK,
		
		ERROR;		
	}	
}
