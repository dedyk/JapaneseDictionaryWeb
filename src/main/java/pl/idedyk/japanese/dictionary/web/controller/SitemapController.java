package pl.idedyk.japanese.dictionary.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.PageNoFoundExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.ServiceUnavailableExceptionLoggerModel;
import pl.idedyk.japanese.dictionary.web.logger.model.SitemapGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.sitemap.SitemapManager;
import pl.idedyk.japanese.dictionary.web.sitemap.exception.NotInitializedException;

@Controller
public class SitemapController {

	private static final Logger logger = LogManager.getLogger(KanjiDictionaryController.class);
	
	@Autowired
	private SitemapManager sitemapManager;
	
	@Autowired
	private LoggerSender loggerSender;
	
    @RequestMapping(value = "/sitemap.xml", method = RequestMethod.GET)
	public void sitemap(HttpServletRequest request, HttpServletResponse response, HttpSession session, OutputStream outputStream) throws IOException {
	
		logger.info("Generowanie pliku sitemap");
		
		// logowanie
		loggerSender.sendLog(new SitemapGenerateLoggerModel(Utils.createLoggerModelCommon(request)));

		response.setContentType("application/xml");
		
		File sitemapFile = null;
		
		try {
			sitemapFile = sitemapManager.getIndexSitemap();
						
		} catch (NotInitializedException e) {
			
			response.sendError(503);
			
			ServiceUnavailableExceptionLoggerModel serviceUnavailableExceptionLoggerModel = new ServiceUnavailableExceptionLoggerModel(Utils.createLoggerModelCommon(request));
			
			loggerSender.sendLog(serviceUnavailableExceptionLoggerModel);
			
			return;			
		}		
		
		if (sitemapFile != null) {
			
			FileInputStream sitemapFileInputStream = null;
			
			try {
				sitemapFileInputStream = new FileInputStream(sitemapFile);
				
				copyStream(sitemapFileInputStream, outputStream);
				
			} finally {
				
				if (sitemapFileInputStream != null) {
					sitemapFileInputStream.close();
				}			
			}
			
		} else {
			response.sendError(404);
			
			PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
			
			loggerSender.sendLog(pageNoFoundExceptionLoggerModel);
		}
	}

    @RequestMapping(value = "/sitemap/{name}/{id}", method = RequestMethod.GET)
	public void sitemap(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("name") String name, @PathVariable("id") int id, OutputStream outputStream) throws IOException {
	
		logger.info("Generowanie pliku sitemap(index) dla: " + name + "/" + id);
		
		// logowanie
		loggerSender.sendLog(new SitemapGenerateLoggerModel(Utils.createLoggerModelCommon(request)));
		
		File sitemapFile = null;
				
		try {
			sitemapFile = sitemapManager.getSitemap(name, id);
			
		} catch (NotInitializedException e) {
			
			response.sendError(503);
			
			ServiceUnavailableExceptionLoggerModel serviceUnavailableExceptionLoggerModel = new ServiceUnavailableExceptionLoggerModel(Utils.createLoggerModelCommon(request));
			
			loggerSender.sendLog(serviceUnavailableExceptionLoggerModel);
			
			return;			
		}
		
		if (sitemapFile != null) {
			
			response.setContentType("application/xml");
			
			FileInputStream sitemapFileInputStream = null;
			
			try {
				sitemapFileInputStream = new FileInputStream(sitemapFile);
				
				copyStream(sitemapFileInputStream, outputStream);
				
			} finally {
				
				if (sitemapFileInputStream != null) {
					sitemapFileInputStream.close();
				}			
			}
			
		} else {			
			response.sendError(404);
			
			PageNoFoundExceptionLoggerModel pageNoFoundExceptionLoggerModel = new PageNoFoundExceptionLoggerModel(Utils.createLoggerModelCommon(request));
			
			loggerSender.sendLog(pageNoFoundExceptionLoggerModel);
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
