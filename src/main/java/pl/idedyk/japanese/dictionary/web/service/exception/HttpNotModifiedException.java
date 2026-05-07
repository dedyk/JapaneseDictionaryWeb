package pl.idedyk.japanese.dictionary.web.service.exception;

public class HttpNotModifiedException extends RuntimeException {
	
    private static final long serialVersionUID = 1L;
    
    private String etag;
    private String lastModified;

	public HttpNotModifiedException(String message) {
        super(message);
    }
	
	public HttpNotModifiedException(String message, String etag, String lastModified) {
        super(message);
        
        this.etag = etag;
        this.lastModified = lastModified;
    }

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
}