package fxlauncher;

import com.sun.javafx.application.ParametersImpl;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CreateManifest {

    public static void main(String[] args) throws IOException {
        URI baseURI = URI.create(args[0]);
        String launchClass = args[1];
        Path appPath = Paths.get(args[2]);
        FXManifest manifest = create(baseURI, launchClass, appPath);

        if (args.length > 3) {
	        // Parse named parameters
	        List<String> rawParams = new ArrayList<>();
	        rawParams.addAll(Arrays.asList(args).subList(3, args.length));
	        ParametersImpl params = new ParametersImpl(rawParams);
	        Map<String, String> named = params.getNamed();

	        // Configure cacheDir
	        if (named != null && named.containsKey("cache-dir"))
		        manifest.cacheDir = named.get("cache-dir");

	        // Append the rest as manifest parameters
	        StringBuilder rest = new StringBuilder();
	        for (String raw : params.getRaw()) {
		        if (raw.startsWith("--cache-dir=")) continue;
		        if (rest.length() > 0) rest.append(" ");
		        rest.append(raw);
	        }

	        // Add the raw parameter string to the manifest
	        if (rest.length() > 0)
	            manifest.parameters = rest.toString();
        }

        JAXB.marshal(manifest, appPath.resolve("app.xml").toFile());
    }

    public static FXManifest create(URI baseURI, String launchClass, Path appPath) throws IOException {
        FXManifest manifest = new FXManifest();
        manifest.uri = baseURI;
        manifest.launchClass = launchClass;

        Files.walkFileTree(appPath, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isDirectory(file) && file.toString().endsWith(".jar") && !file.getFileName().toString().startsWith("fxlauncher"))
                    manifest.files.add(new LibraryFile(appPath, file));
                return FileVisitResult.CONTINUE;
            }
        });

        return manifest;
    }
    
}
