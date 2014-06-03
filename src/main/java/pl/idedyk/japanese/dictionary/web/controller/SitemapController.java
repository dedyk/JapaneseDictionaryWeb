package pl.idedyk.japanese.dictionary.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.SitemapGenerateLoggerModel;
import pl.idedyk.japanese.dictionary.web.sitemap.SitemapManager;
import pl.idedyk.japanese.dictionary.web.sitemap.model.Urlset;

@Controller
public class SitemapController extends CommonController {

	private static final Logger logger = Logger.getLogger(KanjiDictionaryController.class);
	
	@Autowired
	private SitemapManager sitemapManager;
	
	@Autowired
	private LoggerSender loggerSender;
	
    @RequestMapping(value = "/sitemap.xml", method = RequestMethod.GET)
    @ResponseBody
	public Urlset sitemap(HttpServletRequest request, HttpSession session) {
	
		logger.info("Generowanie pliku sitemap");
		
		// logowanie
		loggerSender.sendLog(new SitemapGenerateLoggerModel(session.getId(), Utils.getRemoteIp(request), request.getHeader("User-Agent")));

		return sitemapManager.getSitemap(request.getContextPath());
	}
}
