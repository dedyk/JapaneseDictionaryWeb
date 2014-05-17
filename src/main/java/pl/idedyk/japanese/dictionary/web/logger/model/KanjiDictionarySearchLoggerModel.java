package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiRequest;
import pl.idedyk.japanese.dictionary.api.dictionary.dto.FindKanjiResult;

public class KanjiDictionarySearchLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;

	private FindKanjiRequest findKanjiRequest;
	
	private FindKanjiResult findKanjiResult;
	
	public KanjiDictionarySearchLoggerModel(String sessionId, String remoteIp, FindKanjiRequest findKanjiRequest, FindKanjiResult findKanjiResult) {
		
		super(sessionId, remoteIp);
		
		this.findKanjiRequest = findKanjiRequest;
		this.findKanjiResult = findKanjiResult;
	}

	public FindKanjiRequest getFindKanjiRequest() {
		return findKanjiRequest;
	}

	public void setFindKanjiRequest(FindKanjiRequest findKanjiRequest) {
		this.findKanjiRequest = findKanjiRequest;
	}

	public FindKanjiResult getFindKanjiResult() {
		return findKanjiResult;
	}

	public void setFindKanjiResult(FindKanjiResult findKanjiResult) {
		this.findKanjiResult = findKanjiResult;
	}

	@Override
	public String toString() {
		return "KanjiDictionarySearchLoggerModel [findKanjiRequest=" + findKanjiRequest + ", findKanjiResult="
				+ findKanjiResult + "]";
	}
}
