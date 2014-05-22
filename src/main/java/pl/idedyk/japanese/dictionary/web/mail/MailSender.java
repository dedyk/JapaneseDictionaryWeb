package pl.idedyk.japanese.dictionary.web.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class MailSender {

	private static final Logger logger = Logger.getLogger(MailSender.class);

	private String smtpHost;

	private String smtpFrom;

	private String smtpTo;

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
