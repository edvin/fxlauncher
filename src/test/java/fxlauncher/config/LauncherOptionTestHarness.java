package fxlauncher.config;

import static fxlauncher.config.LauncherOption.ACCEPT_DOWNGRADE;
import static fxlauncher.config.LauncherOption.ARTIFACTS_REPO_URL;
import static fxlauncher.config.LauncherOption.CACHE_DIR;
import static fxlauncher.config.LauncherOption.CONFIG_FILE;
import static fxlauncher.config.LauncherOption.HEADLESS;
import static fxlauncher.config.LauncherOption.IGNORE_SSL;
import static fxlauncher.config.LauncherOption.LINGERING_UPDATE_SCREEN;
import static fxlauncher.config.LauncherOption.LOG_FILE;
import static fxlauncher.config.LauncherOption.MANIFEST_FILE;
import static fxlauncher.config.LauncherOption.MANIFEST_URL;
import static fxlauncher.config.LauncherOption.OFFLINE;
import static fxlauncher.config.LauncherOption.OVERRIDES_URL;
import static fxlauncher.config.LauncherOption.PRELOAD_NATIVE_LIBS;
import static fxlauncher.config.LauncherOption.STOP_ON_UPDATE_ERROR;
import static fxlauncher.config.LauncherOption.WHATS_NEW_URL;
import static fxlauncher.model.lifecycle.LifecyclePhase.STARTUP;
import static java.util.stream.Collectors.toSet;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;

import fxlauncher.model.lifecycle.LifecyclePhase;

public class LauncherOptionTestHarness {

	@AfterEach
	void restoreSetDuring() {
		Stream.of(LauncherOption.values()).forEach(opt -> opt.recordOptionSet(STARTUP));
	}

	protected static final Map<LauncherOption, String> labelMap = new EnumMap<LauncherOption, String>(
			LauncherOption.class) {
		{
			put(CONFIG_FILE, "config-file");
			put(OVERRIDES_URL, "overrides-url");
			put(MANIFEST_URL, "manifest-url");
			put(MANIFEST_FILE, "manifest-file");
			put(ARTIFACTS_REPO_URL, "artifacts-repo-url");
			put(CACHE_DIR, "cache-dir");
			put(LOG_FILE, "log-file");
			put(IGNORE_SSL, "ignore-ssl");
			put(OFFLINE, "offline");
			put(STOP_ON_UPDATE_ERROR, "stop-on-update-error");
			put(ACCEPT_DOWNGRADE, "accept-downgrade");
			put(PRELOAD_NATIVE_LIBS, "preload-native-libs");
			put(HEADLESS, "headless");
			put(WHATS_NEW_URL, "whats-new-url");
			put(LINGERING_UPDATE_SCREEN, "lingering-update-screen");
		}
	};

	protected static final Map<LauncherOption, String> defaultMap = new EnumMap<LauncherOption, String>(
			LauncherOption.class) {
		{
			put(CONFIG_FILE, "launcher.properties");
			put(OVERRIDES_URL, null);
			put(MANIFEST_URL, null);
			put(MANIFEST_FILE, "app.xml");
			put(ARTIFACTS_REPO_URL, null);
			put(CACHE_DIR, ".");
			put(LOG_FILE, System.getProperty("java.io.tmpdir") + "fxlauncher.log");
			put(IGNORE_SSL, Boolean.FALSE.toString());
			put(OFFLINE, Boolean.FALSE.toString());
			put(STOP_ON_UPDATE_ERROR, Boolean.TRUE.toString());
			put(ACCEPT_DOWNGRADE, Boolean.FALSE.toString());
			put(PRELOAD_NATIVE_LIBS, null);
			put(HEADLESS, Boolean.FALSE.toString());
			put(WHATS_NEW_URL, null);
			put(LINGERING_UPDATE_SCREEN, Boolean.TRUE.toString());
		}
	};

	protected static final Map<LauncherOption, String> matcherMap = new EnumMap<LauncherOption, String>(
			LauncherOption.class) {
		{
			put(CONFIG_FILE, "--config-file=somefile");
			put(OVERRIDES_URL, "--overrides-url=https://some-domain.com");
			put(MANIFEST_URL, "--manifest-url=https://some-domain.com");
			put(MANIFEST_FILE, "--manifest-file=somefile");
			put(ARTIFACTS_REPO_URL, "--artifacts-repo-url=https://some-domain.com");
			put(CACHE_DIR, "--cache-dir=somefile");
			put(LOG_FILE, "--log-file=somefile");
			put(IGNORE_SSL, "--ignore-ssl");
			put(OFFLINE, "--offline");
			put(STOP_ON_UPDATE_ERROR, "--stop-on-update-error");
			put(ACCEPT_DOWNGRADE, "--accept-downgrade");
			put(PRELOAD_NATIVE_LIBS, "--preload-native-libs=list,of,libs");
			put(HEADLESS, "--headless");
			put(WHATS_NEW_URL, "--whats-new-url=https://some-url");
			put(LINGERING_UPDATE_SCREEN, "--lingering-update-screen");
		}
	};

	protected static final Resolver getExpectedResolver(LauncherOption opt) {
		switch (opt) {
		case CACHE_DIR:
			return Resolver.CACHE_DIR;
		default:
			return Resolver.DEFAULT;
		}
	}

	protected static final Validator getExpectedValidator(LauncherOption opt) {
		switch (opt) {
		case OVERRIDES_URL:
		case MANIFEST_URL:
		case ARTIFACTS_REPO_URL:
		case WHATS_NEW_URL:
			return Validator.URL;
		case IGNORE_SSL:
		case OFFLINE:
		case STOP_ON_UPDATE_ERROR:
		case ACCEPT_DOWNGRADE:
		case HEADLESS:
		case LINGERING_UPDATE_SCREEN:
			return Validator.BOOL;
		default:
			return Validator.DEFAULT;
		}

	}

	protected static Set<String> expectedLabels = labelMap.values().stream().collect(toSet());

	private static final Random random = new Random();
	private static final int maxIndex = LauncherOption.values().length - 1;

	protected LifecyclePhase getRandomNonStartupPhase() {
		int index = random.nextInt(maxIndex - 1) + 1;
		return LifecyclePhase.values()[index];
	}

	protected Set<LauncherOption> getValueSet() {
		return Stream.of(LauncherOption.values()).collect(toSet());
	}
}
