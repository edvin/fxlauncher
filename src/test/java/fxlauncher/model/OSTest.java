package fxlauncher.model;

import static fxlauncher.model.GenericPathLabel.ALLUSERS;
import static fxlauncher.model.GenericPathLabel.USERLIB;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("Test of methods in the fxlauncher.model.OS enumerated type")
public class OSTest extends OSTestHarness {

	@ParameterizedTest(name = "OS \"{0}\" returns the correct path.")
	@EnumSource(OS.class)
	@DisplayName("When resolving 'ALLUSERS' sentinel value...")
	void testAllUsersPath(OS os) {
		assertEquals(expectedAllUsersPathMap.get(os), os.getGenericPath(ALLUSERS));
	}

	@ParameterizedTest(name = "OS \"{0}\" returns the correct path.")
	@EnumSource(OS.class)
	@DisplayName("When resolver 'USERLIB' sentinel value...")
	void testUserLibPath(OS os) {

		assertEquals(expectedUserLibPathMap.get(os), os.getGenericPath(USERLIB));
	}
}
