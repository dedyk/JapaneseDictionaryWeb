package pl.idedyk.japanese.dictionary.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.SitemapGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.sitemap.SitemapManager;

@Controller
public class SitemapController {

	private static final Logger logger = Logger.getLogger(KanjiDictionaryController.class);
	
	@Autowired
	private SitemapManager sitemapManager;
	
	@Autowired
	private LoggerSender loggerSender;
	
    @RequestMapping(value = "/sitemap.xml", method = RequestMethod.GET)
	public void sitemap(HttpServletRequest request, HttpServletResponse response, HttpSession session, OutputStream outputStream) throws Exception {
	
		logger.info("Generowanie pliku sitemap");
		
		// logowanie
		loggerSender.sendLog(new SitemapGenerateLoggerModel(Utils.createLoggerModelCommon(request)));

		response.setContentType("application/xml");
		
		File sitemapFile = sitemapManager.getSitemap();
		
		FileInputStream sitemapFileInputStream = null;
		
		try {
			sitemapFileInputStream = new FileInputStream(sitemapFile);
			
			copyStream(sitemapFileInputStream, outputStream);
			
		} finally {
			
			if (sitemapFileInputStream != null) {
				sitemapFileInputStream.close();
			}			
		}
	}
    
	private void copyStream(InputStream input, OutputStream output) throws IOException {
		
		byte[] buffer = new byte[1024]; // Adjust if you want
		
		int bytesRead;
		
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
}
