package fxlauncher;

import javafx.application.Application;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public abstract class AbstractLauncher<APP>  {
    private static final Logger log = Logger.getLogger("AbstractLauncher");

    protected static FXManifest manifest;
    private String phase;

    /**
     * Make java.util.logger log to a file. Default it will log to $TMPDIR/fxlauncher.log. This can be overriden by using
     * comman line parameter <code>--logfile=logfile</code>
     *
     * @throws IOException
     */
    protected void setupLogFile() throws IOException {
        String filename = System.getProperty("java.io.tmpdir") + File.separator + "fxlauncher.log";
        if (getParameters().getNamed().containsKey("logfile"))
            filename = getParameters().getNamed().get("logfile");
        System.out.println("logging to " + filename);
        FileHandler handler = new FileHandler(filename);
        handler.setFormatter(new SimpleFormatter());
        log.addHandler(handler);
    }

    /**
     * Check if the SSL connection needs to ignore the validity of the ssl certificate.
     *
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     */
    protected void checkSSLIgnoreflag() throws KeyManagementException, NoSuchAlgorithmException {
        if (getParameters().getUnnamed().contains("--ignoressl")) {
            setupIgnoreSSLCertificate();
        }
    }

    protected ClassLoader createClassLoader(Path cacheDir) {
        List<URL> libs = manifest.files.stream().filter(LibraryFile::loadForCurrentPlatform).map(it -> it.toURL(cacheDir)).collect(Collectors.toList());

        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        if (systemClassLoader instanceof FxlauncherClassCloader)
        {
            ((FxlauncherClassCloader) systemClassLoader).addUrls(libs);
            return systemClassLoader;
        }
        else
        {
            ClassLoader classLoader = new URLClassLoader(libs.toArray(new URL[libs.size()]));
            Thread.currentThread().setContextClassLoader(classLoader);

            setupClassLoader(classLoader);

            return classLoader;
        }
    }

    protected void updateManifest() throws Exception {
        phase = "Update Manifest";
        syncManifest();
    }

    /**
     * Check if remote files are newer then local files. Return true if files are updated, triggering the whatsnew option else false.
     * Also return false and do not check for updates if the <code>--offline</code> commandline argument is set.
     *
     * @return true if new files have been downloaded, false otherwise.
     * @throws Exception
     */
    protected boolean syncFiles() throws Exception {

        Path cacheDir = manifest.resolveCacheDir(getParameters().getNamed());
        log.info(String.format("Using cache dir %s", cacheDir));

        phase = "File Synchronization";

        if (getParameters().getUnnamed().contains("--offline")) {
            log.info("not updating files from remote, offline selected");
            return false; // to signal that nothing has changed.
        }
        List<LibraryFile> needsUpdate = manifest.files.stream()
                .filter(LibraryFile::loadForCurrentPlatform)
                .filter(it -> it.needsUpdate(cacheDir))
                .collect(Collectors.toList());

        if (needsUpdate.isEmpty())
            return false;

        Long totalBytes = needsUpdate.stream().mapToLong(f -> f.size).sum();
        Long totalWritten = 0L;

        for (LibraryFile lib : needsUpdate) {
            Path target = cacheDir.resolve(lib.file).toAbsolutePath();
            Files.createDirectories(target.getParent());

            URI uri = manifest.uri.resolve(lib.file);

            try (InputStream input = openDownloadStream(uri); OutputStream output = Files.newOutputStream(target)) {

                byte[] buf = new byte[65536];

                int read;
                while ((read = input.read(buf)) > -1) {
                    output.write(buf, 0, read);
                    totalWritten += read;
                    Double progress = totalWritten.doubleValue() / totalBytes.doubleValue();
                    updateProgress(progress);
                }
            }
        }
        return true;
    }

    private InputStream openDownloadStream(URI uri) throws IOException {
        if (uri.getScheme().equals("file")) return Files.newInputStream(new File(uri.getPath()).toPath());

        URLConnection connection = uri.toURL().openConnection();
        if (uri.getUserInfo() != null) {
            byte[] payload = uri.getUserInfo().getBytes(StandardCharsets.UTF_8);
            String encoded = Base64.getEncoder().encodeToString(payload);
            connection.setRequestProperty("Authorization", String.format("Basic %s", encoded));
        }
        return connection.getInputStream();
    }

    protected void createApplicationEnvironment() throws Exception {
        phase = "Create Application";

        if (manifest == null)
            throw new IllegalArgumentException("Unable to retrieve embedded or remote manifest.");
        List<String> preloadLibs = manifest.getPreloadNativeLibraryList();
        for (String preloadLib : preloadLibs)
            System.loadLibrary(preloadLib);

        Path cacheDir = manifest.resolveCacheDir(getParameters() != null ? getParameters().getNamed() : null);

        ClassLoader classLoader = createClassLoader(cacheDir);
        Class<APP> appclass = (Class<APP>) classLoader.loadClass(manifest.launchClass);

        createApplication(appclass);
    }

    protected void syncManifest() throws Exception {
        Map<String, String> namedParams = getParameters().getNamed();

        String appStr = null;

        if (namedParams.containsKey("app")) {
            // get --app-param
            appStr = namedParams.get("app");
            log.info(String.format("Loading manifest from 'app' parameter supplied: %s", appStr));
        }

        if (namedParams.containsKey("uri")) {
            // get --uri-param
            String uriStr = namedParams.get("uri");
            if (!uriStr.endsWith("/")) {
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

        if (appStr != null) {
            // --uri was not supplied, but --app was, so load manifest from that
            manifest = FXManifest.load(new File(appStr).toURI());
            return;
        }

        URL embeddedManifest = AbstractLauncher.class.getResource("/app.xml");
        manifest = JAXB.unmarshal(embeddedManifest, FXManifest.class);

        Path cacheDir = manifest.resolveCacheDir(namedParams);
        Path manifestPath = manifest.getPath(cacheDir);

        if (Files.exists(manifestPath))
            manifest = JAXB.unmarshal(manifestPath.toFile(), FXManifest.class);

        if (getParameters().getUnnamed().contains("--offline")) {
            log.info("offline selected");
            return;
        }
        try {
            FXManifest remoteManifest = FXManifest.load(manifest.getFXAppURI());

            if (remoteManifest == null) {
                log.info(String.format("No remote manifest at %s", manifest.getFXAppURI()));
            } else if (!remoteManifest.equals(manifest)) {
                // Update to remote manifest if newer or we specifically accept downgrades
                if (remoteManifest.isNewerThan(manifest) || manifest.acceptDowngrade) {
                    manifest = remoteManifest;
                    JAXB.marshal(manifest, manifestPath.toFile());
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, String.format("Unable to update manifest from %s", manifest.getFXAppURI()), ex);
        }
    }

    protected void setupIgnoreSSLCertificate() throws NoSuchAlgorithmException, KeyManagementException {
        log.info("starting ssl setup");
        TrustManager[] trustManager = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }};
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManager, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    protected boolean checkIgnoreUpdateErrorSetting() {
        return getParameters().getUnnamed().contains("--stopOnUpdateErrors");
    }
    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public FXManifest getManifest() {
        return manifest;
    }

    protected abstract Application.Parameters getParameters();
    protected abstract void updateProgress(double progress);
    protected abstract void createApplication(Class<APP> appClass);
    protected abstract void reportError(String title, Throwable error);
    protected abstract void setupClassLoader(ClassLoader classLoader);
}
