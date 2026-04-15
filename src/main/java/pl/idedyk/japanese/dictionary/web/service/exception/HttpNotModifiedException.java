package pl.idedyk.japanese.dictionary.web.service.exception;

public class HttpNotModifiedException extends RuntimeException {
	
    private static final long serialVersionUID = 1L;

	public HttpNotModifiedException(String message) {
        super(message);
    }
}