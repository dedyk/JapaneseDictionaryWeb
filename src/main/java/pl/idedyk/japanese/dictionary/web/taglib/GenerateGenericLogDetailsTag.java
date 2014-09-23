package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.html.A;
import pl.idedyk.japanese.dictionary.web.html.B;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Hr;
import pl.idedyk.japanese.dictionary.web.html.IHtmlElement;
import pl.idedyk.japanese.dictionary.web.html.Label;
import pl.idedyk.japanese.dictionary.web.html.Table;
import pl.idedyk.japanese.dictionary.web.html.Td;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Tr;
import pl.idedyk.japanese.dictionary.web.mysql.MySQLConnector;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.taglib.utils.Menu;

public class GenerateGenericLogDetailsTag extends GenerateDictionaryDetailsTagAbstract {
	
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

            if (genericLog == null) {
            	
            	Div errorDiv = new Div("alert alert-danger");
            	
            	errorDiv.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.null")));
            	
            	errorDiv.render(out);
            	
            	return SKIP_BODY;
            }
            
            Div mainContentDiv = new Div();
            
            // tytul strony
            mainContentDiv.addHtmlElement(generateTitle());
                        
            Div contentDiv = new Div("col-md-12");
            mainContentDiv.addHtmlElement(contentDiv);
            
            // generowanie informacji podstawowych
            contentDiv.addHtmlElement(generateMainInfo());
            
            // renderowanie
            mainContentDiv.render(out);
            
            return SKIP_BODY;
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	private H generateTitle() throws IOException {
		
		H pageHeader = new H(4);
				
		pageHeader.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.title")));
				
		return pageHeader;
	}
	
	private Div generateMainInfo() throws IOException {
		
		Div panelDiv = new Div("panel panel-default");
		
		Div panelHeading = new Div("panel-heading");
		
		// tytul sekcji
		H h3Title = new H(3, "panel-title");
		h3Title.setId("mainInfoId");
		
		h3Title.addHtmlElement(new Text(getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo")));
				
		panelHeading.addHtmlElement(h3Title);
		
		panelDiv.addHtmlElement(panelHeading);
		
		// zawartosc sekcji
		Div panelBody = new Div("panel-body");
		
		Table table = new Table();
		
		// id
		addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.id"), String.valueOf(genericLog.getId()));
		
		// timestamp
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.timestamp"), String.valueOf(simpleDateFormat.format(genericLog.getTimestamp())));

        // operation
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.operation"), genericLog.getOperation().toString());
        
        // session id
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.sessionId"), genericLog.getSessionId());
        
        // user agent
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.userAgent"), genericLog.getUserAgent());
        
        // request url
        String requestURL = genericLog.getRequestURL();
        
        if (requestURL == null) {
        	addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.requestUrl"), requestURL);
        	
        } else {
        	requestURL = URLDecoder.decode(requestURL, "UTF8");            	
        	        	
        	A requestUrlLink = new A();
        	
        	requestUrlLink.setHref(requestURL);
        	requestUrlLink.addHtmlElement(new Text(requestURL));            	

        	addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.requestUrl"), requestUrlLink);
        }        

        // referer url
        String refererURL = genericLog.getRefererURL();
        
        if (refererURL == null) {
        	addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.refererUrl"), refererURL);
        	
        } else {
        	refererURL = URLDecoder.decode(refererURL, "UTF8");            	
        	        	
        	A refererUrlLink = new A();
        	
        	refererUrlLink.setHref(refererURL);
        	refererUrlLink.addHtmlElement(new Text(refererURL));            	

        	addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.refererUrl"), refererUrlLink);
        }        
        
        // remote ip
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.remoteIp"), genericLog.getRemoteIp());
        
        // remote host
        addRowToTable(table, getMessage("admin.panel.genericLogDetails.page.genericLog.mainInfo.remoteHost"), genericLog.getRemoteHost());
		
		/*
		// kanji
		Div kanjiDiv = generateKanjiSection(mainInfoMenu);
		
		if (kanjiDiv != null) {
			panelBody.addHtmlElement(kanjiDiv);	
			
			panelBody.addHtmlElement(new Hr());
		}
		
		// znaczenie znakow kanji
        Div knownKanjiDiv = generateKnownKanjiDiv(mainInfoMenu);
        
        if (knownKanjiDiv != null) {
        	panelBody.addHtmlElement(knownKanjiDiv);
        	panelBody.addHtmlElement(new Hr());
        }
		
		// czytanie
		Div readingDiv = generateReadingSection(mainInfoMenu);
		panelBody.addHtmlElement(readingDiv);
		panelBody.addHtmlElement(new Hr());
		
        // tlumaczenie
        Div translate = generateTranslateSection(mainInfoMenu);
        panelBody.addHtmlElement(translate);
        
        // generuj informacje dodatkowe
        Div additionalInfo = generateAdditionalInfo(mainInfoMenu);

        if (additionalInfo != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(additionalInfo);
        }
        
        // czesc mowy
        Div wordTypeDiv = generateWordType(mainInfoMenu);
        
        if (wordTypeDiv != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(wordTypeDiv);
        }
        
        // dodatkowe atrybuty
		Div additionalAttributeDiv = generateAttribute(mainInfoMenu);

        if (additionalAttributeDiv != null) {
        	panelBody.addHtmlElement(new Hr());
        	panelBody.addHtmlElement(additionalAttributeDiv);
        }
        */
		
		panelBody.addHtmlElement(table);
        		
		panelDiv.addHtmlElement(panelBody);
		
		return panelDiv;
	}
	
	private void addRowToTable(Table table, String label, String value) {

		if (value == null) {
			value = "";
		}		
		
		addRowToTable(table, label, new Text(value));
	}
	
	private void addRowToTable(Table table, String label, IHtmlElement valueHtmlElement) {
		
		Tr tr = new Tr();
		
		Td td1 = new Td(null, "padding: 0px 10px 0px 0px");
		
		Label td1Label = new Label();
		
		td1Label.addHtmlElement(new Text(label));
		
		td1.addHtmlElement(td1Label);
		tr.addHtmlElement(td1);
		
		Td td2 = new Td();		
		td2.addHtmlElement(valueHtmlElement);
		
		tr.addHtmlElement(td2);
		
		table.addHtmlElement(tr);
	}
	
	private String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}

	public GenericLog getGenericLog() {
		return genericLog;
	}

	public void setGenericLog(GenericLog genericLog) {
		this.genericLog = genericLog;
	}
}
