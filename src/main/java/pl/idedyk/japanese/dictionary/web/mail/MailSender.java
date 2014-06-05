package pl.idedyk.japanese.dictionary.web.mail;

import java.util.Locale;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import pl.idedyk.japanese.dictionary.web.mysql.model.DailyReportSendLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.GenericLog;
import pl.idedyk.japanese.dictionary.web.mysql.model.SuggestionSendLog;

public class MailSender {

	private static final Logger logger = Logger.getLogger(MailSender.class);
	
	@Autowired
	private MessageSource messageSource;

	private String smtpHost;

	private String smtpFrom;

	private String smtpTo;
	
	public void sendSuggestion(GenericLog genericLog, SuggestionSendLog suggestionSendLog) {
		
		String subject = messageSource.getMessage("mailSender.suggestion.template.subject", new Object[] { suggestionSendLog.getTitle() }, Locale.getDefault());
		
		String body = messageSource.getMessage("mailSender.suggestion.template.body", new Object[] {
				suggestionSendLog.getGenericLogId(),
				suggestionSendLog.getSender(),
				genericLog.getRemoteHost(),
				genericLog.getRemoteIp(),
				suggestionSendLog.getTitle(),
				suggestionSendLog.getBody() }, Locale.getDefault());
		
		sendMail(subject, body);
	}
	
	public void sendDailyReport(DailyReportSendLog dailyReportSendLog) {
		
		sendMail(dailyReportSendLog.getTitle(), dailyReportSendLog.getReport());
	}

	public void sendMail(String subject, String body) {

		try {
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

			// ustaw tresc wiadomosci
			message.setText(body);

			// wyslij wiadomosc
			Transport.send(message);

		} catch (MessagingException e) {
			logger.error("Błąd wysyłki wiadomości", e);
		}
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
