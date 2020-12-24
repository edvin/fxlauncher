package fxlauncher.model;

import static fxlauncher.model.GenericPathLabel.ALLUSERS;
import static fxlauncher.model.GenericPathLabel.USERLIB;
import static java.util.logging.Logger.getLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;


/**
 * Represents an operating-system supported by FxLauncher
 * Also provides resolution of OS-specific generic paths USERLIB and ALLUSERS
 * @author idavis1
 *
 */
public enum OS {
	WIN,
	MAC,
	LINUX,
	OTHER,
	;
	
	private final static Logger log = getLogger(OS.class.getName());
	public static final OS current;
	
	private final Path home = Paths.get(System.getProperty("user.home"));
	private final Map<GenericPathLabel, Path> pathMap = new EnumMap<GenericPathLabel, Path>(GenericPathLabel.class);
	
	private OS() {
		Path userLibPath;
		Path allUsersPath;
		
		switch (this.name().toLowerCase()) {
			case "mac":
				userLibPath = home.resolve("Library").resolve("Application Support");
				allUsersPath = Paths.get("/Library/Application Support");
				break;
			case "win":
				userLibPath = home.resolve("AppData").resolve("Local");
				allUsersPath = Paths.get(Optional.ofNullable(System.getenv("ALLUSERSPROFILE")).orElse(""));
				break;
			default:
				userLibPath = home;
				allUsersPath = Paths.get("/usr/local/share");
				break;
		}
		pathMap.put(USERLIB, userLibPath);
		pathMap.put(ALLUSERS, allUsersPath);
	}
	
	/**
	 * Fetch the generic path for a supported sentinel value and the OS represented by this instance
	 * @param label the sentinel value, as an enum
	 * @return the {@link Path} object for the provided sentinel value.
	 */
	public Path getGenericPath(GenericPathLabel label) {
		return pathMap.get(label);
	}
	
	// initializer runs at class-load time
	static {
		String os = System.getProperty("os.name", "generic").toLowerCase();
		
		if ((os.contains("mac")) || (os.contains("darwin"))) current = MAC;
		else if (os.contains("win")) current = WIN;
		else if (os.contains("nux")) current = LINUX;
		else current = OTHER;
		log.finer(String.format("Current operating system is: %s", MAC));
	}
}
