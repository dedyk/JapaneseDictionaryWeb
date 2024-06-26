package pl.idedyk.japanese.dictionary.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.AdminLoggerModel.Result;
import pl.idedyk.japanese.dictionary.web.service.exception.BadCredentialsExceptionWithAuthentication;

@Component
public class AuthenticationService implements AuthenticationProvider, AuthenticationSuccessHandler, AuthenticationFailureHandler, AccessDeniedHandler, LogoutSuccessHandler {

	private static final Logger logger = LogManager.getLogger(AuthenticationService.class);
	
	@Value("${admin.username}")
	private String adminUsername;
	
	@Value("${admin.password}")
	private String adminPassword;
	
	@Autowired
	private PortResolver portResolver;
	
	@Autowired
	private PortMapper portMapper;
	
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
		redirect(request, response, "/adm/panel");
	}
	
	private void redirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl) throws IOException {
				
        Integer currentPort = Integer.valueOf(portResolver.getServerPort(request));
        Integer redirectPort = portMapper.lookupHttpsPort(currentPort);

        if (redirectPort != null) {
        	
            boolean includePort = redirectPort.intValue() != 443;

            redirectUrl = "https://" + request.getServerName() + ((includePort) ? (":" + redirectPort) : "") + redirectUrl;
            
            response.sendRedirect(redirectUrl);
            
        } else {
        	
        	response.sendRedirect(request.getContextPath() + redirectUrl);
        }		
	}
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		logger.error("Niepoprawne logowanie do panelu administratora");
		
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_LOGIN_FAILURE);
    	adminLoggerModel.setResult(Result.ERROR);
    	
    	if (exception instanceof BadCredentialsExceptionWithAuthentication) {
    		
    		BadCredentialsExceptionWithAuthentication badCredentialsExceptionWithAuthentication = (BadCredentialsExceptionWithAuthentication)exception;
    		
        	adminLoggerModel.addParam("user", badCredentialsExceptionWithAuthentication.getAuthentication().getName());
        	adminLoggerModel.addParam("password", badCredentialsExceptionWithAuthentication.getAuthentication().getCredentials().toString());
    	}		
    	
		// logowanie
		loggerSender.sendLog(adminLoggerModel);

		// przekierowanie na strone logowania
		redirect(request, response, "/admlogin?error");
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		
		if (adminUsername.equals(username) == true && adminPassword.equals(password) == true) {
			
			List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
			
			grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			
	        return new UsernamePasswordAuthenticationToken(adminUsername, adminPassword, grantedAuths);

		} else {
			
			BadCredentialsExceptionWithAuthentication badCredentialsExceptionWithAuthentication = new BadCredentialsExceptionWithAuthentication("Błąd logowania");
			
			badCredentialsExceptionWithAuthentication.setAuthentication(authentication);
			
			throw badCredentialsExceptionWithAuthentication;
		}		
	}
	
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		
		logger.info("Wylogowano z panelu administratora");
	
    	AdminLoggerModel adminLoggerModel = new AdminLoggerModel(Utils.createLoggerModelCommon(request));
    	
    	adminLoggerModel.setType(AdminLoggerModel.Type.ADMIN_LOGOUT_SUCCESS);
    	adminLoggerModel.setResult(Result.OK);
		    	
		// logowanie
		loggerSender.sendLog(adminLoggerModel);

		// przekierowanie na strone glowna
		redirect(request, response, "/");		
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
		redirect(request, response, "/accessDenied");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
