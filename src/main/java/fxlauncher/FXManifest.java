package fxlauncher;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
@XmlRootElement(name = "Application")
public class FXManifest {
	public static final String filename = "fxapp.xml";

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
	String wrapperStyle = "-fx-spacing: 10;\n-fx-padding: 25;";
	@XmlElement
	String errorTitle = "Failed to %s application";
	@XmlElement
	String errorHeader = "There was an error during %s of the application";

    public URI getFXAppURI() {
        return uri.resolve("fxapp.xml");
    }
}

