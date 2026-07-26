package pl.idedyk.japanese.dictionary.web.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pl.idedyk.japanese.dictionary.web.common.Utils;
import pl.idedyk.japanese.dictionary.web.controller.model.CaptchaModel;
import pl.idedyk.japanese.dictionary.web.logger.LoggerSender;
import pl.idedyk.japanese.dictionary.web.logger.model.CatchaStartLoggerModel;

@Controller
public class CaptchaController {
	
	public static final String CAPTCHA_URL_PREFIX = "/captcha/";
	public static final String CAPTCHA_URL_START = CAPTCHA_URL_PREFIX + "start";
	
	public static final String CAPTCHA_SESSION_CORRECT_TEXT = "captchaCorrectText";
	
	@Autowired
	protected LoggerSender loggerSender;
	
	private static final Logger logger = LogManager.getLogger(CaptchaController.class);
	
	@RequestMapping(value = CAPTCHA_URL_START, method = RequestMethod.GET)
	public String start(HttpServletRequest request, HttpServletResponse response, HttpSession session, Map<String, Object> model) throws IOException {

		logger.info("Start captcha");
		
		// utworzenie model weryfikacji Captcha
		CaptchaModel captchaModel = new CaptchaModel();

		// wygenerowanie wykrzywiacza
		String[] captchaData = generateCaptchImageAndEncodeAsBase64();
		
		// zapis poprawnego kodu captcha do sesji
		session.setAttribute(CAPTCHA_SESSION_CORRECT_TEXT, captchaData[0]);
		
		// zapis obrazka do modelu
		captchaModel.setCaptchaBase64Image(captchaData[1]);		
		
		// logowanie dla loggera
		CatchaStartLoggerModel redirectToCatchaLoggerModel = new CatchaStartLoggerModel(Utils.createLoggerModelCommon(request));
		
		loggerSender.sendLog(redirectToCatchaLoggerModel);
		
		// wypelnienie modelu z danymi formularza
		model.put("command", captchaModel);
		
		return "captcha";
	}
	
	private String[] generateCaptchImageAndEncodeAsBase64() throws IOException {
		
		// inicjacja generowania wykrzywiacza
		DefaultKaptcha kaptcha = new DefaultKaptcha();
		
		Properties properties = new Properties();
		
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "250");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "50");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "8");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, "black");
                
        kaptcha.setConfig(new Config(properties));

        // wygenerowanie literek
        String text = kaptcha.createText();
        
        // generacja wykrzywiacza
        BufferedImage image = kaptcha.createImage(text);
        
        // zawartosc obrazka
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        ImageIO.setUseCache(false);
        ImageIO.write(image, "png", byteArrayOutputStream);
        
        return new String[] { text, "data:image/png;base64," + Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()) };		
	}
}
