package fxlauncher;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CreateManifest {

    public static void main(String[] args) throws IOException {
        URI baseURI = URI.create(args[0]);
        String launchClass = args[1];
        Path appPath = Paths.get(args[2]);
        String name = args[3];
        FXManifest manifest = create(baseURI, launchClass, appPath, name);
        JAXB.marshal(manifest, appPath.resolve(FXManifest.filename).toFile());
    }

    public static FXManifest create(URI baseURI, String launchClass, Path appPath, String name) throws IOException {
        FXManifest manifest = new FXManifest();
        manifest.uri = baseURI;
        manifest.launchClass = launchClass;
        manifest.name = name;

        Files.walkFileTree(appPath, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isDirectory(file) && file.toString().endsWith(".jar"))
                    manifest.files.add(new LibraryFile(appPath, file));
                return FileVisitResult.CONTINUE;
            }
        });

        return manifest;
    }
    
}
