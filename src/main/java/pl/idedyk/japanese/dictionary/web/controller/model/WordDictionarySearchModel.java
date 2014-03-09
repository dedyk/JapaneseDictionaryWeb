package pl.idedyk.japanese.dictionary.web.controller.model;

import java.util.ArrayList;
import java.util.List;

import pl.idedyk.japanese.dictionary.api.dto.DictionaryEntryType;

public class WordDictionarySearchModel {
	
	private String word;
	
	private String wordPlace;
	
	private Boolean searchKanji;
	
	private Boolean searchKana;
	
	private Boolean searchRomaji;
	
	private Boolean searchTranslate;
	
	private Boolean searchInfo;
	
	private List<Boolean> dictionaryTypeBooleanList;
	
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

	public Boolean getSearchKanji() {
		return searchKanji;
	}

	public void setSearchKanji(Boolean searchKanji) {
		this.searchKanji = searchKanji;
	}

	public Boolean getSearchKana() {
		return searchKana;
	}

	public void setSearchKana(Boolean searchKana) {
		this.searchKana = searchKana;
	}

	public Boolean getSearchRomaji() {
		return searchRomaji;
	}

	public void setSearchRomaji(Boolean searchRomaji) {
		this.searchRomaji = searchRomaji;
	}

	public Boolean getSearchTranslate() {
		return searchTranslate;
	}

	public void setSearchTranslate(Boolean searchTranslate) {
		this.searchTranslate = searchTranslate;
	}

	public Boolean getSearchInfo() {
		return searchInfo;
	}

	public void setSearchInfo(Boolean searchInfo) {
		this.searchInfo = searchInfo;
	}

	public List<Boolean> getDictionaryTypeBooleanList() {
		return dictionaryTypeBooleanList;
	}

	public void setDictionaryTypeBooleanList(List<Boolean> dictionaryTypeBooleanList) {
		this.dictionaryTypeBooleanList = dictionaryTypeBooleanList;
	}

	public void addDictionaryType(DictionaryEntryType dictionaryEntryType) {
		
		if (dictionaryTypeBooleanList == null) {
			dictionaryTypeBooleanList = new ArrayList<Boolean>();
		}
				
		dictionaryTypeBooleanList.add(Boolean.TRUE);
	}
}
