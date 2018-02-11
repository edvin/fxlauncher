package fxlauncher;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class JFXPanelSwingLauncher extends HeadlessMainLauncher {
    private static final Logger log = Logger.getLogger("JFXPanelSwingLauncher");

    private static JFrame frame;
    private static ProgressIndicator progressIndicator;

    public JFXPanelSwingLauncher(LauncherParams parameters) {
        super(parameters);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
        {
            initAndShowGUI();
            List<String> mainArgs = Arrays.asList(args);
            LauncherParams parameters = new LauncherParams(mainArgs);
            JFXPanelSwingLauncher jfxPanelLauncher = new JFXPanelSwingLauncher(parameters);
            try {
                showDialog();
                jfxPanelLauncher.process();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeDialog();
            }
        });

    }

    @Override
    protected void updateProgress(double progress)
    {
        super.updateProgress(progress);
        progressIndicator.setProgress(progress);
        if (progress == 1) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore
            }
            showDialog();
        }
    }

    @Override
    protected void reportError(String title, Throwable error) {
        super.reportError(title, error);
        JOptionPane.showMessageDialog(frame, error.getMessage(), title, JOptionPane.ERROR_MESSAGE);
        error.printStackTrace();
    }

    private static void initAndShowGUI()
    {
        final JFXPanel fxPanel = new JFXPanel();
        frame = new JFrame();
        frame.add(fxPanel);
        frame.setSize(300, 200);
        frame.setUndecorated(true);

        frame.setBackground(new java.awt.Color(0, 0, 0, 0));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Platform.runLater(() -> {
            Scene scene = createScene();
            fxPanel.setScene(scene);
        });
    }

    private static Scene createScene()
    {
        progressIndicator = new ProgressIndicator(-1);
        progressIndicator.setPrefSize(200, 200);
        VBox root = new VBox(progressIndicator);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 300, 300);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private static void closeDialog() {
        frame.dispose();
    }

    private static void showDialog() {
        frame.setVisible(true);
    }


}
