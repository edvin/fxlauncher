package fxlauncher;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@XmlRootElement(name = "Application")
public class FXManifest {
	@XmlAttribute
	URI uri;
	@XmlAttribute(name = "launch")
	String launchClass;
	@XmlElement(name = "lib")
	List<LibraryFile> files = new ArrayList<>();
	@XmlElement
	String updateText = "Updating...";
	@XmlElement
	String updateLabelStyle = "-fx-font-weight: bold;";
	@XmlElement
	String progressBarStyle = "-fx-pref-width: 200;";
	@XmlElement
	String wrapperStyle = "-fx-spacing: 10; -fx-padding: 25;";
	@XmlElement
	String parameters;
	@XmlElement
	String cacheDir;

	public String getFilename() {
		return String.format("%s.xml", launchClass);
	}

	public URI getFXAppURI() {
		if (uri.getPath().endsWith("/"))
			return uri.resolve("app.xml");

		return URI.create(uri.toString() + "/app.xml");
	}

	public Path getPath(Path cacheDir) {
		return cacheDir.resolve(getFilename());
	}

	public Path resolveCacheDir(Map<String, String> namedParams) {
		if (namedParams == null) namedParams = Collections.emptyMap();

		String cacheDir = namedParams.containsKey("cache-dir") ? namedParams.get("cache-dir") : this.cacheDir;

		if (cacheDir == null || cacheDir.isEmpty()) return Paths.get(".");

		Path path;

		if (cacheDir.startsWith("USERLIB/")) {
			switch (OS.current) {
				case mac:
					path = Paths.get(System.getProperty("user.home"))
						.resolve("Library")
						.resolve("Application Support")
						.resolve(cacheDir.substring(8));
					break;
				case win:
					path = Paths.get(System.getProperty("user.home"))
						.resolve("AppData")
						.resolve(cacheDir.substring(8));
					break;
				default:
					path = Paths.get(System.getProperty("user.home"))
						.resolve("." + cacheDir.substring(8));
			}
		} else {
			path = Paths.get(cacheDir);
		}

		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return Paths.get(cacheDir);
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FXManifest that = (FXManifest) o;

		if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
		if (launchClass != null ? !launchClass.equals(that.launchClass) : that.launchClass != null) return false;
		if (files != null ? !files.equals(that.files) : that.files != null) return false;
		if (updateText != null ? !updateText.equals(that.updateText) : that.updateText != null) return false;
		if (updateLabelStyle != null ? !updateLabelStyle.equals(that.updateLabelStyle) : that.updateLabelStyle != null)
			return false;
		if (progressBarStyle != null ? !progressBarStyle.equals(that.progressBarStyle) : that.progressBarStyle != null)
			return false;
		return wrapperStyle != null ? wrapperStyle.equals(that.wrapperStyle) : that.wrapperStyle == null;

	}

	public int hashCode() {
		int result = uri != null ? uri.hashCode() : 0;
		result = 31 * result + (launchClass != null ? launchClass.hashCode() : 0);
		result = 31 * result + (files != null ? files.hashCode() : 0);
		result = 31 * result + (updateText != null ? updateText.hashCode() : 0);
		result = 31 * result + (updateLabelStyle != null ? updateLabelStyle.hashCode() : 0);
		result = 31 * result + (progressBarStyle != null ? progressBarStyle.hashCode() : 0);
		result = 31 * result + (wrapperStyle != null ? wrapperStyle.hashCode() : 0);
		return result;
	}

}