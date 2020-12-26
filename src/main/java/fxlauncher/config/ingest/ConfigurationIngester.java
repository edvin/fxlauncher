package fxlauncher.config.ingest;

import java.util.List;

import fxlauncher.downstream.DownstreamParameters;

/**
 * Base class for all classes that read in a source of FxLauncher configuration
 * data. Subclasses are expected to implement the
 * {@code _ingestParams()) method and
 * return the set of ingested options that do not match any {@link
 * LauncherOption} value.
 *
 * @author idavis1
 */
public abstract class ConfigurationIngester {

	// if no link DownstreamParameters object is provided, use a do-nothing one that
	// will be discarded by the trash-collector later.
	private static DownstreamParameters defaultDownstreamParams = new DownstreamParameters();

	protected boolean overwriteArgs = true;
	protected DownstreamParameters downstreamParams = defaultDownstreamParams;

	// set for all new instances
	public static void setDefaultDownstreamParams(DownstreamParameters downstreamParams) {
		ConfigurationIngester.defaultDownstreamParams = downstreamParams;
	}

	/**
	 * Initiate implementation-specific ingest process and stash the overflow in a
	 * {@link DownstreamParameters} object.
	 */
	public void ingest() {
		List<String> leftovers = _ingestParams();
		leftovers.forEach(arg -> downstreamParams.merge(arg, overwriteArgs));
	}

	public ConfigurationIngester storeDownstreamParamsIn(DownstreamParameters downstreamParams) {
		this.downstreamParams = downstreamParams;
		return this;
	}

	/**
	 * Must-override method containing implementation-specific ingest logic
	 *
	 * @return a list of ingested options not used by FxLauncher itself
	 */
	protected abstract List<String> _ingestParams();
}
