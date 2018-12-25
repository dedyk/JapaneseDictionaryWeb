package pl.idedyk.japanese.dictionary.web.taglib;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.service.UserAgentService;

public class IsRobotTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	@Override
	public int doStartTag() throws JspException {
		
		ServletRequest servletRequest = pageContext.getRequest();
		
		String userAgent = null;
		
		if (servletRequest instanceof HttpServletRequest) {			
			HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
			
			userAgent = httpServletRequest.getHeader("User-Agent");			
		}
		
		if (userAgent == null) {
			return SKIP_BODY;
		}
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		UserAgentService userAgentService = webApplicationContext.getBean(UserAgentService.class);
		
		boolean robot = userAgentService.isRobot(userAgent);
				
		if (robot == true) {
			return EVAL_BODY_INCLUDE;
			
		} else {
			return SKIP_BODY;
		}		
	}
}
