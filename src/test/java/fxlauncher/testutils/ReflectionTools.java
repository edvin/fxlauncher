package fxlauncher.testutils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import fxlauncher.model.OS;

/**
 * A collection of reflection-based utility methods to assist in testing
 *
 * @author idavis1
 *
 */
public class ReflectionTools {

	/**
	 * Sets an environment variable in the memory-resident representation without
	 * actually overwriting anything on the underlying system
	 *
	 * @param name the name of the environment variable to set
	 * @param val  the value we want it to have
	 * @throws Exception if the reflection fails for security reasons
	 */
	@SuppressWarnings("unchecked")
	public static void updateEnv(String name, String val) throws ReflectiveOperationException {
		Map<String, String> env = System.getenv();
		Field field = env.getClass().getDeclaredField("m");
		field.setAccessible(true);
		((Map<String, String>) field.get(env)).put(name, val);
	}

	/**
	 * Overwrites the private field 'current' in the OS enum for purposes of testing
	 * OS-dependent outputs
	 *
	 * @param newCurrentOS the new current OS
	 * @throws Exception if reflection fails
	 */
	public static void setCurrentOS(OS newCurrentOS) throws Exception {
		Field currentOSField = OS.class.getField("current");
		currentOSField.setAccessible(true);

		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(currentOSField, currentOSField.getModifiers() & ~Modifier.FINAL);

		currentOSField.set(null, newCurrentOS);
	}
}
