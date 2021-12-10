package pl.idedyk.japanese.dictionary.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.FaviconIconSendLoggerModel;

@Controller
public class FaviconIconController {

	private static final Logger logger = LogManager.getLogger(FaviconIconController.class);

	@Value("${base.server}")
	private String baseServer;

	@Autowired
	private LoggerSender loggerSender;

	@RequestMapping(value = "/favicon.ico", method = RequestMethod.GET)
	public void getFaviconIco(HttpServletRequest request, HttpServletResponse response, HttpSession session, OutputStream outputStream) throws IOException {

		logger.info("Wysyłanie pliku favicon.ico");

		// logowanie
		loggerSender.sendLog(new FaviconIconSendLoggerModel(Utils.createLoggerModelCommon(request)));

		InputStream faviconPngInputStream = FaviconIconController.class.getResourceAsStream("/favicon/favicon.png");

		response.setContentType("image/png");
		
		copyStream(faviconPngInputStream, outputStream);
	}

	private void copyStream(InputStream input, OutputStream output) throws IOException {
		
		byte[] buffer = new byte[1024]; // Adjust if you want
		
		int bytesRead;
		
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
}
