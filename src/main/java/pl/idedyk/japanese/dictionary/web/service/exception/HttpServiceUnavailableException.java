package pl.idedyk.japanese.dictionary.web.service.exception;

public class HttpServiceUnavailableException extends RuntimeException {
	
    private static final long serialVersionUID = 1L;

	public HttpServiceUnavailableException(String message) {
        super(message);
    }
}