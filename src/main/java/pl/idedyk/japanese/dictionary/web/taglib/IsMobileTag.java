package pl.idedyk.japanese.dictionary.web.taglib;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import pl.idedyk.japanese.dictionary.web.common.Utils;

public class IsMobileTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private boolean mobile;

	@Override
	public int doStartTag() throws JspException {
		
		ServletRequest servletRequest = pageContext.getRequest();
		
		String userAgent = null;
		
		if (servletRequest instanceof HttpServletRequest) {			
			HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
			
			userAgent = httpServletRequest.getHeader("User-Agent");			
		}
		
		if (userAgent == null) {
			return EVAL_BODY_INCLUDE;
		}
				
		boolean mobileResult = Utils.isMobile(userAgent);
				
		if (mobileResult == mobile) {
			return EVAL_BODY_INCLUDE;
		
		} else {
			return SKIP_BODY;
		}
	}

	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}
}
