package pl.idedyk.japanese.dictionary.web.service.exception;

public class HttpResourceGoneException extends RuntimeException {
	
    private static final long serialVersionUID = 1L;

	public HttpResourceGoneException(String message) {
        super(message);
    }
}