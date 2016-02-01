# FXLauncher

Auto updating launcher for JavaFX Applications. Combined with JavaFX native packagaging, you get
a native installer with automatic app updates.

You can see the launcher in action in this [Demo Application](https://github.com/edvin/tornadofx-samples). The
example uses Maven, but the launcher is not maven spesific in any way.

## How does it work?

FXLauncher is a very small binary that can be used to boot your application. All it needs is access to your application 
manifest [example here](http://fxsamples.tornado.no/app.xml). FXLauncher will look
for the manifest in the current folder, or you can specify it via a parameter.
 
After the manifest is retrieved and stored in the current folder as `app.xml`, FXLauncher synchronizes every file
 mentioned in the manifest while providing the user with a progress indicator. After all resources are in sync,
 a classloader is initialized with all the resources from the manifest.
 
Lastly, the application entrypoint retrieved from the manifest is invoked. Everything happens in-JVM, no restarts needed.

Before each run, the launcher will synchronize all resources and seamlessly launch an always updated application.

## How to configure your project

Have a look at the [deployment descriptor example](https://github.com/edvin/tornadofx-samples/blob/master/pom.xml). You need
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
			<vendor>MyCompany</vendor>
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
		</executions>
	</plugin>
	```
	
	Note that the mainClass is set to the launcher, _not_ to your main application class. Instead of using `javafx-maven-plugin` you can
	use any mechanism that copies all the dependencies to a single folder. Nothing special here.

3. Configure manifest creation

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
			</arguments>
		</configuration>
	</plugin>
	```
	
	The main method in `fxlauncher.CreateManifest` is invoked with the following arguments:
	
	 - Published APP Base URL
	 - Your application main class
	 - The output directory for the manifest (This must be where the resources for the app is placed)
	
	You can run this from the command line instead of using a build tool:
	 
	```bash
	java -cp fxlauncher-1.0.1.jar fxlauncher.CreateManifest http://hostname/app your.package.AppClass target/jfx/app MyApp
	```
	
	All you need to do now is upload the contents of the target folder to the URL specified as the first argument to `CreateManifest`. This last step
	is what you would do every time you want to deploy a new version of your app.

### Application start alternatives

1. Lanucher is local, remote manifest

	```bash
	java -jar fxlauncher-1.0.1.jar http://fxsamples.tornado.no/app.xml
	```

2. Launcher and manifest is local (download app.xml first)

	```bash
	java -jar fxlauncher-1.0.1.jar
	```

3. Build a native installer that wraps only the launcher, and points to the remote manifest

	_The installer only has to be built once, no need to rebuild it when you deploy a new version of your application. 
	 Even the launcher will be updated if you upgrade the launcher dependency later on._

	 Create an empty folder with the following structure:
	 
	```
	lib
	 |
	 fxlauncher-1.0.jar
	```

	Make javapackager bundle the launcher together with an argument pointing it to where you uploaded the manifest:
	
	```
	javapackager -deploy -native -outdir packages -outfile MyApp -srcdir . -srcfiles lib/fxlauncher-1.0.1.jar \
	 -argument http://hostname/app/app.xml -appclass fxlauncher.Launcher -name "Myapp" -title "MyApp"
	```

	The result is a native installer for your platform that will automatically update whenever you deploy new artifacts to `http://hostname/app`.
	
	To deploy your artifacts, a simple `scp` will do the trick, or you can use a build system plugin that transfers the files for you.
	
### Try a native installer
	
You can download prebuilt installers for a demo application to see FXLauncher in action

- [MacOSX](http://fxsamples.tornado.no/FXSamples-1.0.dmg)
- [Windows](http://fxsamples.tornado.no/FXSamples-1.0.exe)
- [Linux](http://fxsamples.tornado.no/fxsamples-1.0.deb)

## A slimmer alternative

It is also possible to embed the launchar jar in a native installer system like Advanced Installer
and configure it to start the launcher with a single paramter pointing to your `app.xml` - same approach as above, but without
 using javapackager. With this approach, you can choose wether to include a JRE or have the installer software preinstall it.

### A note on classloaders

FXLauncher uses a custom classloader to dynamically load the synchronized resources. This classloader is 
then made available to the `FXMLLoader`. You can access it via `FXMLLoader.getDefaultClassLoader()`.