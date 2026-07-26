package pl.idedyk.japanese.dictionary.web.controller.model;

public class CaptchaModel {
	private String userCaptcha;

	private String captchaBase64Image;
	
	public String getUserCaptcha() {
		return userCaptcha;
	}

	public void setUserCaptcha(String userCaptcha) {
		this.userCaptcha = userCaptcha;
	}

	public String getCaptchaBase64Image() {
		return captchaBase64Image;
	}

	public void setCaptchaBase64Image(String captchaBase64Image) {
		this.captchaBase64Image = captchaBase64Image;
	}
}
