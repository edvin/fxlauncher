package fxlauncher.except;

/**
 * Unchecked exception. Implementation handles the specific case where some configurable
 * parameter was passed a value that did not validate
 */
public class FxLauncherValidationException extends FxLauncherException {

	private static final long serialVersionUID = 1L;

	public FxLauncherValidationException() {
		super();
	}

	public FxLauncherValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FxLauncherValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FxLauncherValidationException(String message) {
		super(message);
	}

	public FxLauncherValidationException(Throwable cause) {
		super(cause);
	}

	
}
