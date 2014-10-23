package pl.idedyk.japanese.dictionary.web.logger.model;

public class WordDictionaryNameCatalogLoggerModel extends LoggerModelCommon {

	private static final long serialVersionUID = 1L;
	
	private int pageNo;
	
	public WordDictionaryNameCatalogLoggerModel(LoggerModelCommon loggerModelCommon, int pageNo) {
		
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
		return "WordDictionaryNameCatalogLoggerModel [pageNo=" + pageNo + "]";
	}
}
