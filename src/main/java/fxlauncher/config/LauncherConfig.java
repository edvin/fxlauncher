package fxlauncher.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.logging.Logger;

import fxlauncher.except.FXLauncherConfigException;
import fxlauncher.model.lifecycle.LifecyclePhase;

/**
 * Class represents the current configuration state of the FXLauncher
 * application itself, and provides methods for changing that state.
 *
 * Class is internally a singleton, accessed by static methods.
 *
 * @author idavis1
 */
public class LauncherConfig {

	private final static Logger log = Logger.getLogger(LauncherConfig.class.getName());

	private final Map<LauncherOption, String> configMap = new EnumMap<LauncherOption, String>(LauncherOption.class);
	private static final LauncherConfig instance = new LauncherConfig();

	/**
	 * update the current state value of a {@link LauncherOption}.
	 *
	 * Before storing the value, performs any validation and transformation defined
	 * in the {@link LauncherOption} instance.
	 *
	 * @param option the {@link LauncherOption} to be associated with a value
	 * @param value  the value to be associated
	 */
	public static void setOption(LauncherOption option, String value) {
		log.finer(ATTEMPT_SET_MSG.apply(option, value));
		String resolved = option.getResolver().apply(value);
		if (!value.equals(resolved))
			log.finer(RESOLVED_MSG.apply(value, resolved));
		validateOptionValue(option, value);

		instance.configMap.put(option, resolved);
		option.recordOptionSet(LifecyclePhase.current);
		log.fine(OPTION_SET_MSG.apply(option, resolved));
	}

	/**
	 * Fetch any value currently associated with the given {@link LauncherOption},
	 *
	 * @param option the {@link LauncherOption} to be retrieved
	 * @return the explicitly-set value associated with the given
	 *         {@link LauncherOption}, or a suitable default if no explicit value is
	 *         present
	 */
	public static String getOption(LauncherOption option) {
		return instance.configMap.getOrDefault(option, option.getDefault());
	}

	private static void validateOptionValue(LauncherOption option, String value) {
		if (!option.getValidator().test(value)) {
			throw new FXLauncherConfigException(
					String.format("Cannot ingest invalid value '%s' into option '%s'. Not a valid value. Expected %s",
							value, option, Validator.getExpected(option.getValidator())));
		}
	}

	private static final BiFunction<LauncherOption, String, String> ATTEMPT_SET_MSG = (opt, val) -> String
			.format("Attempting to set LauncherOption.%s to value '%s'", opt, val);
	private static final BinaryOperator<String> RESOLVED_MSG = (unresolved, resolved) -> String
			.format("Input '%s' resolved to '%s'", unresolved, resolved);
	private static final BiFunction<LauncherOption, String, String> OPTION_SET_MSG = (opt, val) -> String
			.format("Successfully set option '%s' to '%s'", opt, val);
}
