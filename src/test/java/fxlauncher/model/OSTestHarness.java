package fxlauncher.model;

import static fxlauncher.model.OS.LINUX;
import static fxlauncher.model.OS.MAC;
import static fxlauncher.model.OS.OTHER;
import static fxlauncher.model.OS.WIN;
import static java.util.logging.Logger.getLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;

import fxlauncher.testutils.ReflectionTools;

public class OSTestHarness {

	private static final Logger log = getLogger(OSTest.class.getName());

	private static final String ALLUSERS_ENVVAR_KEY = "ALLUSERSPROFILE";
	private static final String ALLUSERS_ENVVAR_VAL = "TEST_ALL_USERS_PROFILE";
	private static final String HOME_PATH = System.getProperty("user.home");

	/*
	 * initializer causes intervention in environment variables to occur before
	 * OS.class is loaded (And therefore before any of its member objects are
	 * instantiated and initialized).
	 */
	static {
		try {
			ReflectionTools.updateEnv(ALLUSERS_ENVVAR_KEY, ALLUSERS_ENVVAR_VAL);
		} catch (ReflectiveOperationException e) {
			log.warning(
					"Failed to override value of system environment variable 'ALLUSERSPROFILE' in memory-resident map. Expect 'WIN' test case for 'ALLUSERS' test to fail");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("serial")
	protected static final Map<OS, Path> expectedAllUsersPathMap = new EnumMap<OS, Path>(OS.class) {
	};

	@SuppressWarnings("serial")
	protected static final Map<OS, Path> expectedUserLibPathMap = new EnumMap<OS, Path>(OS.class) {
	};

	@BeforeAll
	public static void init() throws ReflectiveOperationException {
		expectedAllUsersPathMap.put(MAC, Paths.get("/Library/Application Support"));
		expectedAllUsersPathMap.put(WIN, Paths.get(ALLUSERS_ENVVAR_VAL));
		expectedAllUsersPathMap.put(LINUX, Paths.get("/usr/local/share"));
		expectedAllUsersPathMap.put(OTHER, Paths.get("/usr/local/share"));

		expectedUserLibPathMap.put(MAC, Paths.get(HOME_PATH).resolve("Library").resolve("Application Support"));
		expectedUserLibPathMap.put(WIN, Paths.get(HOME_PATH).resolve("AppData").resolve("Local"));
		expectedUserLibPathMap.put(LINUX, Paths.get(HOME_PATH));
		expectedUserLibPathMap.put(OTHER, Paths.get(HOME_PATH));
	}

}
