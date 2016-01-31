# FXLauncher

Auto updating launcher for JavaFX Applications. Combined with JavaFX native packagaging, you get
a native installer with automatic app updates.

You can see the launcher in action in this [Demo Application](https://github.com/edvin/tornadofx-samples). The
example uses Maven, but the launcher is not maven spesific in any way.

## How does it work?

FXLauncher is a very small binary that can be used to boot your application. All it needs is access to your application 
manifest (example here)[https://github.com/edvin/tornadofx-samples/blob/master/app/app.xml]. FXLauncher will look
for the manifest in the current folder, or you can specify the it with an url parameter.
 
After the manifest is retrieved and stored in the current folder as `app.xml`, FXLauncher synchronizes every file
 mentioned in the manifest while providing the user with a progress indicator. After all resources are in sync,
 a classloader is initialized with all the resources from the manifest.
 
Lastly, the application entrypoint retrieved from the manifest is invoked.

Before each run, the launcher will synchronize all resources and seamlessly launch an always updated application.

## How to configure your project

Look at the (deployment descriptor example)[https://github.com/edvin/tornadofx-samples/blob/master/pom.xml]. You need
to perform the following steps:

1. Include FXLauncher as a dependency

```xml
<dependency>
	<groupId>no.tornado</groupId>
	<artifactId>fxlauncher</artifactId>
	<version>1.0.1</version>
</dependency>
```

2. Configure the `javafx-maven-plugin` (or equivalent) to build your application

```xml
<plugin>
	<groupId>com.zenjava</groupId>
	<artifactId>javafx-maven-plugin</artifactId>
	<version>8.1.5</version>
	<configuration>
		<mainClass>fxlauncher.Launcher</mainClass>
		<appName>CRMApplication</appName>
		<vendor>SYSE</vendor>
		<needShortcut>true</needShortcut>
		<needMenu>true</needMenu>
	</configuration>
	<executions>
		<execution>
			<id>create-jfxjar</id>
			<phase>package</phase>
			<goals>
				<goal>build-jar</goal>
			</goals>
		</execution>
		<execution>
			<id>create-native</id>
			<phase>install</phase>
			<goals>
				<goal>build-native</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

Note that the mainClass is set to the launcher, _not_ to your main application class. Also note that the `build-jar`
goal is executed in the `package` phase. The example above also includes a `built-native` goal in the `install` phase.
It is important that the `build-native` goal is run after FXLauncher adds the manifest.

3. Create the manifest

```xml
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>exec-maven-plugin</artifactId>
	<version>1.4.0</version>
	<executions>
		<execution>
			<phase>package</phase>
			<goals>
				<goal>java</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
		<mainClass>fxlauncher.CreateManifest</mainClass>
		<arguments>
			<argument>http://hostname/app</argument>
			<argument>your.package.AppClass</argument>
			<argument>${project.build.directory}/jfx/app</argument>
			<argument>${project.description}</argument>
		</arguments>
	</configuration>
</plugin>
```

The main method in `fxlauncher.CreateManifest` is invoked with the following arguments:
 1. Published APP Base URL
 2. Your application main class
 3. The output directory for the manifest (This must be where the resources for the app is placed)
 4. The application name used in the launcher

You can run this from the command line instead of using a build tool:
 
```bash
java -jar fxlauncher-1.0.1.jar http://hostname/app your.package.AppClass target/jfx/app MyApp
```

All you need to do now is upload the contents of the target folder to the URL specified as the first argument to `CreateManifest`.

To build a native installer instead, execute `mvn clean install` and look in `target/jfx/native` for your binary.

There is no need to rebuild the installer everytime you change your app. Just run `mvn clean package` and synchronize
the contents of the build output folder to the Published APP URL, and every instance of your application will run
the new version the next time they start.

### A note on classloaders

FXLauncher uses a custom classloader to dynamically load the synchronized resources. This classloader is 
then made available to the `FXMLLoader`. You can access it via `FXMLLoader.getDefaultClassLoader()`.