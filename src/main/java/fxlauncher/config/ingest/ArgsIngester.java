package fxlauncher.config.ingest;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import fxlauncher.config.LauncherConfig;
import fxlauncher.config.LauncherOption;

/**
 * Concrete implemention of {@link ConfigurationIngester} that deduces
 * {@link LauncherConfig} settings from command-line arguments (or any other
 * String array);
 *
 * @author idavis1
 *
 */
public class ArgsIngester extends ConfigurationIngester {

	private static final Logger log = Logger.getLogger(ArgsIngester.class.getName());

	private String[] args = new String[0];

	public ArgsIngester() {
		super();
	}

	public ArgsIngester(String... args) {
		super();
		this.args = args;
	}

	// -- invoked by superclass when ingestParams() is called.
	@Override
	protected List<String> _ingestParams() {
		log.info(BEGIN_MSG.apply(args));
		List<String> leftovers = Stream.of(args).filter(this::matchAndExtract).collect(toList());
		return leftovers;
	}

	/**
	 * Fluent interface for setting the set of arguments to be ingested
	 *
	 * @param args the array of arguments to be ingested
	 * @return this ArgsIngester object, so that further operations can be performed
	 *         on it.
	 */
	public ArgsIngester forArgs(String[] args) {
		this.args = args;
		return this;
	}

	private boolean matchAndExtract(String arg) {
		for (LauncherOption opt : LauncherOption.values()) {
			Matcher matcher = opt.getMatcher(arg);
			if (matcher.find()) {
				log.finer(MATCHED_OPT_MSG.apply(arg, opt.toString()));
				String value = matcher.group(1);
				LauncherConfig.setOption(opt, value);
				return false;
			} else {
				log.finer(UNMATCHED_OPT_MSG.apply(arg));
				continue;
			}
		}
		return true;
	}

	private static final Function<String[], String> BEGIN_MSG = args -> String
			.format("Ingesting command-line arguments: %s", Stream.of(args).collect(joining(",", "[", "]")));
	private static final BinaryOperator<String> MATCHED_OPT_MSG = (arg, opt) -> String
			.format("Matched argument '%s' with LauncherOption '%s'", arg, opt);
	private static final UnaryOperator<String> UNMATCHED_OPT_MSG = (arg) -> String
			.format("No matching LauncherOption found for argument: '%s'. Sending to downstream application", arg);
}
