package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.sql.Timestamp;

import pl.idedyk.japanese.dictionary.api.android.queue.event.QueueEventOperation;

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
}
