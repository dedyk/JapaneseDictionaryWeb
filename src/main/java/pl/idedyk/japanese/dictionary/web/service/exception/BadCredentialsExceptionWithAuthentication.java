package pl.idedyk.japanese.dictionary.web.service.exception;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

public class BadCredentialsExceptionWithAuthentication extends BadCredentialsException {
	
	private static final long serialVersionUID = 1L;
	
	private Authentication authentication;
	
	public BadCredentialsExceptionWithAuthentication(String msg) {
		super(msg);
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}	
}
