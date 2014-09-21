package pl.idedyk.japanese.dictionary.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel.Result;

@Component
public class AuthenticationService implements AuthenticationProvider, AuthenticationSuccessHandler, AuthenticationFailureHandler, AccessDeniedHandler {

	private static final Logger logger = Logger.getLogger(AuthenticationService.class);
	
	@Value("${admin.username}")
	private String adminUsername;
	
	@Value("${admin.password}")
	private String adminPassword;
	
	@Autowired
	private LoggerSender loggerSender;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_LOGIN_SUCCESS);
    	adminLoggerModel.setResult(Result.OK);

		// logowanie
		loggerSender.sendLog(adminLoggerModel);
		
		// przekierowanie do panelu admina		
		response.sendRedirect(request.getContextPath() + "/adm/panel");
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		logger.error("Niepoprawne logowanie do panelu administratora");
		
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_LOGIN_FAILURE);
    	adminLoggerModel.setResult(Result.ERROR);
		
    	adminLoggerModel.addParam("user", exception.getAuthentication().getName());
    	adminLoggerModel.addParam("password", exception.getAuthentication().getCredentials().toString());
    	
		// logowanie
		loggerSender.sendLog(adminLoggerModel);

		// przekierowanie na strone logowania
		response.sendRedirect(request.getContextPath() + "/admlogin?error");
	}

	@SuppressWarnings("deprecation")
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		
		if (adminUsername.equals(username) == true && adminPassword.equals(password) == true) {
			
			List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
			
			grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			
	        return new UsernamePasswordAuthenticationToken(adminUsername, adminPassword, grantedAuths);

		} else {
			
			BadCredentialsException badCredentialsException = new BadCredentialsException("Błąd logowania");
			
			badCredentialsException.setAuthentication(authentication);
			
			throw badCredentialsException;			
		}		
	}
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
						
		logger.error("Brak dostępu do strony: " + Utils.getRequestURL(request));
		
		// logowanie
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_ACCESS_ERROR);
    	adminLoggerModel.setResult(Result.ERROR);
		
    	adminLoggerModel.addParam("user", request.getRemoteUser());
    	
		// logowanie
		loggerSender.sendLog(adminLoggerModel);

		// przekierowanie na strone bledu
		response.sendRedirect(request.getContextPath() + "/admAccessDenied");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
