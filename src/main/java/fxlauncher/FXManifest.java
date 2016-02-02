package fxlauncher;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
@XmlRootElement(name = "Application")
public class FXManifest {
	@XmlAttribute
    String name;
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

    public String getFilename() {
        return String.format("%s.xml", launchClass);
    }

    public URI getFXAppURI() {
        return uri.resolve("app.xml");
    }

	public Path getPath() {
		return Paths.get(getFilename());
	}

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FXManifest that = (FXManifest) o;

        if (!name.equals(that.name)) return false;
        if (!uri.equals(that.uri)) return false;
        if (!launchClass.equals(that.launchClass)) return false;
        if (!files.equals(that.files)) return false;
        if (!updateText.equals(that.updateText)) return false;
        if (!updateLabelStyle.equals(that.updateLabelStyle)) return false;
        if (!progressBarStyle.equals(that.progressBarStyle)) return false;
        return wrapperStyle.equals(that.wrapperStyle);

    }

    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + launchClass.hashCode();
        result = 31 * result + files.hashCode();
        result = 31 * result + updateText.hashCode();
        result = 31 * result + updateLabelStyle.hashCode();
        result = 31 * result + progressBarStyle.hashCode();
        result = 31 * result + wrapperStyle.hashCode();
        return result;
    }
}