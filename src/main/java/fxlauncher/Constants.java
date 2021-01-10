package fxlauncher;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Constants {
	
	private static final ResourceBundle bundleMessage;
	
	static {		
		bundleMessage = ResourceBundle.getBundle("Messages");
	}
	
	private Constants() {
	}

	public static String getString(final String pKey) {
		try {
			return bundleMessage.getString(pKey);
		} catch (MissingResourceException e) {
			return String.valueOf('!') + pKey + '!';
		}
	}
}
