package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindWordResult;

public class WordDictionarySearchLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private FindWordRequest findWordRequest;
	
	private FindWordResult findWordResult;
	
	public WordDictionarySearchLoggerModel(LoggerModelCommon loggerModelCommon, FindWordRequest findWordRequest, FindWordResult findWordResult) {
		
		super(loggerModelCommon);
		
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
