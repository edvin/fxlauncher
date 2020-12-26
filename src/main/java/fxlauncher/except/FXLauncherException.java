package fxlauncher.except;

/**
 * Abstract RuntimeException that should be the root of all RuntimeExceptions
 * that are thrown during execution of FxLauncher.
 * @author idavis1
 */
public abstract class FXLauncherException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FXLauncherException() {
		super();
	}

	public FXLauncherException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FXLauncherException(String message, Throwable cause) {
		super(message, cause);
	}

	public FXLauncherException(String message) {
		super(message);
	}

	public FXLauncherException(Throwable cause) {
		super(cause);
	}
	
}
