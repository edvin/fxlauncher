package fxlauncher;

import com.sun.javafx.application.ParametersImpl;
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

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class Launcher extends Application {
    private static final Logger log = Logger.getLogger("Launcher");

    private Application app;
    private Stage primaryStage;
    private Stage stage;
    private UIProvider uiProvider;
    private StackPane root;

    private final AbstractLauncher superLauncher = new AbstractLauncher<Application>() {
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
            PlatformImpl.runAndWait(() ->
            {
                try {
                    if (Application.class.isAssignableFrom(appClass)) {
                        app = appClass.newInstance();
                    }
                } catch (Throwable t) {
                    reportError("Error creating app class", t);
                }
            });
        }

        @Override
        protected void reportError(String title, Throwable error) {
            log.log(Level.WARNING, title, error);

            Platform.runLater(() ->
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(String.format("%s\ncheck the logfile 'fxlauncher.log, usually in the %s directory", title, System.getProperty("java.io.tmpdir")));
//            alert.setHeaderText(title+"\nCheck the logfile usually in the "+System.getProperty("java.io.tmpdir") + "directory");
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
     * Check if a new version is available and return the manifest for the new version or null if no update.
     * <p>
     * Note that updates will only be detected if the application was actually launched with FXLauncher.
     *
     * @return The manifest for the new version if available
     */
    public static FXManifest checkForUpdate() throws IOException {
        // We might be called even when FXLauncher wasn't used to start the application
        if (AbstractLauncher.manifest == null) return null;
        FXManifest manifest = FXManifest.load(URI.create(AbstractLauncher.manifest.uri + "/app.xml"));
        return manifest.equals(AbstractLauncher.manifest) ? null : manifest;
    }


    /**
     * Initialize the UI Provider by looking for an UIProvider inside the launcher
     * or fallback to the default UI.
     * <p>
     * A custom implementation must be embedded inside the launcher jar, and
     * /META-INF/services/fxlauncher.UIProvider must point to the new implementation class.
     * <p>
     * You must do this manually/in your build right around the "embed manifest" step.
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
                log.log(Level.WARNING, String.format("Error during %s phase", superLauncher.getPhase()), ex);
                if (superLauncher.checkIgnoreUpdateErrorSetting()) {
                    superLauncher.reportError(String.format("Error during %s phase", superLauncher.getPhase()), ex);
                    System.exit(1);
                }
            }

            try {
                superLauncher.createApplicationEnvironment();
                launchAppFromManifest(filesUpdated[0]);
            } catch (Exception ex) {
                superLauncher.reportError(String.format("Error during %s phase", superLauncher.getPhase()), ex);
            }

        }).start();
    }

    private void launchAppFromManifest(boolean showWhatsnew) throws Exception {
        superLauncher.setPhase("Application Environment Prepare");

        try {
            initApplication();
        } catch (Throwable ex) {
            superLauncher.reportError("Error during app init", ex);
        }
        superLauncher.setPhase("Application Start");
        log.info("Show whats new dialog? " + showWhatsnew);

        PlatformImpl.runAndWait(() ->
        {
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
                superLauncher.reportError("Failed to start application", ex);
            }
        });
    }

    private void showWhatsNewDialog(String whatsNewPage) {
        WebView view = new WebView();
        view.getEngine().load(Launcher.class.getResource(whatsNewPage).toExternalForm());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("What's new");
        alert.setHeaderText("New in this update");
        alert.getDialogPane().setContent(view);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void createUpdateWrapper() {
        superLauncher.setPhase("Update Wrapper Creation");

        Platform.runLater(() ->
        {
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
            ParametersImpl.registerParameters(app, new LauncherParams(getParameters(), superLauncher.getManifest()));
            PlatformImpl.setApplicationName(app.getClass());
            superLauncher.setPhase("Application Init");
            app.start(primaryStage);
        } else {
            // Start any executable jar (i.E. Spring Boot);
            List<LibraryFile> files = superLauncher.getManifest().files;
            String cacheDir = superLauncher.getManifest().cacheDir;
            String command = String.format("java -jar %s/%s", cacheDir, files.get(0).file);
            log.info(String.format("Execute command '%s'", command));
            Runtime.getRuntime().exec(command);
        }
    }
}
