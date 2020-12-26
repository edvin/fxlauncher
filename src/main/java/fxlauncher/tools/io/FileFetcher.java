package fxlauncher.tools.io;

import java.io.InputStream;
import java.util.Optional;

import fxlauncher.except.FXLauncherException;

/**
 * Standardized interface for fetching resources via I/O
 *
 * @author idavis1
 *
 */
public interface FileFetcher {
	public Optional<InputStream> fetch() throws FXLauncherException;
}
