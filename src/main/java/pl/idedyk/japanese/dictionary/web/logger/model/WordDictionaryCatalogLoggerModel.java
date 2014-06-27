package pl.idedyk.japanese.dictionary.web.logger.model;

public class WordDictionaryCatalogLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private int pageNo;
	
	public WordDictionaryCatalogLoggerModel(LoggerModelCommon loggerModelCommon, int pageNo) {
		
		super(loggerModelCommon);
		
		this.pageNo = pageNo;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	@Override
	public String toString() {
		return "WordDictionaryCatalogLoggerModel [pageNo=" + pageNo + "]";
	}
}
