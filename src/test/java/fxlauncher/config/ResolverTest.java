package fxlauncher.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import fxlauncher.model.OS;

public class ResolverTest extends ResolverTestHarness {

	@DisplayName("Resolver.DEFAULT...")
	@ParameterizedTest(name = "returns \"{0}\" unchanged")
	@ValueSource(strings = { "", "null", "someString" })
	public void defaultResolverTest(String input) {
		input = input.equals("null") ? null : input;
		assertEquals(input, Resolver.DEFAULT.apply(input));
	}

	@DisplayName("Resolver.CACHE_DIR...")
	@Nested
	class CacheDirResolver {

		@DisplayName("when handling ALLUSERS sentinel...")
		@Nested
		class AllUsersTests {

			@DisplayName("resolves leading ALLUSERS for...")
			@ParameterizedTest(name = "OS {0}")
			@EnumSource(OS.class)
			void resolveAllUsersAtStartOfPath(OS os) {
				setCurrentOS(os);

				String inputPath = "ALLUSERS/TEST/PATH";
				assertEquals(expectedAllUsersPathMap.get(os), Resolver.CACHE_DIR.apply(inputPath));
			}

			@DisplayName("ignores non-leading ALLUSERS for...")
			@ParameterizedTest(name = "OS {0}")
			@EnumSource(OS.class)
			void ignoreNonLeadingAllUsers(OS os) {
				setCurrentOS(os);

				String inputPath = "TEST/ALLUSERS/PATH";
				assertEquals(inputPath, Resolver.CACHE_DIR.apply(inputPath));
			}
		}

		@DisplayName("when handling USERLIB sentinel...")
		@Nested
		class UserLibTests {

			@DisplayName("resolves leading USERLIB for...")
			@ParameterizedTest(name = "OS {0}")
			@EnumSource(OS.class)
			void resolveAllUsersAtStartOfPath(OS os) {
				setCurrentOS(os);

				String inputPath = "USERLIB/TEST/PATH";
				assertEquals(expectedUserLibPathMap.get(os), Resolver.CACHE_DIR.apply(inputPath));
			}

			@DisplayName("ignores non-leading ALLUSERS for...")
			@ParameterizedTest(name = "OS {0}")
			@EnumSource(OS.class)
			void ignoreNonLeadingAllUsers(OS os) {
				setCurrentOS(os);

				String inputPath = "TEST/USERLIB/PATH";
				assertEquals(inputPath, Resolver.CACHE_DIR.apply(inputPath));
			}
		}

		@DisplayName("When handling a null path, returns it unchanged")
		@Test
		void testNullHandling() {
			assertEquals(null, Resolver.CACHE_DIR.apply(null));
		}

		@DisplayName("When handling an empty String, returns it unchanged")
		@Test
		void testEmptyStringHandling() {
			assertEquals("", Resolver.CACHE_DIR.apply(""));
		}
	}
}
