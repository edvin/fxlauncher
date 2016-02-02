# FXLauncher

Auto updating launcher for JavaFX Applications. Combined with JavaFX native packagaging, you get
a native installer with automatic app updates.

You can see the launcher in action in this [Demo Application](https://github.com/edvin/tornadofx-samples). The
example uses Maven, but the launcher is not maven spesific in any way.

There is also a [QuickStart Project](https://github.com/edvin/fxldemo) with a complete 
[pom.xml](https://github.com/edvin/fxldemo/blob/master/pom.xml) that describes the steps involved.
 
## How does it work?

FXLauncher is a 14Kb jar that can be used to boot your application. All it needs is access to your application 
manifest [example here](http://fxldemo.tornado.no/app.xml). FXLauncher will look
for the manifest in the current folder, or you can specify it via a parameter.
 
After the manifest is retrieved and stored in the current folder as `app.xml`, FXLauncher synchronizes every file
 mentioned in the manifest while providing the user with a progress indicator. After all resources are in sync,
 a classloader is initialized with all the resources from the manifest.
 
Lastly, the application entrypoint retrieved from the manifest is invoked. Everything happens in-JVM, no restarts needed.

Before each run, the launcher will synchronize all resources and seamlessly launch an always updated application.

## How to use FXLauncher

Have a look at the [pom.xml](https://github.com/edvin/fxldemo/blob/master/pom.xml) from the [QuickStart Project](https://github.com/edvin/fxldemo).

The following steps are involved:

- Compile project jar to app.dir
- Copy dependencies to app.dir
- Generate app.xml manifest and embed into launcher
- Create native installer
- Upload artifacts to auto update repository

All you need to do is configure the project spesific configuration properties:

```xml
<properties>
	<!-- Application Name -->
	<app.filename>${project.name}</app.filename>

	<!-- The JavaFX Application class name -->
	<app.mainClass>no.tornado.FxmlDemo</app.mainClass>

	<!-- The app and launcher will be assembled in this folder -->
	<app.dir>${project.build.directory}/app</app.dir>

	<!-- Native installers will be build in this folder -->
	<app.installerdir>${project.build.directory}/installer</app.installerdir>

	<!-- Base URL where you will host the application artifacts -->
	<app.url>http://your.hostname/app/</app.url>

	<!-- Optional scp target for application artifacts hosted at the above url -->
	<app.deploy.target>user@your.hostname:folder</app.deploy.target>
</properties>
```

### Maven targets
 
#### Generate the application

	mvn clean package
	
#### Deploy the application artifacts to your webserver
	
	mvn exec:exec@deploy-app
	
#### Build a native installer
	
	mvn exec:exec@installer

The native installer does not contain any application code, only the launcher and the manifest uri. There is
	no need to rebuild your native installer when you update your project, simply run the `deploy-app` goal
	and all users will run the newest version on their next startup. Point users to the `fxlauncher.jar` or
	 to a native installer if you wish.
	
### Try a native installer
	
You can download prebuilt installers for a more complex demo application to see FXLauncher in action

- [MacOSX](http://fxsamples.tornado.no/FXSamples-1.0.dmg)
- [Windows](http://fxsamples.tornado.no/FXSamples-1.0.exe)
- [Linux](http://fxsamples.tornado.no/fxsamples-1.0.deb)

## A slimmer alternative

It is also possible to embed the launchar jar in a native installer system like Advanced Installer - same approach as above, 
but without using javapackager. With this approach, you can choose wether to include a JRE or have the installer software preinstall it.
Again, you are only distributing the launcher with the native installer, the rest is pulled down on demand.

### A note on classloaders

FXLauncher uses a custom classloader to dynamically load the synchronized resources. This classloader is 
then made available to the `FXMLLoader`. You can access it via `FXMLLoader.getDefaultClassLoader()`.