package fxlauncher.config;

import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@FunctionalInterface
/*
 * Functional Interface for validating values passed to LauncherConfig plus
 * default implementations for common cases.
 */
public interface Validator extends Predicate<String> {

	static public String getExpected(Validator validator) {
		if (validator == Validator.BOOL)
			return "boolean";
		if (validator == Validator.URL)
			return "URL";
		return "String";
	}

	// OWASP-supplied URL pattern-matching regexp
	static final String URL_REGEX = "^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))"
			+ "(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)" + "([).!';/?:,][[:blank:]])?$";

	static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

	public static final Validator DEFAULT = string -> true;

	public static final Validator BOOL = string -> {
		Logger.getLogger(Validator.BOOL.getClass().getName())
				.finer(String.format("validating as boolean: '%s'", string));
		return string == null ? false : string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false");
	};

	public static final Validator URL = string -> {
		Logger.getLogger(Validator.URL.getClass().getName()).finer(String.format("validating as URL: '%s'", string));

		return string == null ? false : URL_PATTERN.matcher(string).matches();
	};
}
