package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class QueueItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	
	private String name;
	
	private QueueItemStatus status;
	
	private String hostName;
	
	private Timestamp sendTimestamp;
	
	private int deliveryCount;
	
	private Timestamp nextAttempt;
	
	private byte[] object;
		
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public QueueItemStatus getStatus() {
		return status;
	}

	public void setStatus(QueueItemStatus status) {
		this.status = status;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Timestamp getSendTimestamp() {
		return sendTimestamp;
	}

	public void setSendTimestamp(Timestamp sendTimestamp) {
		this.sendTimestamp = sendTimestamp;
	}

	public int getDeliveryCount() {
		return deliveryCount;
	}

	public void setDeliveryCount(int deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	public Timestamp getNextAttempt() {
		return nextAttempt;
	}

	public void setNextAttempt(Timestamp nextAttempt) {
		this.nextAttempt = nextAttempt;
	}

	public byte[] getObject() {
		return object;
	}

	public void setObject(byte[] object) {
		this.object = object;
	}
}
