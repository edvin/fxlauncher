package fxlauncher.model;

import static fxlauncher.model.GenericPathLabel.ALLUSERS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Test of methods in the fxlauncher.model.OS enumerated type")
public class OSTest {

    @SuppressWarnings("serial")
    private static final Map<OS, Path> expectedAllUsersPathMap = new EnumMap<OS, Path>(OS.class) {};

    @BeforeAll
    public static void init() {
        expectedAllUsersPathMap.put(OS.MAC, Paths.get("/Library/Application Support"));
        expectedAllUsersPathMap.put(OS.WIN, Paths.get("/AppData/Local"));
        expectedAllUsersPathMap.put(OS.LINUX, Paths.get("/usr/local/share"));
        expectedAllUsersPathMap.put(OS.OTHER, Paths.get("/usr/local/share"));
    }

    @ParameterizedTest(name = "OS \"{0}\" returns the correct ALLUSERS path.")
    @ValueSource(strings = {"WIN", "MAC", "LINUX", "OTHER"})
    @DisplayName("Test of ALLUSERS Path resolution for all Operating Systems")
    void testAllUsersPath(String osString) {

        OS os = OS.valueOf(osString);
        Path expected = expectedAllUsersPathMap.get(os);

        assertEquals(expected, os.getGenericPath(ALLUSERS));
    }
}
