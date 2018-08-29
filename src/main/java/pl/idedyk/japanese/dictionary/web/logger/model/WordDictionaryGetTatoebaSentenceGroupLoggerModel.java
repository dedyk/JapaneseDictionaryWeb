package pl.idedyk.japanese.dictionary.web.logger.model;

public class WordDictionaryGetTatoebaSentenceGroupLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private String groupId;
	
	public WordDictionaryGetTatoebaSentenceGroupLoggerModel(LoggerModelCommon loggerModelCommon, String groupId) {
		
		super(loggerModelCommon);
		
		this.groupId = groupId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Override
	public String toString() {
		return "WordDictionaryGetTatoebaSentenceGroupLoggerModel [groupId=" + groupId + "]";
	}
}
