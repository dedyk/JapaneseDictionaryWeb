package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.IHtmlElement;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.AdminRequestLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLogOperationEnum;

public class GenericLogItemTableRowTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private GenericLog genericLog;
	
	private MySQLConnector mySQLConnector;
	
	private MessageSource messageSource;
	
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		this.mySQLConnector = webApplicationContext.getBean(MySQLConnector.class);
		
		try {
            JspWriter out = pageContext.getOut();
            
            Tr tr = new Tr();            
            
            // id
            addColumn(tr, String.valueOf(genericLog.getId()));
                        
            // timestamp
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            addColumn(tr, String.valueOf(simpleDateFormat.format(genericLog.getTimestamp())));

            // operation
            addColumn(tr, genericLog.getOperation().toString());
            
            // sub operation
            if (genericLog.getOperation() == GenericLogOperationEnum.ADMIN_REQUEST) {
            	
            	AdminRequestLog adminRequestLog = mySQLConnector.getAdminRequestLogByGenericId(genericLog.getId());
            	
            	if (adminRequestLog != null) {
            		addColumn(tr, adminRequestLog.getType());
            		
            	} else {
            		addColumn(tr, (String)null);
            	}
            	
            } else {
            	addColumn(tr, (String)null);
            }            
            
            // request url
            /*
            String requestUrl = genericLog.getRequestURL();
            
            if (requestUrl == null) {
            	addColumn(tr, requestUrl);
            	
            } else {
            	requestUrl = URLDecoder.decode(requestUrl, "UTF8");            	
            	
            	String shortRequestUrl = requestUrl.substring(0, requestUrl.length() > 150 ? 150 : requestUrl.length());
            	
            	A link = new A();
            	
            	link.setHref(requestUrl);
            	link.addHtmlElement(new Text(shortRequestUrl));            	
            	
            	addColumn(tr, link);
            }
            */
            
            // remote ip
            addColumn(tr, genericLog.getRemoteIp());
            
            // remote host
            addColumn(tr, genericLog.getRemoteHost());
                        
            // details link
	    	Td detailsLinkTd = new Td();
	    	tr.addHtmlElement(detailsLinkTd);
            
            String link = LinkGenerator.generateShowGenericLog(pageContext.getServletContext().getContextPath(), genericLog);
            A linkButton = new A();
            detailsLinkTd.addHtmlElement(linkButton);
            
            linkButton.setClazz("btn btn-default");
            linkButton.setHref(link);
            
            linkButton.addHtmlElement(new Text(messageSource.getMessage(
            		"admin.panel.genericlog.column.detailsButton", null, Locale.getDefault())));
            
            tr.render(out);
            
            return SKIP_BODY;
 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	private void addColumn(Tr tr, String value) {
		
		if (value == null) {
			value = "";
		}
		
        Td td = new Td();
        tr.addHtmlElement(td);
        
        td.addHtmlElement(new Text(value));		
	}
	
	private void addColumn(Tr tr, IHtmlElement htmlElementValue) {
				
        Td td = new Td();
        tr.addHtmlElement(td);
        
        td.addHtmlElement(htmlElementValue);		
	}

	public GenericLog getGenericLog() {
		return genericLog;
	}

	public void setGenericLog(GenericLog genericLog) {
		this.genericLog = genericLog;
	}
}
