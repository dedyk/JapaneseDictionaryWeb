package pl.idedyk.japanese.dictionary.web.logger.model;

public class DailyReportLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
	
	private String title;
	
	private String report; 

	public DailyReportLoggerModel(String title, String report) {
		super(null, null, null);
		
		this.title = title;
		this.report = report;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}
}
