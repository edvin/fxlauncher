package fxlauncher.config;

import static fxlauncher.model.lifecycle.LifecyclePhase.LOAD_EMBEDDED_CONFIG;
import static fxlauncher.model.lifecycle.LifecyclePhase.PARSE_CLI_ARGS;
import static fxlauncher.model.lifecycle.LifecyclePhase.STARTUP;
import static java.util.stream.Collectors.joining;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fxlauncher.model.lifecycle.LifecyclePhase;

/**
 * The set of configurable options for FxLauncher.
 * @author idavis1
 */
public enum LauncherOption {
	CONFIG_FILE("config-file", true, Defaults.CONFIG_FILE, null, null),
	OVERRIDES_URL("overrides-url", true, Defaults.NONE, null, Validator.URL),
	MANIFEST_URL("manifest-url", true, Defaults.NONE, null, Validator.URL),
	MANIFEST_FILE("manifest-file", true, Defaults.MANIFEST_FILE, null, null),
	ARTIFACTS_REPO_URL("artifacts-repo-url", true, Defaults.NONE, null, Validator.URL),
	CACHE_DIR("cache-dir", true, Defaults.CACHE_DIR, Resolver.CACHE_DIR, null),
	LOG_FILE("log-file", true, Defaults.LOG_FILE, null, null),
	IGNORE_SSL("ignore-ssl", true, Defaults.BOOL_FALSE, null, Validator.BOOL),
	OFFLINE("offline", false, Defaults.BOOL_FALSE, null, null),
	STOP_ON_UPDATE_ERROR("stop-on-update-error", false, Defaults.BOOL_TRUE, null, null),
	ACCEPT_DOWNGRADE("accept-downgrade", false, Defaults.BOOL_FALSE, null, null),
	PRELOAD_NATIVE_LIBS("preload-native-libs", true, Defaults.NONE, null, null),
	HEADLESS("headless", false, Defaults.BOOL_FALSE, null, null),
	WHATS_NEW_URL("whats-new-url", true, Defaults.NONE, null, Validator.URL),
	LINGERING_UPDATE_SCREEN("lingering-update-screen", true, Defaults.BOOL_TRUE, null, Validator.BOOL),;

	// the string that should be used in a command-line argument or properties file to set this option
	private final String label;
	private final Pattern pattern;

	// used to identify when a value has not been explicitly set by a user
	private LifecyclePhase lastSetDuring = STARTUP;

	private final String defaultVal;

	private final Resolver resolver;
	private final Validator validator;

	/**
	 * Retrieve the set of labels for all {@link LauncherOption} instances
	 * @return the set of labels
	 */
	public static Set<String> labels() {
		return Arrays.asList(values()).stream().map(LauncherOption::getLabel).collect(Collectors.toSet());
	}

	/**
	 * Get a subset of options that satisfy some predicate
	 * @param predicate The predicate that determines which enum instances should be included in the subset
	 * @return the set of {@link LauncherOption} objects that satisfy the predicate (return true);
	 */
	private static Set<LauncherOption> getSubset(Predicate<LauncherOption> predicate) {
		return Stream.of(values()).filter(predicate)
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(LauncherOption.class)));
	}

	/**
	 * Get all those options that were most recently set from a properties file
	 * @return the set of {@link LauncherOption} objects
	 */
	public static Set<LauncherOption> getProps() {
		return getSubset(LauncherOption::isProp);
	}

	/**
	 * Get all those options that were most recently set from a command-line argument
	 * @return the set of {@link LauncherOption} objects
	 */
	public static Set<LauncherOption> getArgs() {
		return getSubset(LauncherOption::isArg);
	}

	/**
	 * Get all those options that have not been explicitly set
	 * @return the set of {@link LauncherOption} objects;
	 */
	public static Set<LauncherOption> getUnset() {
		return getSubset(LauncherOption::isUnset);
	}

	private LauncherOption(String label, boolean hasArg, String defaultVal, Resolver resolver, Validator validator) {
		this.label = label;
		this.defaultVal = defaultVal;

		String patternString = Stream.of("--", label, hasArg ? "(=(.+)" : "").collect(joining());
		pattern = Pattern.compile(patternString);

		this.resolver = resolver == null ? Resolver.DEFAULT : resolver;
		this.validator = validator == null ? Validator.DEFAULT : validator;
	}

	/**
	 * get the label from this {@link LauncherOption} instance
	 * @return the label for a particular {@link LauncherOption}
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * get the default value from this {@link LauncherOption}
	 * @return the default value for a particular {@link LauncherOption}
	 */
	public String getDefault() {
		return this.defaultVal;
	}

	/**
	 * check whether this @{link LauncherOption} was last set from a properties file
	 * @return {@code true} if this {@link LauncherOption} was last set from a properties file, {@code false} otherwise
	 */
	public boolean isProp() {
		return lastSetDuring == LOAD_EMBEDDED_CONFIG;
	}

	/**
	 * check whether this {@link LauncherOption} was last set from a command-line argument
	 * @return {@code true} if this {@link LauncherOption} was last set from a command-line argument, {@code false} otherwise
	 */
	public boolean isArg() {
		return lastSetDuring == PARSE_CLI_ARGS;
	}

	/**
	 * check whether this {@link LauncherOption} has been explicitly set
	 * @return {@code true} if this {@link LauncherOption} has ever been explicitly set, {@code false} otherwise
	 */
	public boolean isSet() {
		return lastSetDuring != STARTUP;
	}

	/**
	 * check whether this {@link LauncherOption}  has not been explicitly set
	 * @return {@code false} if this {@link LauncherOption} has ever been explicitly set, {@code true} otherwise
	 */
	public boolean isUnset() {
		return !this.isSet();
	}

	/**
	 * get a Matcher object that can be used to match a String to a {@link LauncherOption}
	 * used to resolve properties and command-line arguments
	 * @param arg
	 * @return a {@link Matcher} created from the {@link LauncherOption}'s label and the string to be matched.
	 */
	Matcher getMatcher(String arg) {
		return pattern.matcher(arg);
	}

	/**
	 * get the {@link Resolver} registered to a {@link LauncherOption}
	 * @return the {@link Resolver}
	 */
	Resolver getResolver() {
		return resolver;
	}

	/**
	 * get the {@link Validator} registered to a {@link LauncherOption}
	 * @return
	 */
	Validator getValidator() {
		return validator;
	}

	/**
	 * make a note of the most recent time this {@link LauncherOption} was set
	 * @param phase
	 */
	void recordOptionSet(LifecyclePhase phase) {
		this.lastSetDuring = phase;
	}

	/**
	 * The set of default values for FxLauncher {@link LauncherOption} objects
	 *
	 * @implNote these are included in an inner class rather than as standard
	 * 'private static final String' constants because Java does not provide
	 * access to static objects during execution of the enum constructors.
	 * 
	 * @author idavis1
	 */
	private static class Defaults {
		private static final String CONFIG_FILE = "launcher.properties";
		private static final String LOG_FILE = System.getProperty("java.io.tmpdir") + "fxlauncher.log";
		private static final String MANIFEST_FILE = "app.xml";
		private static final String CACHE_DIR = Paths.get(".").toString();
		private static final String BOOL_FALSE = Boolean.FALSE.toString();
		private static final String BOOL_TRUE = Boolean.TRUE.toString();
		private static final String NONE = null;
	}
}