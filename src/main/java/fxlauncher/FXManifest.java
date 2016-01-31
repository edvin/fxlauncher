package fxlauncher;

import javax.xml.bind.JAXB;
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
	public static final String filename = "app.xml";

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

    public URI getFXAppURI() {
        return uri.resolve(filename);
    }

	public static Path getPath() {
		return Paths.get(filename);
	}

	public void save() {
		JAXB.marshal(this, getPath().toFile());
	}

	public static FXManifest load() {
		return JAXB.unmarshal(getPath().toFile(), FXManifest.class);
	}
}