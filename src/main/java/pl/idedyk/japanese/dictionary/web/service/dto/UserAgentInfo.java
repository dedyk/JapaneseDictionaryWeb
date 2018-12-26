package pl.idedyk.japanese.dictionary.web.service.dto;

public class UserAgentInfo {
	
	private Type type;
	
	private DesktopInfo desktopInfo;
	
	private PhoneTabletInfo phoneTabletInfo;
	
	private JapaneseAndroidLearnerHelperInfo japaneseAndroidLearnerHelperInfo;
	
	private RobotInfo robotInfo;
	
	private OtherInfo otherInfo;
	
	//
	
	public UserAgentInfo(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public DesktopInfo getDesktopInfo() {
		return desktopInfo;
	}

	public void setDesktopInfo(DesktopInfo desktopInfo) {
		this.desktopInfo = desktopInfo;
	}

	public PhoneTabletInfo getPhoneTabletInfo() {
		return phoneTabletInfo;
	}

	public void setPhoneTabletInfo(PhoneTabletInfo phoneTabletInfo) {
		this.phoneTabletInfo = phoneTabletInfo;
	}

	public JapaneseAndroidLearnerHelperInfo getJapaneseAndroidLearnerHelperInfo() {
		return japaneseAndroidLearnerHelperInfo;
	}

	public void setJapaneseAndroidLearnerHelperInfo(JapaneseAndroidLearnerHelperInfo japaneseAndroidLearnerHelperInfo) {
		this.japaneseAndroidLearnerHelperInfo = japaneseAndroidLearnerHelperInfo;
	}

	public RobotInfo getRobotInfo() {
		return robotInfo;
	}

	public void setRobotInfo(RobotInfo robotInfo) {
		this.robotInfo = robotInfo;
	}

	public OtherInfo getOtherInfo() {
		return otherInfo;
	}

	public void setOtherInfo(OtherInfo otherInfo) {
		this.otherInfo = otherInfo;
	}

	//

	public static enum Type {
		
		JAPANESE_ANDROID_LEARNER_HELPER,
		
		DESKTOP,
		
		PHONE,
		
		TABLET,
		
		ROBOT,
		
		OTHER,
		
		NULL;
	}
	
	public static class JapaneseAndroidLearnerHelperInfo {
		
		private int code;
		
		private String codeName;

		public JapaneseAndroidLearnerHelperInfo(int code, String codeName) {
			this.code = code;
			this.codeName = codeName;
		}

		public int getCode() {
			return code;
		}

		public String getCodeName() {
			return codeName;
		}
	}
	
	public static class DesktopInfo {
		
		private String desktopType;
		
		private String operationSystem;
		
		private String browserType;
		
		public DesktopInfo(String desktopType, String operationSystem, String browserType) {
			this.desktopType = desktopType;
			this.operationSystem = operationSystem;
			this.browserType = browserType;
		}

		public String getDesktopType() {
			return desktopType;
		}

		public String getOperationSystem() {
			return operationSystem;
		}

		public String getBrowserType() {
			return browserType;
		}
	}
	
	public static class PhoneTabletInfo {
		
		private String deviceName;
		
		private String operationSystem;
		
		private String browserType;
		
		public PhoneTabletInfo(String deviceName, String operationSystem, String browserType) {
			this.deviceName = deviceName;
			this.operationSystem = operationSystem;
			this.browserType = browserType;
		}

		public String getDeviceName() {
			return deviceName;
		}

		public String getOperationSystem() {
			return operationSystem;
		}

		public String getBrowserType() {
			return browserType;
		}		
	}
	
	public static class RobotInfo {
		
		private String robotName;
		
		private String robotUrl;

		public RobotInfo(String robotName, String robotUrl) {
			this.robotName = robotName;
			this.robotUrl = robotUrl;
		}

		public String getRobotName() {
			return robotName;
		}

		public void setRobotName(String robotName) {
			this.robotName = robotName;
		}

		public String getRobotUrl() {
			return robotUrl;
		}

		public void setRobotUrl(String robotUrl) {
			this.robotUrl = robotUrl;
		}		
	}
	
	public static class OtherInfo {
		
		private String userAgent;

		public OtherInfo(String userAgent) {
			this.userAgent = userAgent;
		}

		public String getUserAgent() {
			return userAgent;
		}
	}
}
