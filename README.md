# FXLauncher

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/no.tornado/fxlauncher/badge.svg?cacheBust=true)](https://search.maven.org/#search|ga|1|no.tornado.fxlauncher)
[![Apache License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Auto updating launcher for JavaFX Applications. Combined with JavaFX native packaging, you get
a native installer with automatic app updates.

You can see the launcher in action in this [Demo Application](http://fxldemo.tornado.no).

### QuickStart projects

- [Maven Example](https://github.com/edvin/fxldemo) with [pom.xml](https://github.com/edvin/fxldemo/blob/master/pom.xml)
- [Gradle Example](https://github.com/edvin/fxldemo-gradle) with [build.gradle](https://github.com/edvin/fxldemo-gradle/blob/master/build.gradle)

### Changelog

Check out [the changelog](https://github.com/edvin/fxlauncher/blob/master/CHANGELOG.md) for a list of all updates to the project.

### Video demonstration
 	
See the launcher in action in this short [screencast](https://www.youtube.com/watch?v=NCP9wjRPQ14). There is also a [video](https://www.youtube.com/watch?v=-6PlFVUgntU) about customizing the update user interface.

## How does it work?

FXLauncher is a 18Kb jar that can be used to boot your application. It knows the location
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

### Adhoc usage
	
FXLauncher can also be used to launch an application at an arbitrary url by specifying the `--app` parameter at startup:
	
```bash
java -jar fxlauncher.jar --app=http://remote/location/app.xml
```

Alternatively (or in combination with `--app...`), you can override the uri attribute in the manifest (`app.xml`) so that both `app.xml` and all resources are loaded from the specified uri. This is especially useful for testing the complete setup locally or from a staging environment.

```bash
java -jar fxlauncher.jar --uri=http://remote/location/
```

The two parameters also work in tandem, allowing you to load a specified manifest from one URL and override its uri.

```bash
java -jar fxlauncher.jar --app=http://remote/location/app.xml --uri=http://remote/location/
```

Note: All parameters (including these) are passed on to your application.  So please ensure that your parameters have a different name if they carry different data.

#### Include other artifacts

`--include-extensions` accepts a comma separated list of filename extensions to include of other resources from the build dir. By default it always includes jar, war.

#### Class-Loader issues

To load the application FXLauncher creates a new classloader fed with the classes of your manifest. This works as long as none of the
classes uses the system classloader to load one of your classes or resources which is not aware of the application classes then.

One example is Java's `java.net.URL` class which uses `Class.forName` to load a custom handler, which uses the classloader of
the callee which is the system classloader then.

To workaround that problem you could setup a special FXLauncher classloader which allows a different strategy how to feed the classpath
with additional classpath entries.

```bash
java -Djava.system.class.loader=fxlauncher.FxlauncherClassCloader -jar fxlauncher.jar --app=http://remote/location/app.xml
```

### Headless

FXLauncher allows you to run in headless mode and thus be used not only for JavaFX applications, but also for e.g. services which run
without any gui at all.

For this to work you could use `fxlauncher.HeadlessMainLauncher`

```bash
java -classpath fxlauncher.jar fxlauncher.HeadlessMainLauncher --app=http://remote/location/app.xml
```

Notice: `WhatsNew` is not supported.

### File based deployment

The app uri can be a `file://` based url instead of a web uri if your application is hosted on a common network location for your users.

#### UNC Paths
FXLauncher can deal with [UNC paths], if you ask it nicely. Instead of `\\server\share\myapp` use `file:////server/share/myapp`.
Yes, that's four forward slashes; two for the `file://` protocol and two for the UNC path.

[UNC paths]: https://www.lifewire.com/unc-universal-naming-convention-818230

### Native installers

The native installer does not contain any application code, only the launcher. There is
	no need to rebuild your native installer when you update your project, simply run the `deploy-app` goal
	and all users will run the newest version on their next startup. Point users to the `fxlauncher.jar` or
	 to a native installer if you wish.
	
### Try a native installer
	
Check out these prebuilt installers for a more complex demo application

- [MacOSX](http://fxldemo.tornado.no/FxlDemo-2.0.dmg)
- [Windows](http://fxldemo.tornado.no/FxlDemo-2.0.exe)
- [Linux](http://fxldemo.tornado.no/fxldemo-2.0.deb)

## Specify cache directory

By default, the artifacts are downloaded to the current working directory. This is usually fine for per-user native 
installers, but if you distribute your application via a system-wide native installer, or just the launcher 
jar, you might want to specify where the downloaded artifacts land. See the 
[cache dir documentation] for more information.

[cache dir documentation]: https://github.com/edvin/fxlauncher/wiki/Optional-Cache-Directory

## Installation location

It's worth noting that the two package alternatives for Windows, (EXE and MSI) have different default install locations.
While EXE will default to [`%LocalAppData%`], the MSI installer will default to `%ProgramFiles%`.  To write to 
`%ProgramFiles%` one definitely does need admin privileges—that poses a problem for FXLauncher which, by default, 
downloads updates to where it was installed. 

If you use the MSI installer you will therefore need to tell FXLauncher to cache artifacts somewhere it is allowed
to put them. For an OS-independent answer to this problem, look no further than the 
[two magical strings][cache dir documentation], `USERLIB` and `ALLUSERS`. 


[`%LocalAppData%`]: https://www.howtogeek.com/318177/what-is-the-appdata-folder-in-windows/

Read more about Java Packager in the official documentation:

https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/self-contained-packaging.html

## Accept downgrades

FXLauncher will not download a remote version if the local version is newer. This is controlled
by comparing a timestamp in the manifest. Specifying `--accept-downgrades=true` as the last argument to CreateManifest will
allow you to make sure that the version you have published will always be used by your clients even if they have a newer version installed.
This option is also available in the Gradle plugin as `acceptDowngrades`.

## Ignore ssl errors.

If you are behind a proxy that does a _man in the middle attack_ to snoop on ssl connections, you can use the
```--ignoressl``` commandline option to ignore the ssl errors this will generate.

## Stop after update errors.

Normally FXlauncher will try to launch the application even if there were errors
during the update of the files from the server. Often this will work using the files
already in the local cache. You can change this behavior by specifying
`--stop-on-update-errors=true`. The error will be shown and the launcher will exit.
Specifying `--stopOnUpdateErrors` will result in the same behavior, but this variant
is deprecated and will be removed in future versions. If both options are specified
the value of `--stop-on-update-errors` will be used.

## Show what's new dialog.

You can have FXLauncher show a whats new dialog. This dialog will
only be shown when FXLauncher has to download new files from the server. The html will be shown in a `WebView`. Specify `--whats-new=https://whatsnewurl/here`

### Applications with no default UI

By default the update screen will remain visible until the primary stage is shown. If your application does not show anything on the primary
stage at startup, the update screen will stay visible until it does. You can pass in `--lingering-update-screen=false` to hide it immediately
after the update process has completed.

## A slimmer alternative

Instead of using javapackager to create an installer, you can embed the fxlauncher jar in a native installer system like Advanced Installer—same approach as above, 
but without using javapackager. With this approach, you can choose whether to include a JRE or have the installer software preinstall it.
Again, you are only distributing the launcher with the native installer, the rest is pulled down on demand.

### A note on classloaders

FXLauncher uses a custom classloader to dynamically load the synchronized resources. This classloader is 
then made available to the `FXMLLoader`. You can access it via `FXMLLoader.getDefaultClassLoader()`.

### Platform specific resources

FXLauncher supports filtering of resources for the running platform. Any resource
that ends with `-[mac|win|linux].jar` will only be downloaded and put on the classpath on the corresponding
platform. The manifest enforces this though the `os` attribute in `app.xml`.

### Native libraries

If you need to load native libraries before the custom class loader kicks inn, specify the `--preload-native-libraries=` parameter
to CreateManifest. It supports a comma separated list of libraries to load. Remember: No extensions, just the library name.

### Custom UI

There are two ways to customize the appearance of the update UI. Either you can configure the 
supported style properties in the manifest, or you can provide a custom implementation of the
[UIProvider](https://github.com/edvin/fxlauncher/blob/master/src/main/java/fxlauncher/UIProvider.java)
to completely customize the UI. Have a look at this [Custom UI Demo Project](https://github.com/edvin/fxlauncher-custom-ui) for
more information about customizing the updater.
