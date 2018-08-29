package pl.idedyk.japanese.dictionary.web.logger.model;

import pl.idedyk.japanese.dictionary.api.dto.GroupEnum;

public class WordDictionaryGetGroupDictionaryEntriesLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private GroupEnum groupEnum;
	
	public WordDictionaryGetGroupDictionaryEntriesLoggerModel(LoggerModelCommon loggerModelCommon, GroupEnum groupEnum) {
		
		super(loggerModelCommon);
		
		this.groupEnum = groupEnum;
	}

	public GroupEnum getGroupEnum() {
		return groupEnum;
	}

	public void setGroupEnum(GroupEnum groupEnum) {
		this.groupEnum = groupEnum;
	}

	@Override
	public String toString() {
		return "WordDictionaryGetGroupDictionaryEntriesLoggerModel [groupEnum=" + groupEnum + "]";
	}
}
