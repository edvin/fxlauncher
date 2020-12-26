package fxlauncher.config;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.UnaryOperator;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import fxlauncher.old.Launcher;

public class LoggerConfig {

	/**
	 * Set the local logfile based on {@link LauncherOption} {@code LOG_FILE}
	 *
	 * @param logfile the file that log messages should be sent to
	 */
	public static void initLocalLogging(String logfile) {
		Logger appRootLog = Logger.getLogger(Launcher.class.getPackage().getName());
		Logger log = Logger.getLogger(LoggerConfig.class.getName());

		FileHandler handler;
		log.info("Attempting to begin logging to " + logfile);
		try {
			handler = new FileHandler(logfile);
			handler.setFormatter(new SimpleFormatter());
			appRootLog.addHandler(handler);
			appRootLog.info("now logging to " + logfile);
		} catch (SecurityException | IOException e) {
			log.warning(CREATE_HANDLER_FAILED_MSG.apply(logfile));
			e.printStackTrace();
		}
	}

	private final static UnaryOperator<String> CREATE_HANDLER_FAILED_MSG = logfile -> String.format(
			"Unable to log to '%s'. Logging to default location instead: %s", logfile, Paths.get(".").toAbsolutePath());
}
