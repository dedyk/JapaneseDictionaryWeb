package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.idedyk.japanese.dictionary.web.config.xsd.Config.Redirects.Redirect;
import pl.idedyk.japanese.dictionary.web.service.ConfigService;
import pl.idedyk.japanese.dictionary.web.service.ConfigService.ConfigWrapper;

public class RedirectByConfigFilter extends RedirectCommonFilter implements Filter {
		
	private Map<String, String> redirectMap = new TreeMap<>();
	private Long redirectMapLastModified = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		ServletContext servletContext = request.getServletContext();		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
				
		//
		
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		HttpServletResponse httpServletResponse = (HttpServletResponse)response;
		
				
		// prawdzenie, czy nie nalezy przekierowac zgodnie z plikiem konfiguracyjnym
		String redirectUrl = getUrlToRedirectByConfig(webApplicationContext, httpServletRequest, httpServletResponse);
				
		if (redirectUrl == null) { // nie ma przekierowania
			chain.doFilter(request, response); // idziemy dalej z normalnym wywolaniem			
			
			return;
		}
		
		// robimy przekierowanie
		redirectToUrl(webApplicationContext, httpServletRequest, httpServletResponse, redirectUrl);
        
        // zrobienie commit'a
        response.flushBuffer();		
	}
	
	@Override
	public void destroy() {
		// noop		
	}
	
	private synchronized String getUrlToRedirectByConfig(WebApplicationContext webApplicationContext, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		ConfigService configService = webApplicationContext.getBean(ConfigService.class);
		
		// pobranie konfiguracji
		ConfigWrapper config = configService.getConfig();
		
		// sprawdzenie, czy nie nalezy ponownie przegenerowac mapy przekierowan
		if (redirectMapLastModified == null || redirectMapLastModified.longValue() != config.getLastModified().longValue()) { // przegenerowanie
			redirectMap.clear();
			
			// pobranie przekierowac z konfiguracji
			List<Redirect> redirect = config.getConfig().getRedirects().getRedirect();
			
			redirect.stream().forEach(r -> redirectMap.put(r.getFrom(), r.getTo()));
			
			redirectMapLastModified = config.getLastModified();
		}
		
		// sprawdzenie, czy nie nalezy przekierowac
		String currentUrl = httpServletRequest.getRequestURI();
		
		return redirectMap.get(currentUrl);
	}
}
