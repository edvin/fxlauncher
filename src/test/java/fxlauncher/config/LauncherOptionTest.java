package fxlauncher.config;

import static fxlauncher.config.LauncherOption.ACCEPT_DOWNGRADE;
import static fxlauncher.config.LauncherOption.CACHE_DIR;
import static fxlauncher.config.LauncherOption.HEADLESS;
import static fxlauncher.config.LauncherOption.MANIFEST_URL;
import static fxlauncher.config.LauncherOption.OFFLINE;
import static fxlauncher.config.LauncherOption.OVERRIDES_URL;
import static fxlauncher.config.LauncherOption.PRELOAD_NATIVE_LIBS;
import static fxlauncher.config.LauncherOption.WHATS_NEW_URL;
import static fxlauncher.model.lifecycle.LifecyclePhase.LOAD_EMBEDDED_CONFIG;
import static fxlauncher.model.lifecycle.LifecyclePhase.PARSE_CLI_ARGS;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.SetUtils.difference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class LauncherOptionTest extends LauncherOptionTestHarness {

	@DisplayName("Static method tests: ")
	@Nested
	class staticMethods {

		@DisplayName("'labels()' method returns expected set of Strings")
		@Test
		void labelsTest() {
			assertEquals(expectedLabels, LauncherOption.labels());
		}

		@DisplayName("'getProps()' method returns values set during ingestion of embedded properties file")
		@Test
		void getPropsTest() {
			Set<LauncherOption> props = Stream.of(CACHE_DIR, HEADLESS)
					.peek(opt -> opt.recordOptionSet(LOAD_EMBEDDED_CONFIG)).collect(toSet());

			assertEquals(props, LauncherOption.getProps());
		}

		@DisplayName("'getArgs()' method returns values set during ingestion of command-line args file")
		@Test
		void getArgsTest() {
			Set<LauncherOption> args = Stream.of(ACCEPT_DOWNGRADE, OFFLINE)
					.peek(opt -> opt.recordOptionSet(PARSE_CLI_ARGS)).collect(toSet());

			assertEquals(args, LauncherOption.getArgs());
		}

		@DisplayName("'getUnset()' returns all values that have not been explicitly set")
		@Test
		void getUnsetTest() {
			Set<LauncherOption> presetOpts = Stream.of(OVERRIDES_URL, MANIFEST_URL, WHATS_NEW_URL, PRELOAD_NATIVE_LIBS)
					.peek(opt -> opt.recordOptionSet(getRandomNonStartupPhase())).collect(toSet());

			assertEquals(difference(getValueSet(), presetOpts), LauncherOption.getUnset());
		}

		@DisplayName("'getSet()' returns all values that have been previously set")
		@Test
		void getSetTest() {
			Set<LauncherOption> presetOpts = Stream.of(OVERRIDES_URL, MANIFEST_URL, WHATS_NEW_URL, PRELOAD_NATIVE_LIBS)
					.peek(opt -> opt.recordOptionSet(getRandomNonStartupPhase())).collect(toSet());

			assertEquals(presetOpts, LauncherOption.getSet());
		}
	}

	@DisplayName("Instance method tests: ")
	@Nested
	class instanceMethods {

		@DisplayName("'getLabel()'...")
		@ParameterizedTest(name = "returns the appropriate label for {0}")
		@EnumSource(LauncherOption.class)
		void getLabelTest(LauncherOption opt) {
			assertEquals(labelMap.get(opt), opt.getLabel());
		}

		@DisplayName("'getDefault()'...")
		@ParameterizedTest(name = "returns the appropriate default value for {0}")
		@EnumSource(LauncherOption.class)
		void getDefaultTest(LauncherOption opt) {
			assertEquals(defaultMap.get(opt), opt.getDefault());
		}

		@DisplayName("'isProp()'...")
		@ParameterizedTest(name = "returns true iff LauncherOption {0} was last set during ingest properties")
		@EnumSource(LauncherOption.class)
		void isPropTest(LauncherOption opt) {
			assertFalse(opt.isProp());
			opt.recordOptionSet(LOAD_EMBEDDED_CONFIG);
			assertTrue(opt.isProp());
		}

		@DisplayName("'isArg()'... ")
		@ParameterizedTest(name = "returns true iff LauncherOption {0} was last set during ingest cli-args")
		@EnumSource(LauncherOption.class)
		void isArgTest(LauncherOption opt) {
			assertFalse(opt.isArg());
			opt.recordOptionSet(PARSE_CLI_ARGS);
			assertTrue(opt.isArg());
		}

		@DisplayName("'isSet()'...")
		@ParameterizedTest(name = "returns true iff LauncherOption {0} has been explicitly set")
		@EnumSource(LauncherOption.class)
		void isSetTest(LauncherOption opt) {
			assertFalse(opt.isSet());
			opt.recordOptionSet(getRandomNonStartupPhase());
			assertTrue(opt.isSet());
		}

		@DisplayName("'isUnset()'...")
		@ParameterizedTest(name = "returns true iff LauncherOption {0} has never been explicitly set")
		@EnumSource(LauncherOption.class)
		void isUnsetTest(LauncherOption opt) {
			assertTrue(opt.isUnset());
			opt.recordOptionSet(getRandomNonStartupPhase());
			assertFalse(opt.isUnset());
		}

		@DisplayName("'getMatcher()'...")
		@ParameterizedTest(name = "returns a Matcher for LauncherOption {0} that returns true for an appropriate string")
		@EnumSource(LauncherOption.class)
		void getMatcherTest(LauncherOption opt) {
			String matchString = matcherMap.get(opt);
			assertTrue(opt.getMatcher(matchString).matches());
		}

		@DisplayName("'getResolver()'...")
		@ParameterizedTest(name = "returns the appropriate Resolver for LauncherOption {0}")
		@EnumSource(LauncherOption.class)
		void getResolverTest(LauncherOption opt) {
			assertEquals(getExpectedResolver(opt), opt.getResolver());
		}

		@DisplayName("'getValidator()...")
		@ParameterizedTest(name = "returns the appropriate Validator for LauncherOption {0}")
		@EnumSource(LauncherOption.class)
		void getValidatorTest(LauncherOption opt) {
			assertEquals(getExpectedValidator(opt), opt.getValidator());
		}
	}
}
