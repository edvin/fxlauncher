package fxlauncher;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DefaultUIProvider implements UIProvider {
	private ProgressBar progressBar;

	public Parent createLoader() {
		StackPane root = new StackPane(new ProgressIndicator());
		root.setPrefSize(200, 80);
		root.setPadding(new Insets(10));
		return root;
	}

	public Parent createUpdater(FXManifest manifest) {
		progressBar = new ProgressBar();
		progressBar.setStyle(manifest.progressBarStyle);

		Label label = new Label(manifest.updateText);
		label.setStyle(manifest.updateLabelStyle);

		VBox wrapper = new VBox(label, progressBar);
		wrapper.setStyle(manifest.wrapperStyle);

		return wrapper;
	}

	public void updateProgress(double progress) {
		progressBar.setProgress(progress);
	}

	public void init(Stage stage) {

	}
}
