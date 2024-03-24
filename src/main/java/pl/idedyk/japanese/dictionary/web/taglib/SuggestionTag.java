package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.TagSupport;
import pl.idedyk.japanese.dictionary.web.common.LinkGenerator;
import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Button.ButtonType;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.FieldSet;
import pl.idedyk.japanese.dictionary.web.html.Form;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Input;
import pl.idedyk.japanese.dictionary.web.html.Input.InputType;
import pl.idedyk.japanese.dictionary.web.html.Label;
import pl.idedyk.japanese.dictionary.web.html.Legend;
import pl.idedyk.japanese.dictionary.web.html.Script;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.TextArea;

public class SuggestionTag extends TagSupport {

	private static final long serialVersionUID = 1L;
		
	@Override
	public int doStartTag() throws JspException {
		
		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		MessageSource messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		
		try {
            JspWriter out = pageContext.getOut();
            
            Div mainDiv = generateBody(pageContext, messageSource, null, null);
            
            // renderowanie tresci
            mainDiv.render(out);
                        
            return SKIP_BODY;
 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }		
	}
	
	public static Div generateBody(PageContext pageContext, MessageSource messageSource, String dialogId, String defaultTitle) {
		
        Div mainDiv = new Div(dialogId == null ? "col-md-12" : "");
        
        if (dialogId == null) {        
	        // tytul
	        H titleH4 = new H(4);
	        mainDiv.addHtmlElement(titleH4);
        }
        
        // formularz
        Form form = new Form("form-horizontal");
        mainDiv.addHtmlElement(form);
        
        form.setId("suggestionFormId");
        
        // zbior pol formularza
        FieldSet fielSet = new FieldSet();
        form.addHtmlElement(fielSet);
        
        if (dialogId == null) {
            // tytul
            Legend legendTitle = new Legend();
            fielSet.addHtmlElement(legendTitle);
            
            legendTitle.addHtmlElement(new Text(getMessage(messageSource, "suggestion.tag.title")));        	
        }
        
        // temat
        Div titleDiv = new Div("form-group");
        fielSet.addHtmlElement(titleDiv);
        
        Label titleLabel = new Label("col-md-4 control-label");
        titleDiv.addHtmlElement(titleLabel);
        
        titleLabel.setFor("title");
        
        titleLabel.addHtmlElement(new Text(getMessage(messageSource, "suggestion.tag.suggestion.title")));
        
        Div titleInputDiv = new Div("col-md-5");
        titleDiv.addHtmlElement(titleInputDiv);
        
        Input titleInput = new Input("form-control input-md");
        titleInputDiv.addHtmlElement(titleInput);
        
        titleInput.setId("titleId");
        titleInput.setName("title");
        titleInput.setPlaceholder(getMessage(messageSource, "suggestion.tag.suggestion.title.input.placeholder"));
        
        if (defaultTitle != null) {
        	titleInput.setValue(defaultTitle);
        }
        
        titleInput.setType(InputType.TEXT);
        
        // nadawca
        Div senderDiv = new Div("form-group");
        fielSet.addHtmlElement(senderDiv);
        
        Label senderLabel = new Label("col-md-4 control-label");
        senderDiv.addHtmlElement(senderLabel);
        
        senderLabel.setFor("sender");
        
        senderLabel.addHtmlElement(new Text(getMessage(messageSource, "suggestion.tag.suggestion.sender")));
        
        Div senderInputDiv = new Div("col-md-5");
        senderDiv.addHtmlElement(senderInputDiv);
        
        Input senderInput = new Input("form-control input-md");
        senderInputDiv.addHtmlElement(senderInput);
        
        senderInput.setId("senderId");
        senderInput.setName("sender");
        senderInput.setPlaceholder(getMessage(messageSource, "suggestion.tag.suggestion.sender.input.placeholder"));
        senderInput.setValue("");
        senderInput.setType(InputType.EMAIL);
        
        // tresc sugestii
        
        Div bodyDiv = new Div("form-group");
        fielSet.addHtmlElement(bodyDiv);
        
        Label bodyLabel = new Label("col-md-4 control-label");
        bodyDiv.addHtmlElement(bodyLabel);
        
        bodyLabel.setFor("sender");
        
        bodyLabel.addHtmlElement(new Text(getMessage(messageSource, "suggestion.tag.suggestion.body")));
        
        Div bodyTextAreaDiv = new Div("col-md-5");
        bodyDiv.addHtmlElement(bodyTextAreaDiv);
        
        TextArea bodyTextArea = new TextArea("form-control");
        bodyTextAreaDiv.addHtmlElement(bodyTextArea);
        
        bodyTextArea.setId("bodyId");
        bodyTextArea.setName("body");
        bodyTextArea.setRows(6);

        // wyslij
        Div sendDiv = new Div("form-group");
        fielSet.addHtmlElement(sendDiv);
        
        Label sendLabel = new Label("col-md-4 control-label");
        sendDiv.addHtmlElement(sendLabel);
                        
        sendLabel.addHtmlElement(new Text(""));
        
        Div buttonsDiv = new Div("col-md-5");
        sendDiv.addHtmlElement(buttonsDiv);
        
        if (dialogId != null) {
			// przycisk zamkniecia 2
			Button closeButton2 = new Button("btn btn-default", "margin-right: 50px");
			
			closeButton2.setButtonType(ButtonType.BUTTON);
			closeButton2.setDataDismiss("modal");
			
			closeButton2.addHtmlElement(new Text(getMessage(messageSource, "suggestion.tag.suggestion.dialog.cancel")));
			
			buttonsDiv.addHtmlElement(closeButton2);
        }
        
        Button sendButton = new Button("btn btn-primary");
        buttonsDiv.addHtmlElement(sendButton);
        
        sendButton.setId("sendId");
        sendButton.setName("send");
        sendButton.setButtonType(ButtonType.SUBMIT);
        
        sendButton.addHtmlElement(new Text(getMessage(messageSource, "suggestion.tag.suggestion.send")));
                
        mainDiv.addHtmlElement(generateScript(pageContext, messageSource, dialogId));
        
        return mainDiv;
	}
	
	private static Script generateScript(PageContext pageContext, MessageSource messageSource, String dialogId) {
		
		StringBuffer scriptBody = new StringBuffer();
		
		scriptBody.append("$(document).ready(function() {\n");

		scriptBody.append("    $('#suggestionFormId').validate({\n");
		scriptBody.append("        rules: {\n");
		
		scriptBody.append("            title: {\n");
		scriptBody.append("                minlength: 10,\n");
		scriptBody.append("                required: true\n");
		scriptBody.append("            },\n");

		scriptBody.append("            sender: {\n");
		scriptBody.append("                email: true,\n");
		scriptBody.append("                required: true\n");
		scriptBody.append("            },\n");

		scriptBody.append("            body: {\n");
		scriptBody.append("                minlength: 20,\n");
		scriptBody.append("                required: true\n");
		scriptBody.append("            },\n");
		
		scriptBody.append("        },\n");
		scriptBody.append("        submitHandler: function(form) {\n");
		
		scriptBody.append("			$.ajax({\n");
											
		scriptBody.append("				url: \"" + LinkGenerator.generateSendSuggestionLink(pageContext.getServletContext().getContextPath()) + "\",\n");
												
		scriptBody.append("				data: {\n");
		scriptBody.append("					suggestion: { title : $( '#titleId' ).val(), sender : $( '#senderId' ).val(), body : $( '#bodyId' ).val() } \n");
		scriptBody.append("				},\n");
											
		scriptBody.append("				type: \"POST\",\n");
										
		scriptBody.append("				dataType : \"json\",\n");
										
		scriptBody.append("				success: function( json ) {\n");
		scriptBody.append("					$( '#titleId' ).val('');\n");
		scriptBody.append("					$( '#senderId' ).val('');\n");
		scriptBody.append("					$( '#bodyId' ).val('');\n\n");
		scriptBody.append("					alert('" + getMessage(messageSource, "suggestion.tag.suggestion.send.ok") + "');\n");
		scriptBody.append("				},\n");
										
		scriptBody.append("				error: function( xhr, status, errorThrown ) {\n");
		scriptBody.append("					alert('" + getMessage(messageSource, "suggestion.tag.suggestion.send.problem") + "');\n");
		scriptBody.append("				},\n");
										
		scriptBody.append("				complete: function( xhr, status ) {\n");
		
		if (dialogId != null) {
			scriptBody.append("					$( '#" + dialogId + "' ).modal('hide');\n");
		}
		
		scriptBody.append("				}\n");
		scriptBody.append("			});\n");
		
		scriptBody.append("         return false;\n");
		scriptBody.append("        }\n");
		scriptBody.append("    });\n");
		scriptBody.append("});\n");
		
		Script script = new Script();
		
		script.addHtmlElement(new Text(scriptBody.toString()));
		
		return script;
	}
	
	private static String getMessage(MessageSource messageSource, String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}
}
