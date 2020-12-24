package fxlauncher.testutils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * A collection of reflection-based utility methods to assist in testing
 * @author idavis1
 *
 */
public class ReflectionTools {

	/**
	 * Sets an environment variable in the memory-resident representation
	 * without actually overwriting anything on the underlying system
	 * @param name the name of the environment variable to set
	 * @param val the value we want it to have
	 * @throws Exception if the reflection fails for security reasons
	 */
	@SuppressWarnings("unchecked")
	public static void updateEnv(String name, String val) throws ReflectiveOperationException{
		Map<String, String> env = System.getenv();
		Field field = env.getClass().getDeclaredField("m");
		field.setAccessible(true);
		((Map<String, String>) field.get(env)).put(name,  val);
	}
}
