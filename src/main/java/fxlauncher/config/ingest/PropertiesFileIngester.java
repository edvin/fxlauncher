package fxlauncher.config.ingest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fxlauncher.config.LauncherConfig;
import fxlauncher.config.LauncherOption;
import fxlauncher.tools.io.ClasspathResourceFetcher;

/**
 * Implementation of {@link ConfigurationIngester} that reads values in from a
 * Java properties file.
 *
 * @author idavis1
 *
 */
public class PropertiesFileIngester extends ConfigurationIngester {

	private static final Logger log = Logger.getLogger(PropertiesFileIngester.class.getName());

	private final Properties props = new Properties();

	// using Supplier here allows the source to be defined at construction-time, but
	// not retrieved before _ingest() is invoked. This is handy when another
	// ConfigurationIngester can change configuration state after this object is
	// constructed, but before the _ingest() method is invoked.
	private Supplier<String> resourceNameSupplier = () -> LauncherOption.CONFIG_FILE.getDefault();

	public PropertiesFileIngester() {
		super();
	}

	public PropertiesFileIngester(String resourceName) {
		super();
		this.resourceNameSupplier = () -> resourceName;
	}

	public PropertiesFileIngester(Supplier<String> resourceNameSupplier) {
		super();
		this.resourceNameSupplier = resourceNameSupplier;
	}

	/**
	 * Fluent interface for setting the {@code resourceName} member.
	 *
	 * @param resourceName the static name of a resource containing configuration
	 *                     settings as Java properties
	 * @return this PropertiesFileIngester object, so that further operations can be
	 *         performed on it
	 */
	public PropertiesFileIngester forPropertyFile(String resourceName) {
		this.resourceNameSupplier = () -> resourceName;
		return this;
	}

	/**
	 * Fluent interface for setting the {@code resourceName} member.
	 *
	 * @param resourceName a {@link Supplier} of the name of a resource containing
	 *                     configuration settings as Java properties
	 * @return this PropertiesFileIngester object, so that further operations can be
	 *         performed on it
	 */
	public PropertiesFileIngester forPropertyFile(Supplier<String> resourceNameSupplier) {
		this.resourceNameSupplier = resourceNameSupplier;
		return this;
	}

	// invoked by superclass -- ingest properties from the named resource
	@Override
	protected List<String> _ingestParams() {
		loadEmbeddedProps(resourceNameSupplier.get());
		props.forEach(this::matchAndExtract);
		return props.entrySet().stream().map(PropertiesFileIngester::formatProperty).collect(Collectors.toList());
	}

	private void loadEmbeddedProps(String resourceName) {
		log.fine(NOTIFY_MSG.apply(resourceName));
		Optional<InputStream> fetched = new ClasspathResourceFetcher(resourceName).fetch();

		if (fetched.isPresent()) {
			log.info(FOUND_MSG.apply(resourceName));
			try {
				props.load(fetched.get());
			} catch (IOException e) {
				log.warning(INVALID_MSG.apply(resourceName));
				e.printStackTrace();
			}
		}

		log.info(NOT_FOUND_MSG);
	}

	private void matchAndExtract(Object keyObj, Object valueObj) {
		String key = keyObj.toString();
		String value = valueObj.toString();

		log.finer(INGEST_PROP_MSG.apply(key, value));
		for (LauncherOption opt : LauncherOption.values()) {
			if (key.equals(opt.getLabel())) {
				log.finer(MATCHED_OPT_MSG.apply(key, opt.toString()));
				LauncherConfig.setOption(opt, value);
				props.remove(keyObj);
			}
			return;
		}
		log.finer(UNMATCHED_OPT_MSG.apply(key));
	}

	private static String formatProperty(Map.Entry<Object, Object> property) {
		return (property.getValue() == null) ? UNNAMED_PROP_FMT.apply(property) : NAMED_PROP_FMT.apply(property);
	}

	// just giving convenient names to some simple but cumbersome String formatting
	// logic to improve readability above this comment
	private static final UnaryOperator<String> NOTIFY_MSG = resourceName -> String
			.format("Importing embedded properties resource: %s", resourceName);
	private static final UnaryOperator<String> FOUND_MSG = resourceName -> String
			.format("Found embedded properties file %s", resourceName);
	private static final UnaryOperator<String> INVALID_MSG = resourceName -> String
			.format("Failed to load embedded properties file '%s'. Check file format/syntax", resourceName);
	private static final String NOT_FOUND_MSG = "No embedded properties file found";

	private static final BinaryOperator<String> INGEST_PROP_MSG = (key, value) -> String
			.format("Attempting to property with key=%s and value=%s...", key, value);
	private static final BinaryOperator<String> MATCHED_OPT_MSG = (key, opt) -> String
			.format("Matched property key '%s' to LauncherOption '%s'...", key, opt);
	private static final UnaryOperator<String> UNMATCHED_OPT_MSG = key -> String
			.format("No LauncherOption found to match property key '%s'. Passing along to downstream app", key);

	private static final Function<Map.Entry<Object, Object>, String> NAMED_PROP_FMT = prop -> String.format("--%s=%s",
			prop.getKey().toString(), prop.getValue().toString());
	private static final Function<Map.Entry<Object, Object>, String> UNNAMED_PROP_FMT = prop -> String.format("--%s",
			prop.getKey().toString());

}
