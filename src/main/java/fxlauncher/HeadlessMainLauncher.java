package fxlauncher;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadlessMainLauncher extends AbstractLauncher<Object> {
	private static final Logger log = Logger.getLogger("HeadlessMainLauncher");

	private LauncherParams parameters;

	private Class<?> appClass;

	public HeadlessMainLauncher(LauncherParams parameters) {
		this.parameters = parameters;
	}

	public static void main(String[] args) throws Exception {
		List<String> mainArgs = Arrays.asList(args);

		LauncherParams parameters = new LauncherParams(mainArgs);

		HeadlessMainLauncher headlessMainLauncher = new HeadlessMainLauncher(parameters);
		headlessMainLauncher.process();
	}

	protected void process() throws Exception {
		syncManifest();

		// replace parameters to deal with manifest settings
		parameters = new LauncherParams(parameters, getManifest());

		setupLogFile();
		checkSSLIgnoreflag();

		updateManifest();

		syncFiles();

		createApplicationEnvironment();
		launchApp();
	}

	public LauncherParams getParameters() {
		return parameters;
	}

	@Override
	protected void updateProgress(double progress) {
		log.info(String.format("Progress: %d%%", (int) (progress * 100)));
	}

	@Override
	protected void createApplication(Class<Object> appClass) {
		this.appClass = appClass;
	}

	private void launchApp() throws Exception {
		setPhase("Application Start");

		Method mainMethod = appClass.getMethod("main", String[].class);
		mainMethod.invoke(null, (Object) new String[0]);
	}

	protected void reportError(String title, Throwable error) {
		log.log(Level.SEVERE, title, error);
	}

	@Override
	protected void setupClassLoader(ClassLoader classLoader) {
	}

}
