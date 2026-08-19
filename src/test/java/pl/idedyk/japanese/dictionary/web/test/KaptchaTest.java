package pl.idedyk.japanese.dictionary.web.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

public class KaptchaTest {

	public static void main(String[] args) throws Exception {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        
        Properties properties = new Properties();
        
        // properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "640");
        // properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "480");
        
        properties.setProperty("kaptcha.image.width", "250");
        properties.setProperty("kaptcha.image.height", "50");
        properties.setProperty("kaptcha.textproducer.char.length", "8");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        // properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
        
        Config config = new Config(properties);
        
        kaptcha.setConfig(config);
        
        // String randomText = "Fryderyk";
        
        BufferedImage image = kaptcha.createImage(kaptcha.createText()); // createText(); // createImage(randomText);

        ImageIO.setUseCache(false);
        ImageIO.write(image, "png", new File("/tmp/a/test.png"));
        
        // String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

        //return new CaptchaResponse("data:image/png;base64," + base64Image);
	}

}
