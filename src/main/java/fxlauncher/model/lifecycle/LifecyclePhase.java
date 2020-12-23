package fxlauncher.model.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Enumeration lists the phases of the default lifecycle of a
 * running FxLauncher instance, keeps track of the current phase, 
 * and notifies registered listeners when the phase changes
 * @author idavis1
 */
public enum LifecyclePhase {
	STARTUP,
	LOAD_EMBEDDED_CONFIG,
	PARSE_CLI_ARGS,
	LOAD_REMOTE_CONFIG,
	FETCH_EXPLICIT_MANIFEST,
	FETCH_CACHED_MANIFEST,
	FETCH_REMOTE_MANIFEST,
	WRITE_MANIFEST_TO_CACHE,
	SYNC_ARTIFACTS,
	PREPARE_APPLICATION_ENVIRONMENT,
	LOAD_SYSTEM_LIBS,
	CREATE_APPLICATION,
	APPLICATION_INIT,
	APPLICATION_START,
	;
	
	private static Logger log = Logger.getLogger(LifecyclePhase.class.getName());
	
	public static LifecyclePhase current = STARTUP;
	
	private List<LifecycleListener> enterPhaseListeners = new ArrayList<>();
	private List<LifecycleListener> exitPhaseListeners = new ArrayList<>();

	/**
	 * Move to a new current lifecycle phase and notify listeners
	 * @param phase the new current phase
	 */
	public static void setCurrent(LifecyclePhase phase) {
		LifecyclePhase.current.exitPhaseListeners.forEach(LifecycleListener::notifyListener);
		log.fine("beginning launcher lifecycle phase");
		current = phase;
		LifecyclePhase.current.enterPhaseListeners.forEach(LifecycleListener::notifyListener);
	}
	
	/**
	 * add a listener to be notified when entering this phase
	 * @param listener the {@link LifecycleListener} to add
	 */
	public void registerEnterListener(LifecycleListener listener) {
		this.enterPhaseListeners.add(listener);
	}
	
	/**
	 * add a listener to be notified when exiting this phase
	 * @param listener the {@link LifecycleListener} to add
	 */
	public void registerExitListener(LifecycleListener listener) {
		this.exitPhaseListeners.add(listener);
	}
}
