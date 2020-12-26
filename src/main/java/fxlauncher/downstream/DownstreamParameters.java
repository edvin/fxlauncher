package fxlauncher.downstream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;

/**
 * Subclass of JavaFX's abstract {@link Application.Parameters} meant to collect
 * all arguments provided to FxLauncher, but not explicitly used by FxLauncher.
 * The assumption is that these are meant for the downstream application.
 *
 * Also provides the same service for non-JavaFX downstream applications
 * launched via their {@code main()} method by allowing retrieval of the
 * rawParams member as an array.
 *
 * @author idavis1
 *
 */
public class DownstreamParameters extends Application.Parameters {

	private static final Pattern NAMED_PATTERN = Pattern.compile("--(?<key>.\\+)=(?<value>.\\+)");

	// use Set to simply enforce uniqueness
	private static Set<String> rawParams = new HashSet<>();
	private static Set<String> unnamedParams = new HashSet<>();
	private static Map<String, String> namedParams = new HashMap<>();

	/**
	 * Return the set of raw arguments
	 */
	@Override
	public List<String> getRaw() {
		return new ArrayList<>(rawParams);
	}

	/**
	 * Return the set of unnamed arguments (i.e. those not of the format
	 * '--key=value')
	 */
	@Override
	public List<String> getUnnamed() {
		return new ArrayList<>(unnamedParams);
	}

	/**
	 * Return the set of named arguments (i.e. those of the format '--key=value')
	 */
	@Override
	public Map<String, String> getNamed() {
		return namedParams;
	}

	/**
	 * Return the set of arguments as a String array This is useful when trying to
	 * launch the downstream program via a {@code main()} method.
	 *
	 * @return the set of arguments as an array
	 */
	public String[] getArgs() {
		return rawParams.parallelStream().toArray(String[]::new);
	}

	/**
	 * Add the argument to the set of downstream parameters, unless it is already
	 * present in the set
	 *
	 * Useful when ingesting a manifest to avoid overwriting any
	 * {@link LauncherConfig} options that were explicitly set by command-line args,
	 * embedded configuration files, or remote overrides
	 *
	 * @param string the argument to be merged into the set of Parameters
	 */
	public void mergeIfNotPresent(String string) {
		merge(string, false);
	}

	/**
	 * Add the argument to the set of downstream parameters, overwriting any
	 * pre-existing value
	 *
	 * Useful when ingesting initial configuration data from command-line args,
	 * embedded configuration files, and remote overrides, where precedence is set
	 * by order and each should override the previous sets of configuration.
	 *
	 * @param string the argument to be merged into the set of Parameters
	 */
	public void mergeOverwriting(String string) {
		merge(string, true);
	}

	/**
	 * Generic merge method that hands both overwriting and non-overwriting cases
	 *
	 * note that overwriting only applies to named parameters uniqueness of
	 * non-named parameters and CLI arguments is ensured by the use of a Set
	 * internally.
	 *
	 * @param string    the argument to be merged into the set of Parameters
	 * @param overwrite a boolean indicating whether existing parameters should be
	 *                  overwritten
	 */
	public void merge(String string, boolean overwrite) {
		Matcher matcher = NAMED_PATTERN.matcher(string);
		if (matcher.find()) {
			String key = matcher.group("key");
			String value = matcher.group("value");
			if (overwrite || !namedParams.containsKey(key)) {
				namedParams.put(key, value);
			}
		} else {
			unnamedParams.add(string);
		}
		rawParams.add(string);
	}
}
