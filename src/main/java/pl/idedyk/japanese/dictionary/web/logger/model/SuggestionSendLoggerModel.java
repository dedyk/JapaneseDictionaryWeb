package pl.idedyk.japanese.dictionary.web.logger.model;

public class SuggestionSendLoggerModel extends LoggerModelCommon {
	
	private static final long serialVersionUID = 1L;
	
	private String title;
	
	private String sender;
	
	private String body;
		
	public SuggestionSendLoggerModel(String sessionId, String remoteIp, String title, String sender, String body) {		
		super(sessionId, remoteIp);
		
		this.title = title;
		this.sender = sender;
		this.body = body;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
