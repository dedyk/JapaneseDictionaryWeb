package pl.idedyk.japanese.dictionary.web.spring;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.Assert;

public class SecureChannelProcessor extends org.springframework.security.web.access.channel.SecureChannelProcessor {

	@Override
    public void decide(FilterInvocation invocation, Collection<ConfigAttribute> config) throws IOException, ServletException {
		
        Assert.isTrue((invocation != null) && (config != null), "Nulls cannot be provided");

        for (ConfigAttribute attribute : config) {
        	
            if (supports(attribute)) {
            	
            	HttpServletRequest httpRequest = invocation.getHttpRequest();
                
            	String xForwardedProto = httpRequest.getHeader("x-forwarded-proto");
            	
            	if (httpRequest.isSecure() == false && (xForwardedProto == null || xForwardedProto.equals("https") == false)) {
            		
                	getEntryPoint().commence(invocation.getRequest(), invocation.getResponse());
                }
            }
        }
    }
}
