package fxlauncher;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
@XmlRootElement(name = "Application")
public class FXManifest {
	@XmlAttribute
	public Long ts;
	@XmlAttribute
	public URI uri;
	@XmlAttribute(name = "launch")
	public String launchClass;
	@XmlElement(name = "lib")
	public List<LibraryFile> files = new ArrayList<>();
	@XmlElement
	public String updateText = "Updating...";
	@XmlElement
	public String updateLabelStyle = "-fx-font-weight: bold;";
	@XmlElement
	public String progressBarStyle = "-fx-pref-width: 200;";
	@XmlElement
	public String wrapperStyle = "-fx-spacing: 10; -fx-padding: 25;";
	@XmlElement
	public String parameters;
	@XmlElement
	public String cacheDir;
	@XmlElement
	public Boolean acceptDowngrade = false;
	@XmlElement
	public Boolean stopOnUpdateErrors = false;
	@XmlElement
	public String preloadNativeLibraries;
	@XmlElement
	public String whatsNewPage;
	@XmlElement
	public Boolean lingeringUpdateScreen = false;

	public List<String> getPreloadNativeLibraryList() {
		if (preloadNativeLibraries == null || preloadNativeLibraries.isEmpty()) return Collections.emptyList();
		return Arrays.asList(preloadNativeLibraries.split(".*,-*"));
	}

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

		if (cacheDir.contains("USERLIB")) {
			String replacement;
			switch (OS.current) {
				case mac:
					replacement = Paths.get(System.getProperty("user.home"))
						.resolve("Library")
						.resolve("Application Support")
						.resolve(cacheDir.substring(8))
						.toString();
					break;
				case win:
					replacement = Paths.get(System.getProperty("user.home"))
						.resolve("AppData")
						.resolve("Local")
						.resolve(cacheDir.substring(8))
						.toString();
					break;
				default:
					replacement = Paths.get(System.getProperty("user.home"))
						.resolve("." + cacheDir.substring(8))
						.toString();
			}
			path = Paths.get(replacement);
		} else if (cacheDir.startsWith("ALLUSERS")) {
			switch (OS.current) {
			case mac:
				path = Paths.get("/Library/Application Support")
					.resolve(cacheDir.substring(9));
				break;
			case win:
				path = Paths.get(System.getenv("ALLUSERSPROFILE"))
					.resolve(cacheDir.substring(9));
				break;
			default:
				path = Paths.get("/usr/local/share")
					.resolve(cacheDir.substring(9));
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

		return path;
	}

	public String getWhatsNewPage()
	{
		return whatsNewPage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FXManifest that = (FXManifest) o;

		if (ts != null ? !ts.equals(that.ts) : that.ts != null) return false;
		if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
		if (launchClass != null ? !launchClass.equals(that.launchClass) : that.launchClass != null) return false;
		if (files != null ? !files.equals(that.files) : that.files != null) return false;
		if (updateText != null ? !updateText.equals(that.updateText) : that.updateText != null) return false;
		if (updateLabelStyle != null ? !updateLabelStyle.equals(that.updateLabelStyle) : that.updateLabelStyle != null) return false;
		if (progressBarStyle != null ? !progressBarStyle.equals(that.progressBarStyle) : that.progressBarStyle != null) return false;
		if (wrapperStyle != null ? !wrapperStyle.equals(that.wrapperStyle) : that.wrapperStyle != null) return false;
		if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
		if (cacheDir != null ? !cacheDir.equals(that.cacheDir) : that.cacheDir != null) return false;
		if (lingeringUpdateScreen != null ? !lingeringUpdateScreen.equals(that.lingeringUpdateScreen) : that.lingeringUpdateScreen != null) return false;
		if (stopOnUpdateErrors != null ? !stopOnUpdateErrors.equals(that.stopOnUpdateErrors) : that.stopOnUpdateErrors != null) return false;
		return acceptDowngrade != null ? acceptDowngrade.equals(that.acceptDowngrade) : that.acceptDowngrade == null;

	}

	@Override
	public int hashCode() {
		int result = ts != null ? ts.hashCode() : 0;
		result = 31 * result + (uri != null ? uri.hashCode() : 0);
		result = 31 * result + (launchClass != null ? launchClass.hashCode() : 0);
		result = 31 * result + (files != null ? files.hashCode() : 0);
		result = 31 * result + (updateText != null ? updateText.hashCode() : 0);
		result = 31 * result + (updateLabelStyle != null ? updateLabelStyle.hashCode() : 0);
		result = 31 * result + (progressBarStyle != null ? progressBarStyle.hashCode() : 0);
		result = 31 * result + (wrapperStyle != null ? wrapperStyle.hashCode() : 0);
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
		result = 31 * result + (acceptDowngrade != null ? acceptDowngrade.hashCode() : 0);
		result = 31 * result + (stopOnUpdateErrors != null ? stopOnUpdateErrors.hashCode() : 0);
		return result;
	}

	public boolean isNewerThan(FXManifest other) {
		return ts == null || other.ts == null || ts > other.ts;
	}

	static FXManifest load(URI uri) throws IOException {
		if (Objects.equals(uri.getScheme(), "file")) {
			return JAXB.unmarshal(new File(uri.getPath()), FXManifest.class);
		}
		URLConnection connection = uri.toURL().openConnection();
		if (uri.getUserInfo() != null) {
			byte[] payload = uri.getUserInfo().getBytes(StandardCharsets.UTF_8);
			String encoded = Base64.getEncoder().encodeToString(payload);
			connection.setRequestProperty("Authorization", String.format("Basic %s", encoded));
		}
		try (InputStream input = connection.getInputStream()) {
			return JAXB.unmarshal(input, FXManifest.class);
		}
	}

}
