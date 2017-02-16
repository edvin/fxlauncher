package fxlauncher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXB;

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

@SuppressWarnings("unchecked")
public class Launcher extends Application
{
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
    public void init() throws Exception
    {
        Iterator<UIProvider> providers = ServiceLoader.load(UIProvider.class).iterator();
        uiProvider = providers.hasNext() ? providers.next() : new DefaultUIProvider();
    }

    public void start(Stage primaryStage) throws Exception
    {
        this.primaryStage = primaryStage;
        stage = new Stage(StageStyle.UNDECORATED);
        root = new StackPane();
        final boolean[] filesUpdated = new boolean[1];
        Scene scene = new Scene(root);
        stage.setScene(scene);

        this.uiProvider.init(stage);
        root.getChildren().add(uiProvider.createLoader());

        stage.show();

        new Thread(() ->
        {
            Thread.currentThread().setName("FXLauncher-Thread");
            try
            {
                updateManifest();
                createUpdateWrapper();
                Path cacheDir = manifest.resolveCacheDir(getParameters().getNamed());
                log.info(String.format("Using cache dir %s", cacheDir));
                filesUpdated[0] = syncFiles(cacheDir);
            }
            catch (Exception ex)
            {
                log.log(Level.WARNING, String.format("Error during %s phase", phase), ex);
            }

            try
            {
                //                if (filesUpdated[0] && !manifest.whatsNewPage.isEmpty())
                //                {
                //                    showWhatsNewDialog(manifest.whatsNewPage);
                //                }
                createApplication();
                launchAppFromManifest(filesUpdated[0]);
            }
            catch (Exception ex)
            {
                reportError(String.format("Error during %s phase", phase), ex);
            }

        }).start();
    }

    private void showWhatsNewDialog(String whatsNewPage)
    {
            WebView view = new WebView();
            view.getEngine().load(Launcher.class.getResource(whatsNewPage).toExternalForm());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("What's new");
            alert.setHeaderText("New in this update");
            alert.getDialogPane().setContent(view);
            alert.showAndWait();
    }

    public static void main(String[] args)
    {
        launch(args);
    }

    private void createUpdateWrapper()
    {
        phase = "Update Wrapper Creation";

        Platform.runLater(() ->
        {
            Parent updater = uiProvider.createUpdater(manifest);
            root.getChildren().clear();
            root.getChildren().add(updater);
        });
    }

    private URLClassLoader createClassLoader(Path cacheDir)
    {
        List<URL> libs = manifest.files.stream().filter(LibraryFile::loadForCurrentPlatform).map(it -> it.toURL(cacheDir)).collect(Collectors.toList());

        return new URLClassLoader(libs.toArray(new URL[libs.size()]));
    }

    private void launchAppFromManifest(boolean showWhatshew) throws Exception
    {
        phase = "Application Init";
        app.init();
        phase = "Application Start";
        log.info("show whats new dialog?"+showWhatshew);
        PlatformImpl.runAndWait(() ->
        {
            try
            {
                if(showWhatshew) showWhatsNewDialog(manifest.whatsNewPage);
                primaryStage.showingProperty().addListener(observable ->
                {
                    if (stage.isShowing())
                        stage.close();
                });
                app.start(primaryStage);
            }
            catch (Exception ex)
            {
                reportError("Failed to start application", ex);
            }
        });
    }

    private void updateManifest() throws Exception
    {
        phase = "Update Manifest";
        syncManifest();
    }

    private boolean syncFiles(Path cacheDir) throws Exception
    {
        phase = "File Synchronization";

        List<LibraryFile>
            needsUpdate =
            manifest.files.stream().filter(LibraryFile::loadForCurrentPlatform).filter(it -> it.needsUpdate(cacheDir)).collect(Collectors.toList());

        if (needsUpdate.isEmpty())
            return false;
        Long totalBytes = needsUpdate.stream().mapToLong(f -> f.size).sum();
        Long totalWritten = 0L;

        for (LibraryFile lib : needsUpdate)
        {
            Path target = cacheDir.resolve(lib.file).toAbsolutePath();
            Files.createDirectories(target.getParent());

            URI uri = manifest.uri.resolve(lib.file);
            URLConnection connection = uri.toURL().openConnection();
            if (uri.getUserInfo() != null)
            {
                byte[] payload = uri.getUserInfo().getBytes(StandardCharsets.UTF_8);
                String encoded = Base64.getEncoder().encodeToString(payload);
                connection.setRequestProperty("Authorization", String.format("Basic %s", encoded));
            }
            try (InputStream input = connection.getInputStream(); OutputStream output = Files.newOutputStream(target))
            {

                byte[] buf = new byte[65536];

                int read;
                while ((read = input.read(buf)) > -1)
                {
                    output.write(buf, 0, read);
                    totalWritten += read;
                    Double progress = totalWritten.doubleValue() / totalBytes.doubleValue();
                    Platform.runLater(() -> uiProvider.updateProgress(progress));
                }
            }
        }
        return true;
    }

    private void createApplication() throws Exception
    {
        phase = "Create Application";

        if (manifest == null)
            throw new IllegalArgumentException("Unable to retrieve embedded or remote manifest.");
        List<String> preloadLibs = manifest.getPreloadNativeLibraryList();
        for (String preloadLib : preloadLibs)
            System.loadLibrary(preloadLib);

        Path cacheDir = manifest.resolveCacheDir(getParameters() != null ? getParameters().getNamed() : null);

        URLClassLoader classLoader = createClassLoader(cacheDir);
        FXMLLoader.setDefaultClassLoader(classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        Platform.runLater(() -> Thread.currentThread().setContextClassLoader(classLoader));
        Class<? extends Application> appclass = (Class<? extends Application>)classLoader.loadClass(manifest.launchClass);

        PlatformImpl.runAndWait(() ->
        {
            try
            {
                app = appclass.newInstance();
                ParametersImpl.registerParameters(app, new LauncherParams(getParameters(), manifest));
                PlatformImpl.setApplicationName(appclass);
            }
            catch (Throwable t)
            {
                reportError("Error creating app class", t);
            }
        });
    }

    public void stop() throws Exception
    {
        if (app != null)
            app.stop();
    }

    private void reportError(String title, Throwable error)
    {
        log.log(Level.WARNING, title, error);

        Platform.runLater(() ->
        {
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

    private void syncManifest() throws Exception
    {
        Map<String, String> namedParams = getParameters().getNamed();

        if (getParameters().getUnnamed().contains("--ignoressl"))
        {
            setupIgnoreSSLCertificate();
        }
        String appStr = null;

        if (namedParams.containsKey("app"))
        {
            // get --app-param
            appStr = namedParams.get("app");
            log.info(String.format("Loading manifest from 'app' parameter supplied: %s", appStr));
        }

        if (namedParams.containsKey("uri"))
        {
            // get --uri-param
            String uriStr = namedParams.get("uri");
            if (!uriStr.endsWith("/"))
            {
                uriStr = uriStr + "/";
            }
            log.info(String.format("Syncing files from 'uri' parameter supplied:  %s", uriStr));

            URI uri = URI.create(uriStr);
            // load manifest from --app param if supplied, else default file at supplied uri
            URI app = appStr != null ? URI.create(appStr) : uri.resolve("app.xml");
            manifest = FXManifest.load(app);
            // set supplied uri in manifest
            manifest.uri = uri;
            return;
        }

        if (appStr != null)
        {
            // --uri was not supplied, but --app was, so load manifest from that
            manifest = FXManifest.load(new File(appStr).toURI());
            return;
        }

        URL embeddedManifest = Launcher.class.getResource("/app.xml");
        manifest = JAXB.unmarshal(embeddedManifest, FXManifest.class);

        Path cacheDir = manifest.resolveCacheDir(namedParams);
        Path manifestPath = manifest.getPath(cacheDir);

        if (Files.exists(manifestPath))
            manifest = JAXB.unmarshal(manifestPath.toFile(), FXManifest.class);

        try
        {
            FXManifest remoteManifest = FXManifest.load(manifest.getFXAppURI());

            if (remoteManifest == null)
            {
                log.info(String.format("No remote manifest at %s", manifest.getFXAppURI()));
            }
            else if (!remoteManifest.equals(manifest))
            {
                // Update to remote manifest if newer or we specifically accept downgrades
                if (remoteManifest.isNewerThan(manifest) || manifest.acceptDowngrade)
                {
                    manifest = remoteManifest;
                    JAXB.marshal(manifest, manifestPath.toFile());
                }
            }
        }
        catch (Exception ex)
        {
            log.log(Level.WARNING, String.format("Unable to update manifest from %s", manifest.getFXAppURI()), ex);
        }
    }

    private void setupIgnoreSSLCertificate() throws NoSuchAlgorithmException, KeyManagementException
    {
        log.info("starting ssl setup");
        TrustManager[] trustManager = new TrustManager[]{
            new X509TrustManager()
            {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
                {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
                {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
            } };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManager, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }
}
