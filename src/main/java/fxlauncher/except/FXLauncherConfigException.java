package fxlauncher.except;

/**
 * UncheckedException thrown when a configuration change fails for any reason.
 *
 * @author idavis1
 *
 */
public class FXLauncherConfigException extends FXLauncherException {
	private static final long serialVersionUID = 1L;

	public FXLauncherConfigException() {
		super();
	}

	public FXLauncherConfigException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FXLauncherConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public FXLauncherConfigException(String message) {
		super(message);
	}

	public FXLauncherConfigException(Throwable cause) {
		super(cause);
	}
}
