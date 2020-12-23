package fxlauncher.except;

/**
 * Abstract RuntimeException that should be the root of all RuntimeExceptions
 * that are thrown during execution of FxLauncher.
 * @author idavis1
 */
public abstract class FxLauncherException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FxLauncherException() {
		super();
	}

	public FxLauncherException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FxLauncherException(String message, Throwable cause) {
		super(message, cause);
	}

	public FxLauncherException(String message) {
		super(message);
	}

	public FxLauncherException(Throwable cause) {
		super(cause);
	}
	
}
