package pl.idedyk.japanese.dictionary.web.mysql.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class WordDictionarySearchMissingWordQueue implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private String missingWord;
	
	private int counter;
	
	private Timestamp firstAppearanceTimestamp;
	private Timestamp lastAppearanceTimestamp;
	
	private Timestamp lockTimestamp;
	
	private int priority;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMissingWord() {
		return missingWord;
	}

	public void setMissingWord(String missingWord) {
		this.missingWord = missingWord;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public Timestamp getFirstAppearanceTimestamp() {
		return firstAppearanceTimestamp;
	}

	public void setFirstAppearanceTimestamp(Timestamp firstAppearanceTimestamp) {
		this.firstAppearanceTimestamp = firstAppearanceTimestamp;
	}

	public Timestamp getLastAppearanceTimestamp() {
		return lastAppearanceTimestamp;
	}

	public void setLastAppearanceTimestamp(Timestamp lastAppearanceTimestamp) {
		this.lastAppearanceTimestamp = lastAppearanceTimestamp;
	}

	public Timestamp getLockTimestamp() {
		return lockTimestamp;
	}

	public void setLockTimestamp(Timestamp lockTimestamp) {
		this.lockTimestamp = lockTimestamp;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
