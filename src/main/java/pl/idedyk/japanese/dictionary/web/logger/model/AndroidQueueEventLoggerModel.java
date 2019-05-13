package pl.idedyk.japanese.dictionary.web.logger.model;

import java.util.Date;
import java.util.Map;

import pl.idedyk.japanese.dictionary.api.android.queue.event.QueueEventOperation;

public class AndroidQueueEventLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
		
	private String userId;
	
	private QueueEventOperation operation;
	
	private Date createDate;
	
	private Map<String, String> params;

	public AndroidQueueEventLoggerModel(LoggerModelCommon loggerModelCommon, String userId, QueueEventOperation operation, 
			Date createDate, Map<String, String> params) {
		
		super(loggerModelCommon);
		
		this.userId = userId;
		this.operation = operation;
		this.createDate = createDate;
		this.params = params;
	}
	
	public String getUserId() {
		return userId;
	}

	public QueueEventOperation getOperation() {
		return operation;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setOperation(QueueEventOperation operation) {
		this.operation = operation;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
