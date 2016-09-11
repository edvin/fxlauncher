package fxlauncher;

import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * The UIProvider is responsible for creating the loader screen and the updater screen.
 * A default implementation is available in the {@link DefaultUIProvider} class, but you
 * can provide a custom implementation to alter the appearance of the loader UI.
 *
 * Implement this interface and make sure to embed the classes inside the fxlauncher.jar
 * right around the "embed manifest" step. You have to do this manually as there is no function
 * in the plugin to support this yet. Basically you have to do the following two steps:
 *
 * 1. Copy the implementation classes into the fxlauncher.jar
 * 2. Create META-INF/services/fxlauncher.UIProvider inside the fxlauncher.jar. The content must
 * be a string with the fully qualified name of your implementation class.
 *
 * Typical example:
 *
 * <div>
 * # cd into directory with ui sources
 * jar uf fxlauncher.jar -C my/package/MyUIProvider.class
 * # cd into directory with META-INF folder
 * jar uf fxlauncher.jar -C META-INF/services/fxlauncher.UIProvider
 * </div>
 */
public interface UIProvider {

	/**
	 * Initialization method called before {@link #createLoader()}
	 * and {@link #createUpdater(FXManifest)}. This is a good place to add
	 * stylesheets and perform other configuration.
	 *
	 * @param stage The stage that will be used to contain the loader and updater.
	 */
	void init(Stage stage);

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
