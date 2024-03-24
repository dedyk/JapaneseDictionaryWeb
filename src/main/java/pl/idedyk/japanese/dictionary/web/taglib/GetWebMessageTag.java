package pl.idedyk.japanese.dictionary.web.taglib;

import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.service.MessageService;
import pl.idedyk.japanese.dictionary.web.service.MessageService.Message;

public class GetWebMessageTag extends TagSupport {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public int doStartTag() throws JspException {

		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		MessageService messageService = webApplicationContext.getBean(MessageService.class);
		
		Message.MessageEntry messageForWeb = messageService.getMessageForWeb();
		
		if (messageForWeb == null) {
			return SKIP_BODY;
		}
		
		String messageText = messageForWeb.getMessage();
		
		if (messageText == null) {
			return SKIP_BODY;
		}
		
		messageText = messageText.trim();
		
		if (messageText.equals("") == true) {
			return SKIP_BODY;
		}		
		
		try {
            JspWriter out = pageContext.getOut();
            
            Div div = new Div();
            
            div.setClazz("alert alert-info");
            
            div.addHtmlElement(new Text(messageText));
            
            div.render(out);
            
            return SKIP_BODY;
 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
