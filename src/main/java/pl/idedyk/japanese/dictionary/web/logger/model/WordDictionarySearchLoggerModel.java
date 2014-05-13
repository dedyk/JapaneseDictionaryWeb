package pl.idedyk.japanese.dictionary.web.logger.model;

import java.io.Serializable;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;

public class WordDictionarySearchLoggerModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private FindWordRequest findWordRequest;
	
	private FindWordResult findWordResult;
		
	public WordDictionarySearchLoggerModel() {
	}

	public WordDictionarySearchLoggerModel(FindWordRequest findWordRequest, FindWordResult findWordResult) {
		this.findWordRequest = findWordRequest;
		this.findWordResult = findWordResult;
	}

	public FindWordRequest getFindWordRequest() {
		return findWordRequest;
	}

	public void setFindWordRequest(FindWordRequest findWordRequest) {
		this.findWordRequest = findWordRequest;
	}

	public FindWordResult getFindWordResult() {
		return findWordResult;
	}

	public void setFindWordResult(FindWordResult findWordResult) {
		this.findWordResult = findWordResult;
	}

	@Override
	public String toString() {
		return "WordDictionarySearchLoggerModel [findWordRequest=" + findWordRequest + ", findWordResult="
				+ findWordResult + "]";
	}
}
