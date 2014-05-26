package pl.idedyk.japanese.dictionary.web.taglib;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.context.MessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import pl.idedyk.japanese.dictionary.web.html.Button;
import pl.idedyk.japanese.dictionary.web.html.Div;
import pl.idedyk.japanese.dictionary.web.html.FieldSet;
import pl.idedyk.japanese.dictionary.web.html.Form;
import pl.idedyk.japanese.dictionary.web.html.H;
import pl.idedyk.japanese.dictionary.web.html.Input;
import pl.idedyk.japanese.dictionary.web.html.Input.InputType;
import pl.idedyk.japanese.dictionary.web.html.Label;
import pl.idedyk.japanese.dictionary.web.html.Legend;
import pl.idedyk.japanese.dictionary.web.html.Text;
import pl.idedyk.japanese.dictionary.web.html.TextArea;

public class SuggestionTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	
	private MessageSource messageSource;
	
	@Override
	public int doStartTag() throws JspException {
		
		// TODO
		// temat
		// pole do wpisywania tresci
		// adres nadawcy
		// guzik ok
		// tytul

		ServletContext servletContext = pageContext.getServletContext();
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		
		this.messageSource = (MessageSource)webApplicationContext.getBean("messageSource");
		
		try {
            JspWriter out = pageContext.getOut();
            
            Div mainDiv = new Div("col-md-12");
            
            // tytul
            H titleH4 = new H(4);
            mainDiv.addHtmlElement(titleH4);
            
            // formularz
            Form form = new Form("form-horizontal");
            mainDiv.addHtmlElement(form);
            
            // zbior pol formularza
            FieldSet fielSet = new FieldSet();
            form.addHtmlElement(fielSet);
            
            // tytul
            Legend legendTitle = new Legend();
            fielSet.addHtmlElement(legendTitle);
            
            legendTitle.addHtmlElement(new Text(getMessage("suggestion.tag.title")));
            
            // temat
            Div titleDiv = new Div("form-group");
            fielSet.addHtmlElement(titleDiv);
            
            Label titleLabel = new Label("col-md-4 control-label");
            titleDiv.addHtmlElement(titleLabel);
            
            titleLabel.setFor("title");
            
            titleLabel.addHtmlElement(new Text(getMessage("suggestion.tag.suggestion.title")));
            
            Div titleInputDiv = new Div("col-md-5");
            titleDiv.addHtmlElement(titleInputDiv);
            
            Input titleInput = new Input("form-control input-md");
            titleInputDiv.addHtmlElement(titleInput);
            
            titleInput.setId("title");
            titleInput.setName("title");
            titleInput.setPlaceholder(getMessage("suggestion.tag.suggestion.title.input.placeholder"));
            titleInput.setRequired("");
            titleInput.setType(InputType.TEXT);
            
            // nadawca
            Div senderDiv = new Div("form-group");
            fielSet.addHtmlElement(senderDiv);
            
            Label senderLabel = new Label("col-md-4 control-label");
            senderDiv.addHtmlElement(senderLabel);
            
            senderLabel.setFor("sender");
            
            senderLabel.addHtmlElement(new Text(getMessage("suggestion.tag.suggestion.sender")));
            
            Div senderInputDiv = new Div("col-md-5");
            senderDiv.addHtmlElement(senderInputDiv);
            
            Input senderInput = new Input("form-control input-md");
            senderInputDiv.addHtmlElement(senderInput);
            
            senderInput.setId("sender");
            senderInput.setName("sender");
            senderInput.setPlaceholder(getMessage("suggestion.tag.suggestion.sender.input.placeholder"));
            senderInput.setRequired("");
            senderInput.setType(InputType.EMAIL);
            
            // tresc sugestii
            
            Div bodyDiv = new Div("form-group");
            fielSet.addHtmlElement(bodyDiv);
            
            Label bodyLabel = new Label("col-md-4 control-label");
            bodyDiv.addHtmlElement(bodyLabel);
            
            bodyLabel.setFor("sender");
            
            bodyLabel.addHtmlElement(new Text(getMessage("suggestion.tag.suggestion.body")));
            
            Div bodyTextAreaDiv = new Div("col-md-5");
            bodyDiv.addHtmlElement(bodyTextAreaDiv);
            
            TextArea bodyTextArea = new TextArea("form-control");
            bodyTextAreaDiv.addHtmlElement(bodyTextArea);
            
            bodyTextArea.setId("body");
            bodyTextArea.setName("body");
            bodyTextArea.setRows(6);

            // wyslij
            Div sendDiv = new Div("form-group");
            fielSet.addHtmlElement(sendDiv);
            
            Label sendLabel = new Label("col-md-4 control-label");
            sendDiv.addHtmlElement(sendLabel);
                            
            sendLabel.addHtmlElement(new Text(""));
            
            Div sendButtonDiv = new Div("col-md-5");
            sendDiv.addHtmlElement(sendButtonDiv);
            
            Button sendButton = new Button("btn btn-primary");
            sendButtonDiv.addHtmlElement(sendButton);
            
            sendButton.setId("send");
            sendButton.setName("send");
            
            sendButton.addHtmlElement(new Text(getMessage("suggestion.tag.suggestion.send")));
            
            // renderowanie tresci
            mainDiv.render(out);
                        
            return SKIP_BODY;
 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }		
	}
	
	private String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.getDefault());
	}
}
