package fxlauncher.config;

import java.util.function.Function;

import fxlauncher.model.GenericPathLabel;
import fxlauncher.model.OS;

public interface Resolver extends Function<String, String>{

	public static Resolver DEFAULT = string -> string;
	
	public static final Resolver CACHE_DIR = unresolved -> {
		if(unresolved == null) return unresolved;
		String resolved = unresolved; // assume no change, then check if change is necessary

		for(GenericPathLabel genericPath : GenericPathLabel.values()) {
			if(unresolved.startsWith(genericPath.name())) {
				String trimmed = unresolved.replace(genericPath.name() + "/", "");
				resolved = OS.current.getGenericPath(genericPath).resolve(trimmed).toString();
			}
		}
		
		return resolved;	
	};
}
