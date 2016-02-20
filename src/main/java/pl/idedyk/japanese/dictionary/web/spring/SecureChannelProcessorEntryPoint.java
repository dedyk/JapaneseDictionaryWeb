package pl.idedyk.japanese.dictionary.web.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.access.channel.AbstractRetryEntryPoint;

public class SecureChannelProcessorEntryPoint extends AbstractRetryEntryPoint {

	private static final String scheme = "https://";
	private static final int standardPort = 443;
	
	public SecureChannelProcessorEntryPoint() {
		super(scheme, standardPort);
	}
	
	@Override
    public void commence(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String queryString = request.getQueryString();
        String redirectUrl = request.getRequestURI() + ((queryString == null) ? "" : ("?" + queryString));

        Integer currentPort = Integer.valueOf(getPortResolver().getServerPort(request));
        Integer redirectPort = getMappedPort(currentPort);

        if (redirectPort != null) {
            boolean includePort = redirectPort.intValue() != standardPort;

            redirectUrl = scheme + request.getServerName() + ((includePort) ? (":" + redirectPort) : "") + redirectUrl;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Redirecting to: " + redirectUrl);
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

	@Override
	protected Integer getMappedPort(Integer mapFromPort) {
		return getPortMapper().lookupHttpsPort(mapFromPort);
	}

}
