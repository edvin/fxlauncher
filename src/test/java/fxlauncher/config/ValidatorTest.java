package fxlauncher.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ValidatorTest {

	@Nested
	@DisplayName("The default implementation of Validator...")
	class DefaultValidator {

		@DisplayName("returns true for every String")
		@ParameterizedTest(name = "returns true for input \"{0}\"")
		@ValueSource(strings = { "", "null", "Some string", "https://some-domain.com?query", "false" })
		public void returnsTrue(String input) {

			input = input.equals("null") ? null : input;
			assertTrue(Validator.DEFAULT.test(input));
		}
	}

	@Nested
	@DisplayName("Validator.BOOLEAN...")
	class BooleanValidator {

		@DisplayName("returns true for appropriate boolean values")
		@ParameterizedTest(name = "returns true for \"{0}\"")
		@ValueSource(strings = { "true", "TRUE", "True", "TrUe", "false", "FALSE", "False", "fAlSe" })
		public void returnsTrue(String input) {
			assertTrue(Validator.BOOL.test(input));
		}

		@DisplayName("returns false for non-boolean Strings")
		@ParameterizedTest(name = "returns false for \"{0}\"")
		@ValueSource(strings = { "", "null", "Some string", "https://some-domain.com?query" })
		public void returnsFalse(String input) {
			input = input.equals("null") ? null : input;
			assertFalse(Validator.BOOL.test(input));
		}
	}

	@Nested
	@DisplayName("Validator.URL...")
	class URLValidator {
		/*
		 * testing every possible valid URL seems imprudent I would suggest adding more
		 * variations to the ValueSource annotation as they become needed
		 */
		@DisplayName("returns true for a valid URL")
		@ParameterizedTest(name = "returns true for \"{0}\"")
		@ValueSource(strings = { "https://some-domain.com/file.txt?key=value,key=value", })
		public void returnsTrue(String input) {
			assertTrue(Validator.URL.test(input));
		}

		@DisplayName("returns false for any non-URL string")
		@ParameterizedTest(name = "returns false for \"{0}\"")
		@ValueSource(strings = { "", "null", "Some string", "false" })
		public void returnsFalse(String input) {
			input = input.equals("null") ? null : input;
			assertFalse(Validator.URL.test(input));
		}
	}
}
