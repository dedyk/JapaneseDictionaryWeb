package pl.idedyk.japanese.dictionary.web.mail;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import pl.idedyk.japanese.dictionary.web.mysql.model.DailyReportSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GeneralExceptionLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.SuggestionSendLog;

public class MailSender {
	
	@Autowired
	private MessageSource messageSource;

	private String smtpHost;

	private String smtpFrom;

	private String smtpTo;
	
	public void sendSuggestion(GenericLog genericLog, SuggestionSendLog suggestionSendLog) throws MessagingException {
		
		String subject = messageSource.getMessage("mailSender.suggestion.template.subject", new Object[] { suggestionSendLog.getTitle() }, Locale.getDefault());
		
		String body = messageSource.getMessage("mailSender.suggestion.template.body", new Object[] {
				suggestionSendLog.getGenericLogId(),
				suggestionSendLog.getSender(),
				genericLog.getRemoteHost(),
				genericLog.getRemoteIp(),
				suggestionSendLog.getTitle(),
				suggestionSendLog.getBody() }, Locale.getDefault());
		
		sendMail(subject, body, false);
	}
	
	public void sendDailyReport(DailyReportSendLog dailyReportSendLog) throws MessagingException {
		
		sendMail(dailyReportSendLog.getTitle(), dailyReportSendLog.getReport(), true);
	}
	
	public void sendGeneralExceptionLog(GenericLog genericLog, GeneralExceptionLog generalExceptionLog) throws MessagingException {
		
		String subject = messageSource.getMessage("mailSender.generalException.template.subject", new Object[] { }, Locale.getDefault());
		
		String body = messageSource.getMessage("mailSender.generalException.template.body", new Object[] {
				generalExceptionLog.getId(), genericLog.getId(), genericLog.getRequestURL(), generalExceptionLog.getStatusCode(), generalExceptionLog.getException()
				
		}, Locale.getDefault());
		
		sendMail(subject, body, false);		
	}
	
	public void sendStartAppInfo(GenericLog genericLog) throws MessagingException {
		
		String subject = messageSource.getMessage("mailSender.start.app.subject", new Object[] { }, Locale.getDefault());
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String body = messageSource.getMessage("mailSender.start.app.body", new Object[] {
				genericLog.getId(), simpleDateFormat.format(genericLog.getTimestamp()) 
				
		}, Locale.getDefault());
		
		sendMail(subject, body, false);		
	}

	public void sendMail(String subject, String body, boolean html) throws MessagingException {

		// ustawienie serwera
		Properties properties = new Properties();
		
		properties.put("mail.smtp.host", smtpHost);
		
		// utworzenie sesji wysylacza mail'i
		Session session = Session.getDefaultInstance(properties);

		// utworzenie wiadomosci
		MimeMessage message = new MimeMessage(session);
		
		// ustaw odbiorce i nadawce
		message.setFrom(new InternetAddress(smtpFrom));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(smtpTo));

		// ustaw temat
		message.setSubject(subject);
		
		if (html == false) {

			// ustaw tresc wiadomosci
			message.setText(body);			
		} else {
			
			// ustaw tresc wiadomosci
			message.setText(body, "utf-8", "html");
		}

		// wyslij wiadomosc
		Transport.send(message);		
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getSmtpFrom() {
		return smtpFrom;
	}

	public void setSmtpFrom(String smtpFrom) {
		this.smtpFrom = smtpFrom;
	}

	public String getSmtpTo() {
		return smtpTo;
	}

	public void setSmtpTo(String smtpTo) {
		this.smtpTo = smtpTo;
	}
}
