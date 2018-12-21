package fxlauncher;

public class Strings {
    public static String ensureEndingSlash(String s) {
        if (s != null && !s.endsWith("/"))
            s += "/";

        return s;
    }
}
