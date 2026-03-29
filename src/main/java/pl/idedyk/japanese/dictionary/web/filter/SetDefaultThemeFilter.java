package pl.idedyk.japanese.dictionary.web.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;

public class SetDefaultThemeFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;        
		HttpSession httpSession = httpServletRequest.getSession();
		
		// pobieramy motyw z ciasteczka (lub wartosc domyslna)
        Utils.ThemeType theme = Utils.getTheme(httpServletRequest);
		
        // zapisujemy do ciasteczka i do sesji
        Utils.setTheme(httpServletResponse, httpSession, theme);
		
		chain.doFilter(request, response);
	}
}
