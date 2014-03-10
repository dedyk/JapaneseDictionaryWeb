package pl.idedyk.japanese.dictionary.web.controller.model;

import java.util.ArrayList;
import java.util.List;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;

public class WordDictionarySearchModel {
	
	private String word;
	
	private String wordPlace;
	
	private List<String> searchIn;
		
	private List<String> dictionaryTypeStringList;
	
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getWordPlace() {
		return wordPlace;
	}

	public void setWordPlace(String wordPlace) {
		this.wordPlace = wordPlace;
	}

	public List<String> getSearchIn() {
		return searchIn;
	}

	public void setSearchIn(List<String> searchIn) {
		this.searchIn = searchIn;
	}

	public List<String> getDictionaryTypeStringList() {
		return dictionaryTypeStringList;
	}

	public void setDictionaryTypeStringList(List<String> dictionaryTypeStringList) {
		this.dictionaryTypeStringList = dictionaryTypeStringList;
	}
	
	public void addDictionaryType(DictionaryEntryType dictionaryEntryType) {
		
		if (dictionaryTypeStringList == null) {
			dictionaryTypeStringList = new ArrayList<String>();
		}
		
		dictionaryTypeStringList.add(dictionaryEntryType.toString());
	}
}
