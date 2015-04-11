package pl.idedyk.japanese.dictionary.web.controller.model;

import java.io.Serializable;

public class AdminPanelMissingWordsQueueModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String size = "50";
	
	private boolean lock = false;

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public boolean isLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}
}
