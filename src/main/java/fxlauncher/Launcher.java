package fxlauncher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Launcher extends Application {
	private static final Logger log = Logger.getLogger("Launcher");

	private Application app;
	private Stage primaryStage;
	private Stage stage;
	private UIProvider uiProvider;
	private StackPane root;

	private final AbstractLauncher<Application> superLauncher = new AbstractLauncher<Application>() {
		@Override
		protected Parameters getParameters() {
			return Launcher.this.getParameters();
		}

		@Override
		protected void updateProgress(double progress) {
			Platform.runLater(() -> uiProvider.updateProgress(progress));
		}

		@Override
		protected void createApplication(Class<Application> appClass) {
			runAndWait(() -> {
				try {
					if (Application.class.isAssignableFrom(appClass)) {
						app = appClass.newInstance();
					} else {
						throw new IllegalArgumentException(
								String.format(Constants.getString("Error.Application.Create.1"), appClass));
					}
				} catch (Throwable t) {
					reportError(Constants.getString("Error.Application.Create.2"), t);
				}
			});
		}

		@Override
		protected void reportError(String title, Throwable error) {
			log.log(Level.WARNING, title, error);

			Platform.runLater(() -> {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle(title);
				alert.setHeaderText(String.format(Constants.getString("Error.Alert.Header"), title,
						System.getProperty("java.io.tmpdir")));
				alert.getDialogPane().setPrefWidth(600);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintWriter writer = new PrintWriter(out);
				error.printStackTrace(writer);
				writer.close();
				TextArea text = new TextArea(out.toString());
				alert.getDialogPane().setContent(text);

				alert.showAndWait();
				Platform.exit();
			});
		}

		@Override
		protected void setupClassLoader(ClassLoader classLoader) {
			FXMLLoader.setDefaultClassLoader(classLoader);
			Platform.runLater(() -> Thread.currentThread().setContextClassLoader(classLoader));
		}

	};

	/**
	 * Check if a new version is available and return the manifest for the new
	 * version or null if no update.
	 * <p>
	 * Note that updates will only be detected if the application was actually
	 * launched with FXLauncher.
	 *
	 * @return The manifest for the new version if available
	 */
	public static FXManifest checkForUpdate() throws IOException {
		// We might be called even when FXLauncher wasn't used to start the application
		if (AbstractLauncher.manifest == null)
			return null;
		FXManifest manifest = FXManifest.load(URI.create(AbstractLauncher.manifest.uri + "/app.xml"));
		return manifest.equals(AbstractLauncher.manifest) ? null : manifest;
	}

	/**
	 * Initialize the UI Provider by looking for an UIProvider inside the launcher
	 * or fallback to the default UI.
	 * <p>
	 * A custom implementation must be embedded inside the launcher jar, and
	 * /META-INF/services/fxlauncher.UIProvider must point to the new implementation
	 * class.
	 * <p>
	 * You must do this manually/in your build right around the "embed manifest"
	 * step.
	 */
	public void init() throws Exception {
		Iterator<UIProvider> providers = ServiceLoader.load(UIProvider.class).iterator();
		uiProvider = providers.hasNext() ? providers.next() : new DefaultUIProvider();
	}

	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		stage = new Stage(StageStyle.UNDECORATED);
		root = new StackPane();
		final boolean[] filesUpdated = new boolean[1];

		Scene scene = new Scene(root);
		stage.setScene(scene);

		superLauncher.setupLogFile();
		superLauncher.checkSSLIgnoreflag();
		this.uiProvider.init(stage);
		root.getChildren().add(uiProvider.createLoader());

		stage.show();

		new Thread(() -> {
			Thread.currentThread().setName("FXLauncher-Thread");
			try {
				superLauncher.updateManifest();
				createUpdateWrapper();
				filesUpdated[0] = superLauncher.syncFiles();
			} catch (Exception ex) {
				log.log(Level.WARNING,
						String.format(Constants.getString("Error.Start.Phase"), superLauncher.getPhase()), ex);
				if (superLauncher.checkIgnoreUpdateErrorSetting()) {
					superLauncher.reportError(
							String.format(Constants.getString("Error.Start.Phase"), superLauncher.getPhase()), ex);
					System.exit(1);
				}
			}

			try {
				superLauncher.createApplicationEnvironment();
				launchAppFromManifest(filesUpdated[0]);
			} catch (Exception ex) {
				superLauncher.reportError(
						String.format(Constants.getString("Error.Start.Phase"), superLauncher.getPhase()), ex);
			}

		}).start();
	}

	private void launchAppFromManifest(boolean showWhatsnew) throws Exception {
		superLauncher.setPhase(Constants.getString("Application.Phase.Prepare"));

		try {
			initApplication();
		} catch (Throwable ex) {
			superLauncher.reportError(Constants.getString("Error.Application.Init"), ex);
		}
		superLauncher.setPhase(Constants.getString("Application.Phase.Start"));
		log.info(() -> Constants.getString("Whatsnew.Log") + showWhatsnew);

		runAndWait(() -> {
			try {
				if (showWhatsnew && superLauncher.getManifest().whatsNewPage != null)
					showWhatsNewDialog(superLauncher.getManifest().whatsNewPage);

				// Lingering update screen will close when primary stage is shown
				if (superLauncher.getManifest().lingeringUpdateScreen) {
					primaryStage.showingProperty().addListener(observable -> {
						if (stage.isShowing())
							stage.close();
					});
				} else {
					stage.close();
				}

				startApplication();
			} catch (Throwable ex) {
				superLauncher.reportError(Constants.getString("Error.Application.Start"), ex);
			}
		});
	}

	private void showWhatsNewDialog(String whatsNewURL) {
		WebView view = new WebView();
		view.getEngine().load(whatsNewURL);
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(Constants.getString("Whatsnew.Title"));
		alert.setHeaderText(Constants.getString("Whatsnew.Header"));
		alert.getDialogPane().setContent(view);
		alert.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void createUpdateWrapper() {
		superLauncher.setPhase(Constants.getString("Application.Phase.Wrapper"));

		Platform.runLater(() -> {
			Parent updater = uiProvider.createUpdater(superLauncher.getManifest());
			root.getChildren().clear();
			root.getChildren().add(updater);
		});
	}

	public void stop() throws Exception {
		if (app != null)
			app.stop();
	}

	private void initApplication() throws Exception {
		if (app != null) {
			app.init();
		}
	}

	private void startApplication() throws Exception {
		if (app != null) {
			Parameters appparams = app.getParameters();
			// check if app has parameters
			if (appparams != null) {
				final LauncherParams params = new LauncherParams(getParameters(), superLauncher.getManifest());
				appparams.getNamed().putAll(params.getNamed());
				appparams.getRaw().addAll(params.getRaw());
				appparams.getUnnamed().addAll(params.getUnnamed());
			}
			PlatformImpl.setApplicationName(app.getClass());
			superLauncher.setPhase(Constants.getString("Application.Phase.Init"));
			app.start(primaryStage);
		} else {
			// Start any executable jar (i.E. Spring Boot);
			String firstFile = superLauncher.getManifest().files.get(0).file;
			log.info(() -> String.format(Constants.getString("Application.log.Noappclass"), firstFile));
			Path cacheDir = superLauncher.getManifest().resolveCacheDir(getParameters().getNamed());
			String command = String.format("java -jar %s/%s", cacheDir.toAbsolutePath(), firstFile);
			log.info(() -> String.format(Constants.getString("Application.log.Execute"), command));
			Runtime.getRuntime().exec(command);
		}
	}

	/**
	 * Runs the specified {@link Runnable} on the JavaFX application thread and
	 * waits for completion.
	 *
	 * @param action the {@link Runnable} to run
	 * @throws NullPointerException if {@code action} is {@code null}
	 */
	void runAndWait(Runnable action) {
		if (action == null)
			throw new NullPointerException("action");

		// run synchronously on JavaFX thread
		if (Platform.isFxApplicationThread()) {
			action.run();
			return;
		}

		// queue on JavaFX thread and wait for completion
		final CountDownLatch doneLatch = new CountDownLatch(1);
		Platform.runLater(() -> {
			try {
				action.run();
			} finally {
				doneLatch.countDown();
			}
		});

		try {
			doneLatch.await();
		} catch (InterruptedException e) {
			// ignore exception
		}
	}
}
