package pl.idedyk.japanese.dictionary.web.taglib;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.UserAgentType;
import net.sf.uadetector.service.UADetectorServiceFactory;

public class IsRobotTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	@Override
	public int doStartTag() throws JspException {

		UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
		
		ServletRequest servletRequest = pageContext.getRequest();
		
		String userAgent = null;
		
		if (servletRequest instanceof HttpServletRequest) {			
			HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
			
			userAgent = httpServletRequest.getHeader("User-Agent");			
		}
		
		if (userAgent == null) {
			return SKIP_BODY;
		}
		
		ReadableUserAgent readableUserAgent = parser.parse(userAgent);

		if (readableUserAgent == null) {
			return SKIP_BODY;
		}
	
		UserAgentType userAgentType = readableUserAgent.getType();
		
		if (userAgentType == null) {
			return SKIP_BODY;
		}
		
		if (userAgentType == UserAgentType.ROBOT) {
			return EVAL_BODY_INCLUDE;
			
		} else {
			return SKIP_BODY;
		}		
	}
}
