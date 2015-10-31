package pl.idedyk.japanese.dictionary.web.controller.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdminPanelMissingWordsQueueModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String size = "50";
	
	private String wordList = null;
	
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

	public String getWordList() {
		return wordList;
	}

	public void setWordList(String wordList) {
		this.wordList = wordList;
	}
	
	public List<String> getWordListAsList() {
		
		List<String> result = new ArrayList<String>();
		
		if (wordList == null) {
			return result;
		}
		
		String[] wordListSplited = wordList.split("\\r?\\n");
		
		for (String currentWord : wordListSplited) {
			
			if (currentWord.trim().equals("") == false) {
				
				String fixedWord = currentWord.trim();
				
				int tabIndex = fixedWord.indexOf("\t");
				
				if (tabIndex != -1) {					
					fixedWord = fixedWord.substring(0, tabIndex).trim();					
				}
				
				result.add(fixedWord);
			}
		}
		
		return result;
	}
}
