package fxlauncher.tools.io;

import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;

import fxlauncher.except.FXLauncherException;
import fxlauncher.old.Launcher;

/**
 * Implementation of {@link FileFetcher} that retrieves a resource from the
 * classpath by its name
 *
 * @author idavis1
 */
public class ClasspathResourceFetcher implements FileFetcher {

	private final static Logger log = Logger.getLogger(ClasspathResourceFetcher.class.getName());

	private final String resourceName;

	public ClasspathResourceFetcher(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * Retrieves the resource named in {@code resourceName} from the classpath
	 */
	@Override
	public Optional<InputStream> fetch() throws FXLauncherException {
		log.fine("Fetching resource " + resourceName);
		return Optional.ofNullable(Launcher.class.getResourceAsStream(resourceName));
	}
}
