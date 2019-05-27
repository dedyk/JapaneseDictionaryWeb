package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import pl.idedyk.japanese.dictionary.api.android.queue.event.QueueEventOperation;
import pl.idedyk.japanese.dictionary.web.common.Utils;

public class AndroidQueueEventLog {

	private Long id;
	
	private Long genericLogId;
	
	private String userId;
	
	private QueueEventOperation operation;
	
	private Timestamp createDate;
	
	private String params;

	public Long getId() {
		return id;
	}

	public Long getGenericLogId() {
		return genericLogId;
	}

	public String getUserId() {
		return userId;
	}

	public QueueEventOperation getOperation() {
		return operation;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public String getParams() {
		return params;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setGenericLogId(Long genericLogId) {
		this.genericLogId = genericLogId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setOperation(QueueEventOperation operation) {
		this.operation = operation;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, String> getParamsAsMap() {
		
		Gson gson = new Gson();
		
		if (params == null) {
			return new HashMap<String, String>();
		}
		
		String paramsForJson = params;
		
		// sprawdzenie, czy to nie jest base64
		String paramsForJsonAsBase64 = Utils.tryAndGetBase64String(paramsForJson);
		
		if (paramsForJsonAsBase64 != null) {
			paramsForJson = paramsForJsonAsBase64;
		}		
		
		try {
			// proba sparsowania jako json
			return gson.fromJson(paramsForJson, Map.class);
			
		} catch (JsonSyntaxException e) {
			// byl wyjatek, probujemy sparsowac po staremu
		}

		Map<String,String> result = new HashMap<>();
		
		String realParamsValue = StringUtils.substringBetween(params, "{", "}");
		
		String[] keyValuePairs = realParamsValue.split(",");		

		for(String pair : keyValuePairs) {
			
		    String[] entry = pair.split("=");
		    
		    if (entry.length != 2) {
		    	continue;
		    }
		    
		    result.put(entry[0].trim(), entry[1].trim());
		}
		
		return result;
	}
}
