package fxlauncher.model.lifecycle;

/**
 * Interface that classes must implement if they want to be notified
 * when the current {@link LifecyclePhase} changes
 * @author idavis1
 */
public interface LifecycleListener {
	public void notifyListener();
}