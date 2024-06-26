package pl.idedyk.japanese.dictionary.web.logger.model;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class AdminLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
	
	private Type type;
	
	private Result result;
	
	private Map<String, Object> params = new TreeMap<String, Object>();
		
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

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	public void addParam(String key, Object value) {
		
		if (params == null) {
			params = new TreeMap<String, Object>();
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
		
		ADMIN_SHOW_GENERIC_LOG,
		
		ADMIN_SHOW_MISSING_WORDS_QUEUE_PANEL,
		
		ADMIN_GET_MISSING_WORDS_QUEUE,
		
		GENERATE_DAILY_REPORT,
		
		SHOW_CURRENT_DAILY_REPORT,
		
		RELOAD_DATABASE;		
	}

	public static enum Result {
		
		OK,
		
		ERROR;		
	}
	
	public static class ObjectWrapper implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private String name;
		
		private Object object;
		
		public ObjectWrapper(String name, Object object) {
			this.name = name;
			this.object = object;
		}

		public String getName() {
			return name;
		}

		public Object getObject() {
			return object;
		}

		@Override
		public String toString() {
			return name;
		}		
	}
}
