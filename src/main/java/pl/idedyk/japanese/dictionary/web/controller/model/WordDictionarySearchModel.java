package pl.idedyk.japanese.dictionary.web.controller.model;

import java.util.ArrayList;
import java.util.List;

public class WordDictionarySearchModel {
	
	private String word;
	
	private String wordPlace;
	
	private Boolean searchKanji;
	
	private Boolean searchKana;
	
	private Boolean searchRomaji;
	
	private Boolean searchTranslate;
	
	private Boolean searchInfo;
	
	private List<DictionaryType> dictionaryTypeList;

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
	
	public List<DictionaryType> getDictionaryTypeList() {
		return dictionaryTypeList;
	}
	
	public List<DictionaryType> cgtDictionaryTypeList() {
		
		if (dictionaryTypeList == null) {
			dictionaryTypeList = new ArrayList<WordDictionarySearchModel.DictionaryType>();
		}
		
		return dictionaryTypeList;
	}

	public void setDictionaryTypeList(List<DictionaryType> dictionaryTypeList) {
		this.dictionaryTypeList = dictionaryTypeList;
	}

	public static class DictionaryType {
		
		private String label;
		
		private boolean search;

		public DictionaryType(String label, boolean search) {
			this.label = label;
			this.search = search;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public boolean getSearch() {
			return search;
		}

		public void setSearch(boolean search) {
			this.search = search;
		}
	}
}
