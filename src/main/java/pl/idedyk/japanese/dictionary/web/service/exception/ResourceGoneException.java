package pl.idedyk.japanese.dictionary.web.service.exception;

public class ResourceGoneException extends RuntimeException {
	
    private static final long serialVersionUID = 1L;

	public ResourceGoneException(String message) {
        super(message);
    }
}