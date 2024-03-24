package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.TagSupport;
import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Script;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.Button.ButtonType;

public class StartInfoTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		// pobranie sesji
		HttpSession session = pageContext.getSession();
		
		if (session.getAttribute("showStartInfo") != null) {
			return SKIP_BODY;
			
		} else {
			session.setAttribute("showStartInfo", false);
		}
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		MessageSource messageSource = (MessageSource)webApplicationContext.getBean("messageSource");

		try {
            JspWriter out = pageContext.getOut();
            
            Div mainDiv = generateBody(pageContext, messageSource);
            
            // renderowanie tresci
            mainDiv.render(out);
                        
            return SKIP_BODY;
 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }		
	}
	
	public static Div generateBody(PageContext pageContext, MessageSource messageSource) {
		
		final String dialogId = "startInfoDialogId";
		
		final int width = 600;
		final int height = 500;
		
		// glowny div calosci
		Div mainDiv = new Div();
		
		// skrypt wyswietlajacy okienko
		Script script = new Script();
		
		StringBuffer scriptBody = new StringBuffer();
		
		scriptBody.append("   $(document).ready(function() {\n");
		scriptBody.append("      $( '#" + dialogId + "' ).modal();\n");
		scriptBody.append("   });\n");
		
		Text scriptText = new Text(scriptBody.toString());
		
		script.addHtmlElement(scriptText);
		
		mainDiv.addHtmlElement(script);
		
		String dialogTitle = messageSource.getMessage("start.info.dialog.title", new String[] { }, null);
		
		// glowny div
		Div startInfoDialogDiv = new Div("modal fade");
		
		startInfoDialogDiv.setId(dialogId);
		startInfoDialogDiv.setWidth(width);
		startInfoDialogDiv.setHeight(height);

		Div modalDialog = new Div("modal-dialog");

		modalDialog.setStyle("width: " + width + "px; top: 25%");
		startInfoDialogDiv.addHtmlElement(modalDialog);
		
		Div modalContent = new Div("modal-content");
		modalDialog.addHtmlElement(modalContent);
		
		Div moldalHeader = new Div("modal-header");
		modalContent.addHtmlElement(moldalHeader);

		// przycisk zamkniecia
		Button closeButton = new Button("close");
		
		closeButton.setButtonType(ButtonType.BUTTON);
		closeButton.setDataDismiss("modal");
		closeButton.setAriaHidden("true");
		
		closeButton.addHtmlElement(new Text("&times;"));
		
		moldalHeader.addHtmlElement(closeButton);

		// tytul okienka
		H dialogTitleH4 = new H(4, "modal-title");
		dialogTitleH4.addHtmlElement(new Text(dialogTitle));
		
		moldalHeader.addHtmlElement(dialogTitleH4);
		
		// zawartosc glowna okienka
		Div modalBody = new Div("modal-body");
		modalContent.addHtmlElement(modalBody);
		
		// tresc glowna
		String body = messageSource.getMessage("start.info.dialog.body", new String[] { }, null);
		
		body = body.replaceAll("\n", "<br/>");
		
		modalBody.addHtmlElement(new Text(body));		
						
		// naglowek okienka
		Div modalFooter = new Div("modal-footer");
		modalContent.addHtmlElement(modalFooter);
		
		// guzik
        Button okButton = new Button("btn btn-primary");
        modalFooter.addHtmlElement(okButton);
        
        okButton.setId("okId");
        okButton.setName("ok");
        okButton.setDataDismiss("modal");
        okButton.setAriaHidden("true");
        okButton.setButtonType(ButtonType.BUTTON);
        
        okButton.addHtmlElement(new Text(messageSource.getMessage("start.info.dialog.ok", new String[] { }, null)));

		mainDiv.addHtmlElement(startInfoDialogDiv);
				
		return mainDiv;		
	}
}
