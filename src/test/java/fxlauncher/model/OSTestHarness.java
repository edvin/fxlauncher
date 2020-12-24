package fxlauncher.model;

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

	/*
	 * initializer causes intervention in System environment variables to occur
	 * before OS.class is loaded (And therefore before any of its member objects are
	 * instantiated.
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

	@BeforeAll
	public static void init() throws ReflectiveOperationException {
		expectedAllUsersPathMap.put(OS.MAC, Paths.get("/Library/Application Support"));
		expectedAllUsersPathMap.put(OS.WIN, Paths.get(ALLUSERS_ENVVAR_VAL));
		expectedAllUsersPathMap.put(OS.LINUX, Paths.get("/usr/local/share"));
		expectedAllUsersPathMap.put(OS.OTHER, Paths.get("/usr/local/share"));
	}

}
