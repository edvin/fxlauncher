package fxlauncher.model;

/**
 * Sentinel values that can be embedded in {@link fxlauncher.config.LauncherOption}
 * entries representing paths. Adding a suitable {@link fxlauncher.config.Resolver}
 * should replace the string-equivalents of these values with the
 * appropriate Operating-System-specific paths.
 * 
 * @author idavis1
 */
public enum GenericPathLabel {
	USERLIB,
	ALLUSERS,
}
