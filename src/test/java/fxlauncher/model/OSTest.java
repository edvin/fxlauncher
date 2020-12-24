package fxlauncher.model;

import static fxlauncher.model.GenericPathLabel.ALLUSERS;
import static fxlauncher.model.GenericPathLabel.USERLIB;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Test of methods in the fxlauncher.model.OS enumerated type")
public class OSTest extends OSTestHarness {

	@ParameterizedTest(name = "OS \"{0}\" returns the correct ALLUSERS path.")
	@ValueSource(strings = { "WIN", "MAC", "LINUX", "OTHER" })
	@DisplayName("Test of ALLUSERS Path resolution for all operating systems")
	void testAllUsersPath(String osString) {

		OS os = OS.valueOf(osString);
		Path expected = expectedAllUsersPathMap.get(os);

		assertEquals(expected, os.getGenericPath(ALLUSERS));
	}

	@ParameterizedTest(name = "OS \"{0}\" returns the correct USERLIB path.")
	@ValueSource(strings = { "WIN", "MAC", "LINUX", "OTHER" })
	@DisplayName("Test of USERLIB Path resolution for all operating systems")
	void testUserLibPath(String osString) {

		OS os = OS.valueOf(osString);
		Path expected = expectedUserLibPathMap.get(os);

		assertEquals(expected, os.getGenericPath(USERLIB));
	}
}
