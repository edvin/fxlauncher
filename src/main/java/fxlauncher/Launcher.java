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
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class Launcher extends Application {
    private static final Logger log = Logger.getLogger("Launcher");

    private FXManifest manifest;
    private Application app;
    private Stage primaryStage;
    private Stage stage;
    private String phase;
    private UIProvider uiProvider;
    private StackPane root;

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
        Scene scene = new Scene(root);
        stage.setScene(scene);

        this.uiProvider.init(stage);
        root.getChildren().add(uiProvider.createLoader());

        stage.show();

        new Thread(() -> {
            Thread.currentThread().setName("FXLauncher-Thread");
            try {
                updateManifest();
                createUpdateWrapper();
                Path cacheDir = manifest.resolveCacheDir(getParameters().getNamed());
                log.info(String.format("Using cache dir %s", cacheDir));
                syncFiles(cacheDir);
            } catch (Exception ex) {
                log.log(Level.WARNING, String.format("Error during %s phase", phase), ex);
            }

            try {
                createApplication();
                launchAppFromManifest();
            } catch (Exception ex) {
                reportError(String.format("Error during %s phase", phase), ex);
            }

        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void createUpdateWrapper() {
        phase = "Update Wrapper Creation";

        Platform.runLater(() -> {
            Parent updater = uiProvider.createUpdater(manifest);
            root.getChildren().clear();
            root.getChildren().add(updater);
        });
    }

    private URLClassLoader createClassLoader(Path cacheDir) {
        List<URL> libs = manifest.files.stream()
                .filter(LibraryFile::loadForCurrentPlatform)
                .map(it -> it.toURL(cacheDir))
                .collect(Collectors.toList());

        return new URLClassLoader(libs.toArray(new URL[libs.size()]));
    }

    private void launchAppFromManifest() throws Exception {
        phase = "Application Init";
        app.init();
        phase = "Application Start";
        PlatformImpl.runAndWait(() -> {
            try {
                primaryStage.showingProperty().addListener(observable -> {
                    if (stage.isShowing()) stage.close();
                });
                app.start(primaryStage);
            } catch (Exception ex) {
                reportError("Failed to start application", ex);
            }
        });
    }

    private void updateManifest() throws Exception {
        phase = "Update Manifest";
        syncManifest();
    }

    private void syncFiles(Path cacheDir) throws Exception {
        phase = "File Synchronization";

        List<LibraryFile> needsUpdate = manifest.files.stream()
                .filter(LibraryFile::loadForCurrentPlatform)
                .filter(it -> it.needsUpdate(cacheDir))
                .collect(Collectors.toList());

        Long totalBytes = needsUpdate.stream().mapToLong(f -> f.size).sum();
        Long totalWritten = 0L;

        for (LibraryFile lib : needsUpdate) {
            Path target = cacheDir.resolve(lib.file).toAbsolutePath();
            Files.createDirectories(target.getParent());

            URI uri = manifest.uri.resolve(lib.file);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            if (uri.getUserInfo() != null) {
                byte[] payload = uri.getUserInfo().getBytes(StandardCharsets.UTF_8);
                String encoded = Base64.getEncoder().encodeToString(payload);
                connection.setRequestProperty("Authorization", String.format("Basic %s", encoded));
            }
            try (InputStream input = connection.getInputStream();
                 OutputStream output = Files.newOutputStream(target)) {

                byte[] buf = new byte[65536];

                int read;
                while ((read = input.read(buf)) > -1) {
                    output.write(buf, 0, read);
                    totalWritten += read;
                    Double progress = totalWritten.doubleValue() / totalBytes.doubleValue();
                    Platform.runLater(() -> uiProvider.updateProgress(progress));
                }
            }
        }
    }

    private void createApplication() throws Exception {
        phase = "Create Application";

        if (manifest == null) throw new IllegalArgumentException("Unable to retrieve embedded or remote manifest.");
        Path cacheDir = manifest.resolveCacheDir(getParameters() != null ? getParameters().getNamed() : null);

        URLClassLoader classLoader = createClassLoader(cacheDir);
        FXMLLoader.setDefaultClassLoader(classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        Platform.runLater(() -> Thread.currentThread().setContextClassLoader(classLoader));
        Class<? extends Application> appclass = (Class<? extends Application>) classLoader.loadClass(manifest.launchClass);

        PlatformImpl.runAndWait(() -> {
            try {
                app = appclass.newInstance();
                ParametersImpl.registerParameters(app, new LauncherParams(getParameters(), manifest));
                PlatformImpl.setApplicationName(appclass);
            } catch (Throwable t) {
                reportError("Error creating app class", t);
            }
        });
    }

    public void stop() throws Exception {
        if (app != null)
            app.stop();
    }

    private void reportError(String title, Throwable error) {
        log.log(Level.WARNING, title, error);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(title);
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

    private void syncManifest() throws Exception {
        Map<String, String> namedParams = getParameters().getNamed();

        if (namedParams.containsKey("app")) {
            String manifestURL = namedParams.get("app");
            log.info(String.format("Loading manifest from parameter supplied location %s", manifestURL));
            manifest = FXManifest.load(URI.create(manifestURL));
            return;
        }

        URL embeddedManifest = Launcher.class.getResource("/app.xml");
        manifest = JAXB.unmarshal(embeddedManifest, FXManifest.class);

        Path cacheDir = manifest.resolveCacheDir(namedParams);
        Path manifestPath = manifest.getPath(cacheDir);

        if (Files.exists(manifestPath))
            manifest = JAXB.unmarshal(manifestPath.toFile(), FXManifest.class);

        try {
            FXManifest remoteManifest = FXManifest.load(manifest.getFXAppURI());

            if (remoteManifest == null) {
                log.info(String.format("No remote manifest at %s", manifest.getFXAppURI()));
            } else if (!remoteManifest.equals(manifest)) {
                // Update to remote manifest if newer or we specifially accept downgrades
                if (remoteManifest.isNewerThan(manifest) || manifest.acceptDowngrade) {
                    manifest = remoteManifest;
                    JAXB.marshal(manifest, manifestPath.toFile());
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unable to update manifest", ex);
        }
    }

}
