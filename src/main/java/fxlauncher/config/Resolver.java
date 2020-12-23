package fxlauncher.config;

import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import fxlauncher.model.GenericPathLabel;
import fxlauncher.model.OS;

@FunctionalInterface
/**
 * Resolver performs a transformation on a String
 * 
 * example: {@link LauncherOption} instance {@link CACHE_DIR} can accept
 * two sentinel values, ALLUSERS and USERLIB, which should be resolved to
 * OS-specific appropriate paths when accessed
 * 
 * @author idavis1
 */
public interface Resolver extends UnaryOperator<String>{
	
	static final Logger log = Logger.getLogger(Resolver.class.getName());

	public static Resolver DEFAULT = unresolved -> unresolved;
	
	public static final Resolver CACHE_DIR = unresolved -> {
		if(unresolved == null) return unresolved;
		log.finer("Attempting resolution of option CACHE_DIR");
		log.finer(String.format("Unresolved value: '%s'", unresolved));
		String resolved = unresolved; // assume no change, then check if change is necessary

		for(GenericPathLabel genericPath : GenericPathLabel.values()) {
			if(unresolved.startsWith(genericPath.name())) {
				String trimmed = unresolved.replace(genericPath.name() + "/", "");
				resolved = OS.current.getGenericPath(genericPath).resolve(trimmed).toString();
			}
		}
		
		log.finer(String.format("Returning resolved value of CACHE_DIR: '%s'", unresolved));
		return resolved;	
	};
}
