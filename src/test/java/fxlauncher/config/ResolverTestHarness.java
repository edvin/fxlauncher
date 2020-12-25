package fxlauncher.config;

import static fxlauncher.model.OS.LINUX;
import static fxlauncher.model.OS.MAC;
import static fxlauncher.model.OS.OTHER;
import static fxlauncher.model.OS.WIN;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import fxlauncher.model.OS;
import fxlauncher.testutils.ReflectionTools;

public class ResolverTestHarness {

	static {
		try {
			ReflectionTools.updateEnv("ALLUSERSPROFILE", "TEST_ALLUSERS_PROFILE");
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	protected static final Map<OS, String> expectedAllUsersPathMap = new EnumMap<OS, String>(OS.class);
	protected static final Map<OS, String> expectedUserLibPathMap = new EnumMap<OS, String>(OS.class);
	private static final String HOME_PATH = System.getProperty("user.home");
	private static final String SEP = File.separator;

	protected OS startingOS;

	@BeforeEach
	void captureCurrentOS() throws ReflectiveOperationException {
		startingOS = OS.current;
	}

	@AfterEach
	void restoreStartingOS() {
		setCurrentOS(startingOS);
	}

	@BeforeAll
	static void init() {
		expectedAllUsersPathMap.put(MAC, "/Library/Application Support/TEST/PATH");
		expectedAllUsersPathMap.put(WIN, "TEST_ALLUSERS_PROFILE/TEST/PATH");
		expectedAllUsersPathMap.put(LINUX, "/usr/local/share/TEST/PATH");
		expectedAllUsersPathMap.put(OTHER, "/usr/local/share/TEST/PATH");

		expectedUserLibPathMap.put(MAC,
				Stream.of(HOME_PATH, "Library", "Application Support", "TEST", "PATH").collect(joining(SEP)));
		expectedUserLibPathMap.put(WIN, Stream.of(HOME_PATH, "AppData", "Local", "TEST", "PATH").collect(joining(SEP)));
		expectedUserLibPathMap.put(LINUX, Stream.of(HOME_PATH, "TEST", "PATH").collect(joining(SEP)));
		expectedUserLibPathMap.put(OTHER, Stream.of(HOME_PATH, "TEST", "PATH").collect(joining(SEP)));
	}

	protected void setCurrentOS(OS newCurrentOS) {
		try {
			ReflectionTools.setCurrentOS(newCurrentOS);
		} catch (Exception e) {
			throw new IllegalStateException("unable to change current OS", e);
		}
	}

}
