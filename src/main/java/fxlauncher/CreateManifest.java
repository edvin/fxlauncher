package fxlauncher;

import com.sun.javafx.application.ParametersImpl;
import com.sun.javafx.fxml.builder.URLBuilder;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateManifest {
    private static ArrayList<String> includeExtensions = new ArrayList<>();

    static {
        includeExtensions.addAll(Arrays.asList("jar", "war"));
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI baseURI = URI.create(args[0]);
        String launchClass = args[1];
        Path appPath = Paths.get(args[2]);

        String cacheDir = null;
        String certDigest = null;
        Boolean acceptDowngrade = null;
        String parameters = null;
        String whatsNew = null;
        String preloadNativeLibraries = null;
        Boolean lingeringUpdateScreen = false;

        if (args.length > 3) {
            // Parse named parameters
            List<String> rawParams = new ArrayList<>();
            rawParams.addAll(Arrays.asList(args).subList(3, args.length));
            ParametersImpl params = new ParametersImpl(rawParams);
            Map<String, String> named = params.getNamed();

            if (named != null) {
                // Configure cacheDir
                if (named.containsKey("cache-dir"))
                    cacheDir = named.get("cache-dir");

                // Configure certDigest
                if (named.containsKey("cert-digest"))
                    certDigest = named.get("cert-digest");

                // Configure acceptDowngrade
                if (named.containsKey("accept-downgrade"))
                    acceptDowngrade = Boolean.valueOf(named.get("accept-downgrade"));

                // Configure preload native libraries
                if (named.containsKey("preload-native-libraries"))
                    preloadNativeLibraries = named.get("preload-native-libraries");

                // Should the update screen stay until the primary stage is shown?
                if (named.containsKey("lingering-update-screen"))
                    lingeringUpdateScreen = Boolean.valueOf(named.get("lingering-update-screen"));

                // Configure the whats-new option
                if (named.containsKey("whats-new"))
                    whatsNew = named.get("whats-new");

                // Add additional files with these extensions to manifest
                if (named.containsKey("include-extensions"))
                    includeExtensions.addAll(
                            Arrays.stream(named.get("include-extensions").split(","))
                                    .filter(s -> s != null && !s.isEmpty())
                                    .collect(Collectors.toList())
                    );
            }

            // Append the rest as manifest parameters
            StringBuilder rest = new StringBuilder();
            for (String raw : params.getRaw()) {
                if (raw.startsWith("--cache-dir=")) continue;
                if (raw.startsWith("--cert-digest=")) continue;
                if (raw.startsWith("--accept-downgrade=")) continue;
                if (raw.startsWith("--include-extensions=")) continue;
                if (raw.startsWith("--preload-native-libraries=")) continue;
                if (raw.startsWith("--whats-new")) continue;
                if (raw.startsWith("--lingering-update-screen")) continue;
                if (rest.length() > 0) rest.append(" ");
                rest.append(raw);
            }

            // Add the raw parameter string to the manifest
            if (rest.length() > 0)
                parameters = rest.toString();
        }

        FXManifest manifest = create(baseURI, launchClass, appPath);
        if (cacheDir != null) manifest.cacheDir = cacheDir;
        if (certDigest != null) manifest.certDigest = certDigest;
        if (acceptDowngrade != null) manifest.acceptDowngrade = acceptDowngrade;
        if (parameters != null) manifest.parameters = parameters;
        if (preloadNativeLibraries != null) manifest.preloadNativeLibraries = preloadNativeLibraries;
        if (whatsNew != null) manifest.whatsNewPage = whatsNew;
        manifest.lingeringUpdateScreen = lingeringUpdateScreen;
        JAXB.marshal(manifest, appPath.resolve("app.xml").toFile());
    }

    public static FXManifest create(URI baseURI, String launchClass, Path appPath) throws IOException, URISyntaxException {
        FXManifest manifest = new FXManifest();
        manifest.ts = System.currentTimeMillis();
        manifest.uri = baseURI;
        manifest.launchClass = launchClass;

        if(!manifest.uri.getPath().endsWith("/")) {
            manifest.uri = new URI(String.format("%s/", baseURI.toString()));
        }
        Files.walkFileTree(appPath, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isDirectory(file) && shouldIncludeInManifest(file) && !file.getFileName().toString().startsWith("fxlauncher"))
                    manifest.files.add(new LibraryFile(appPath, file));
                return FileVisitResult.CONTINUE;
            }
        });

        return manifest;
    }

    /**
     * Add the includeExtensions to the default list of "war" and "jar".
     *
     * Allthough the method is called setIncludeExtensions, it actually does an addAll.
     * @param includeExtensions
     */
    public static void setIncludeExtensions(List<String> includeExtensions) {
        CreateManifest.includeExtensions.addAll(includeExtensions);
    }

    private static boolean shouldIncludeInManifest(Path file) {
        String filename = file.getFileName().toString();
        for (String ext : includeExtensions) {
            if (filename.toLowerCase().endsWith(String.format(".%s", ext.toLowerCase()))) return true;
        }
        return false;
    }

}
