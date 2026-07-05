package pl.idedyk.japanese.dictionary.web.taglib;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.html.A;
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
            
            // operation i sub operation
            StringBuffer operation = new StringBuffer();
            
            operation.append(genericLog.getOperation().toString());
            
            if (genericLog.getOperation() == GenericLogOperationEnum.ADMIN_REQUEST) {
            	
            	AdminRequestLog adminRequestLog = mySQLConnector.getAdminRequestLogByGenericId(genericLog.getId());
            	
            	if (adminRequestLog != null) {
            		operation.append(" (" + adminRequestLog.getType() + ")");
            		// addColumn(tr, adminRequestLog.getType());
            		
            	} else {
            		// addColumn(tr, (String)null);
            	}
            	
            } else {
            	// addColumn(tr, (String)null);
            }      
            
            addColumn(tr, operation.toString());
            
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
            
            // remote ip country
            addColumn(tr, genericLog.getRemoteIpCountry());
            
            // remote ip
            String remoteIp = genericLog.getRemoteIp();
            
            // List<String> remoteIpAddInfoList = new ArrayList<>();
            
            // String remoteIpAsn = genericLog.getRemoteIpAsn();
            // String remoteIpAsnOrganizationName = genericLog.getRemoteIpAsnOrganizationName();
            // String remoteIpCountry = genericLog.getRemoteIpCountry();            		
            
            /*
            if (remoteIpAsn != null) {
            	remoteIpAddInfoList.add(remoteIpAsn);
            }

            if (remoteIpAsnOrganizationName != null) {
            	remoteIpAddInfoList.add(remoteIpAsnOrganizationName);
            }

            if (remoteIpCountry != null) {
            	remoteIpAddInfoList.add(remoteIpCountry);
            }
            */
            
            StringBuffer remoteIpJoined = new StringBuffer();
            
            if (remoteIp != null) {
            	remoteIpJoined.append(remoteIp);
            }
            
            /*
            if (remoteIpAddInfoList.size() > 0) {
            	remoteIpJoined.append(" (" + String.join(", ", remoteIpAddInfoList) + ")");
            } 
            */           
            
            addColumn(tr, remoteIpJoined.toString());
            
            // remote host
            addColumn(tr, genericLog.getRemoteHost() != null ? genericLog.getRemoteHost() : "");
            
            // remote ip asn
            addColumn(tr, genericLog.getRemoteIpAsn() != null ? genericLog.getRemoteIpAsn() + " (" + genericLog.getRemoteIpAsnOrganizationName() + ")" : "");

            // remote ip asn organization name
            // addColumn(tr, genericLog.getRemoteIpAsnOrganizationName());
            
            // details link
	    	Td detailsLinkTd = new Td();
	    	tr.addHtmlElement(detailsLinkTd);
            
            String link = LinkGenerator.generateShowGenericLog(pageContext.getServletContext().getContextPath(), genericLog);
            A linkButton = new A();
            detailsLinkTd.addHtmlElement(linkButton);
            
            linkButton.setClazz("btn btn-default");
            linkButton.setHref(link);
            
            linkButton.addHtmlElement(new Text(messageSource.getMessage(
            		"admin.panel.genericlog.column.detailsButton", null, Locale.getDefault()), true));
            
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
        
        td.addHtmlElement(new Text(value, true));
	}
	
	/*
	private void addColumn(Tr tr, IHtmlElement htmlElementValue) {
				
        Td td = new Td();
        tr.addHtmlElement(td);
        
        td.addHtmlElement(htmlElementValue);		
	}
	*/

	public GenericLog getGenericLog() {
		return genericLog;
	}

	public void setGenericLog(GenericLog genericLog) {
		this.genericLog = genericLog;
	}
}
