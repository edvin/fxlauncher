package fxlauncher;

import javafx.scene.Parent;

public interface UIProvider {
	/**
	 * Create the Node that will be displayed while the launcher is loading resources,
	 * before the update process starts. The default implementation is an intdeterminate
	 * progress indicator, but you can return any arbitrary scene graph.
	 *
	 * @return The launcher UI
	 */
	Parent createLoader();

	/**
	 * Create the Node that will be displayed while the launcher is updating resources.
	 *
	 * This Node should update it's display whenever the {@link #updateProgress(double)}
	 * method is called.
	 *
	 * @see #updateProgress(double)
	 * @return The updater Node
	 */
	Parent createUpdater(FXManifest manifest);

	/**
	 * Called when the update/download progress is changing. The progress is a value between
	 * 0 and 1, indicating the completion rate of the update process.
	 *
	 * @param progress A number between 0 and 1
	 */
	void updateProgress(double progress);
}
