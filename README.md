# FXLauncher

Auto updating launcher for JavaFX Applications. Combined with JavaFX native packaging, you get
a native installer with automatic app updates.

You can see the launcher in action in this [Demo Application](http://fxldemo.tornado.no).

### QuickStart projects

- [Maven Example](https://github.com/edvin/fxldemo) with [pom.xml](https://github.com/edvin/fxldemo/blob/master/pom.xml).
- [Gradle Example](https://github.com/edvin/fxldemo-gradle) with [build.gradle](https://github.com/edvin/fxldemo/blob/master/build.gradle).

### Video demonstration
 	
See the launcher in action in this short [screencast](https://www.youtube.com/watch?v=NCP9wjRPQ14).

## How does it work?

FXLauncher is a 14Kb jar that can be used to boot your application. It knows the location
of your application repository where you host all the app resources.

See a manifest [example here](http://fxldemo.tornado.no/app.xml). FXLauncher will look up the
remote repository to check for a newer version of the manifest on each startup.
 
After the manifest is retrieved, FXLauncher synchronizes every file mentioned in the manifest while 
providing the user with a progress indicator. After all resources are in sync, a classloader is 
initialized with all the resources from the manifest.
 
Lastly, the application entry point retrieved from the manifest is invoked. Everything happens in-JVM, no restarts needed.

Before each run, the launcher will synchronize all resources and seamlessly launch an always updated application.

## How to use FXLauncher

See the QuickStart projects at the top of the README for information on integrating FXLauncher in your build system.
	
#### Native installers

The native installer does not contain any application code, only the launcher. There is
	no need to rebuild your native installer when you update your project, simply run the `deploy-app` goal
	and all users will run the newest version on their next startup. Point users to the `fxlauncher.jar` or
	 to a native installer if you wish.
	
### Try a native installer
	
Check out these prebuilt installers for a more complex demo application

- [MacOSX](http://fxsamples.tornado.no/CRMApplication-1.0.dmg)
- [Windows](http://fxsamples.tornado.no/CRMApplication-1.0.exe)
- [Linux](http://fxsamples.tornado.no/crmapplication-1.0.deb)

## A slimmer alternative

It is also possible to embed the launchar jar in a native installer system like Advanced Installer - same approach as above, 
but without using javapackager. With this approach, you can choose wether to include a JRE or have the installer software preinstall it.
Again, you are only distributing the launcher with the native installer, the rest is pulled down on demand.

### A note on classloaders

FXLauncher uses a custom classloader to dynamically load the synchronized resources. This classloader is 
then made available to the `FXMLLoader`. You can access it via `FXMLLoader.getDefaultClassLoader()`.