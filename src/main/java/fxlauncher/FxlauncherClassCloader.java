package fxlauncher;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by im on 22.02.17.
 */
public class FxlauncherClassCloader extends URLClassLoader
{
    public FxlauncherClassCloader(ClassLoader parentClassLoader)
    {
        super(buildClasspath(System.getProperty("java.class.path")), parentClassLoader);
    }

    void addUrls(List<URL> urls)
    {
        for (URL url : urls)
        {
            this.addURL(url);
        }
    }

    private static URL[] buildClasspath(String classPath)
    {
        if (classPath == null || classPath.trim().length() < 1)
        {
            return new URL[0];
        }

        List<URL> urls = new ArrayList<>();

        int pos;
        while ((pos = classPath.indexOf(File.pathSeparatorChar)) > -1)
        {
            String part = classPath.substring(0, pos);

            addClasspathPart(urls, part);

            classPath = classPath.substring(pos + 1);
        }

        addClasspathPart(urls, classPath);

        return urls.toArray(new URL[urls.size()]);
    }

    private static void addClasspathPart(List<URL> urls, String part)
    {
        if (part == null || part.trim().length() < 1)
        {
            return;
        }

        try
        {
            urls.add(new File(part).toURI().toURL());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
