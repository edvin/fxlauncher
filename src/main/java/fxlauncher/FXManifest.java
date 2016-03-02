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