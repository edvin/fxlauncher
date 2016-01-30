package fxlauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.xml.bind.JAXB;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class Launcher extends Application {
    private static FXManifest manifest;
    private static Application app;
    private static boolean offline = false;

    public void start(Stage primaryStage) throws Exception {
	    ProgressBar progressBar = new ProgressBar();
        progressBar.setStyle(manifest.progressBarStyle);

        Label label = new Label(manifest.updateText);
        label.setStyle(manifest.updateLabelStyle);

        VBox wrapper = new VBox(label, progressBar);
	    wrapper.setStyle(manifest.wrapperStyle);

        Scene scene = new Scene(wrapper);
	    primaryStage.initStyle(manifest.stageStyle);
        primaryStage.setScene(scene);
        primaryStage.setTitle(manifest.name);
        primaryStage.show();

        Task sync = sync();

        progressBar.progressProperty().bind(sync.progressProperty());

        sync.setOnSucceeded(e -> {
            try {
                app = launch(primaryStage);
            } catch (Exception initError) {
                reportError("Launch", initError);
            }
        });

        sync.setOnFailed(e -> reportError("Sync", sync.getException()));
        new Thread(sync).start();
    }


    public URLClassLoader createClassLoader() {
        List<URL> libs = manifest.files.stream().map(LibraryFile::toURL).collect(Collectors.toList());
        return new URLClassLoader(libs.toArray(new URL[libs.size()]));
    }

    public Application launch(Stage primaryStage) throws Exception {
        URLClassLoader classLoader = createClassLoader();
        Class<? extends Application> appclass = (Class<? extends Application>) classLoader.loadClass(manifest.launchClass);
        Thread.currentThread().setContextClassLoader(classLoader);
        Application app = appclass.newInstance();
        app.init();
        app.start(primaryStage);
        return app;
    }


    public Task sync() throws IOException {
        return new Task() {
            protected Object call() throws Exception {
                if (offline)
                    return null;

                List<LibraryFile> needsUpdate = manifest.files.stream().filter(LibraryFile::needsUpdate).collect(Collectors.toList());
                Long totalBytes = needsUpdate.stream().mapToLong(f -> f.size).sum();
                Long totalWritten = 0L;

                for (LibraryFile lib : needsUpdate) {
                    updateMessage(lib.file.concat("..."));

                    Path target = Paths.get(lib.file).toAbsolutePath();
                    Files.createDirectories(target.getParent());

                    try (InputStream input = manifest.uri.resolve(lib.file).toURL().openStream();
                         OutputStream output = Files.newOutputStream(target)) {

                        byte[] buf = new byte[65536];

                        int read;
                        while ((read = input.read(buf)) > -1) {
                            output.write(buf, 0, read);
                            totalWritten += read;
                            updateProgress(totalWritten, totalBytes);
                        }
                    }
                }

                try (ByteArrayOutputStream mfstream = new ByteArrayOutputStream()) {
                    JAXB.marshal(manifest, mfstream);

                    Path manifestPath = Paths.get("fxapp.xml");

                    byte[] data = mfstream.toByteArray();

                    if (Files.notExists(manifestPath) || !Arrays.equals(Files.readAllBytes(manifestPath), data))
                        Files.write(manifestPath, data);
                }

                return null;
            }
        };
    }

    public void stop() throws Exception {
        if (app != null)
            app.stop();
    }

    private static void reportError(String job, Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(String.format(manifest.errorTitle, job));
        alert.setHeaderText(String.format(manifest.errorHeader, job));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        error.printStackTrace(writer);
        writer.close();
        alert.setContentText(out.toString());

        alert.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) throws Exception {
        // If URI given explicitly, load from there
        if (args.length > 0) {
            loadManifest(URI.create(args[0]).resolve(FXManifest.filename));
        } else {
            // If no uri given, load from manifest and try to reload from the uri given in the manifest
	        // If the local manifest doesn't exist, this is the best we can do.
	        try {
		        URI localURI = Paths.get(FXManifest.filename).toUri();
		        loadManifest(localURI);
	        } catch (Exception ex) {
		        manifest = new FXManifest();
		        manifest.errorHeader = String.format("Could not locate %s and no manifest uri parameter was given", FXManifest.filename);
		        launch();
		        Platform.runLater(() -> reportError("Manifest load", ex));
		        return;
	        }

            try {
                loadManifest(manifest.getFXAppURI());
            } catch (Exception networkError) {
                networkError.printStackTrace();
                offline = true;
            }
        }

        launch();
    }

    private static void loadManifest(URI uri) {
        manifest = JAXB.unmarshal(uri, FXManifest.class);
    }
}
