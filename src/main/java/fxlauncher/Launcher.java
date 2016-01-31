package fxlauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.xml.bind.JAXB;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class Launcher extends Application {
	private static final Logger log = Logger.getLogger("Launcher");

	private FXManifest manifest;
	private Application app;
	private StackPane root;
	private Stage primaryStage;
	private ProgressBar progressBar;
	private Stage stage;
	private String phase;

	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;

		root = new StackPane(new ProgressIndicator());
		root.setPrefSize(200, 80);
		root.setPadding(new Insets(10));

		stage = new Stage(StageStyle.UNDECORATED);

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();

		new Thread(() -> {
			try {
				updateManifest();
				createUpdateWrapper();
				syncFiles();
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
			stage.setTitle(manifest.name);

			progressBar = new ProgressBar();
			progressBar.setStyle(manifest.progressBarStyle);

			Label label = new Label(manifest.updateText);
			label.setStyle(manifest.updateLabelStyle);

			VBox wrapper = new VBox(label, progressBar);
			wrapper.setStyle(manifest.wrapperStyle);

			root.getChildren().clear();
			root.getChildren().add(wrapper);
		});
	}


	public URLClassLoader createClassLoader() {
		List<URL> libs = manifest.files.stream().map(LibraryFile::toURL).collect(Collectors.toList());
		return new URLClassLoader(libs.toArray(new URL[libs.size()]));
	}

	private void launchAppFromManifest() throws Exception {
		phase = "Application Init";
		app.init();
		phase = "Application Start";
		Platform.runLater(() -> {
			try {
				stage.close();
				app.start(primaryStage);
			} catch (Exception ex) {
				reportError("Failed to start application", ex);
			}
		});
	}

	private void updateManifest() throws Exception {
		phase = "Update Manifest";

		List<String> params = getParameters().getRaw();

		if (!params.isEmpty())
			updateManifest(params.get(0));
		else
			syncManifest();

		loadManifest(getLocalPath().toUri());
	}

	private void syncFiles() throws Exception {
		phase = "File Synchronization";

		List<LibraryFile> needsUpdate = manifest.files.stream().filter(LibraryFile::needsUpdate).collect(Collectors.toList());
		Long totalBytes = needsUpdate.stream().mapToLong(f -> f.size).sum();
		Long totalWritten = 0L;

		for (LibraryFile lib : needsUpdate) {
			Path target = Paths.get(lib.file).toAbsolutePath();
			Files.createDirectories(target.getParent());

			try (InputStream input = manifest.uri.resolve(lib.file).toURL().openStream();
			     OutputStream output = Files.newOutputStream(target)) {

				byte[] buf = new byte[65536];

				int read;
				while ((read = input.read(buf)) > -1) {
					output.write(buf, 0, read);
					totalWritten += read;
					Double progress = totalWritten.doubleValue() / totalBytes.doubleValue();
					Platform.runLater(() -> progressBar.setProgress(progress));
				}
			}
		}
	}

	private void createApplication() throws Exception {
		phase = "Create Application";

		URLClassLoader classLoader = createClassLoader();
		Class<? extends Application> appclass = (Class<? extends Application>) classLoader.loadClass(manifest.launchClass);
		Thread.currentThread().setContextClassLoader(classLoader);
		app = appclass.newInstance();
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

	private static Path getLocalPath() {
		return Paths.get(FXManifest.filename);
	}

	private void syncManifest() throws Exception {
		if (!Files.exists(FXManifest.getPath()))
			throw new IllegalArgumentException(String.format("No %s in current directory", FXManifest.filename));

		manifest = FXManifest.load();

		try {
			byte[] remoteContent = toByteArray(manifest.getFXAppURI().toURL().openStream());
			byte[] localContent = Files.readAllBytes(FXManifest.getPath());

			if (!Arrays.equals(remoteContent, localContent))
				Files.write(FXManifest.getPath(), remoteContent, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Exception ex) {
			log.log(Level.WARNING, "Unable to update manifest", ex);
		}
	}

	private static byte[] toByteArray(InputStream is) throws IOException {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			int read;
			byte[] buf = new byte[16384];

			while ((read = is.read(buf, 0, buf.length)) != -1)
				buffer.write(buf, 0, read);

			buffer.flush();

			return buffer.toByteArray();
		}
	}

	/**
	 * Load manifest from the given uri and save it to the local filesystem
	 *
	 * @param uri The uri base path to fetch the manifest from
	 */
	private void updateManifest(String uri) {
		loadManifest(URI.create(uri));
		manifest.save();
	}

	private void loadManifest(URI uri) {
		manifest = JAXB.unmarshal(uri, FXManifest.class);
	}
}
