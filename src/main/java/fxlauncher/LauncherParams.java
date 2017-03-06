package fxlauncher;

import javafx.application.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation Application.Parameters that wraps the parameters given to the application
 * at startup, and adds any manifest configured parameters unless they were overriden
 * by the command line.
 */
public class LauncherParams extends Application.Parameters {
    private final List<String> rawArgs = new ArrayList<>();
    private final Map<String, String> namedParams = new HashMap<>();
    private final List<String> unnamedParams = new ArrayList<>();

    public LauncherParams(List<String> rawArgs) {
        this.rawArgs.addAll(rawArgs);
        computeParams();
    }

    public LauncherParams(Application.Parameters delegate, FXManifest manifest) {
        // Add all raw args from the parent application
        rawArgs.addAll(delegate.getRaw());

        // Add parameters from the manifest unless they were already specified on the command line
        if (manifest.parameters != null) {
            for (String arg : manifest.parameters.split("\\s")) {
                if (arg != null) {
                    if (rawArgs.contains(arg))
                        continue;

                    if (arg.startsWith("--") && arg.contains("=")) {
                        String argname = arg.substring(0, arg.indexOf("="));
                        if (rawArgs.stream().filter(a -> a.startsWith(argname)).findAny().isPresent())
                            continue;
                    }

                    rawArgs.add(arg);
                }
            }
        }

        computeParams();
    }

    private void computeParams() {
        // Compute named and unnamed parameters
        computeNamedParams();
        computeUnnamedParams();
    }

    public List<String> getRaw() {
        return rawArgs;
    }

    public List<String> getUnnamed() {
        return unnamedParams;
    }

    public Map<String, String> getNamed() {
        return namedParams;
    }

    /**
     * Returns true if the specified string is a named parameter of the
     * form: --name=value
     *
     * @param arg the string to check
     * @return true if the string matches the pattern for a named parameter.
     */
    private boolean isNamedParam(String arg) {
        return arg.startsWith("--") && (arg.indexOf('=') > 2 && validFirstChar(arg.charAt(2)));
    }

    /**
     * This method parses the current array of raw arguments looking for
     * name,value pairs. These name,value pairs are then added to the map
     * for this parameters object, and are of the form: --name=value.
     */
    private void computeNamedParams() {
        rawArgs.stream().filter(this::isNamedParam).forEach(arg -> {
            final int eqIdx = arg.indexOf('=');
            String key = arg.substring(2, eqIdx);
            String value = arg.substring(eqIdx + 1);
            namedParams.put(key, value);
        });
    }
    /**
     * This method computes the list of unnamed parameters, by filtering the
     * list of raw arguments, stripping out the named parameters.
     */
    private void computeUnnamedParams() {
        unnamedParams.addAll(rawArgs.stream().filter(arg -> !isNamedParam(arg)).collect(Collectors.toList()));
    }

    /**
     * Validate the first character of a key. It is valid if it is a letter or
     * an "_" character.
     *
     * @param c the first char of a key string
     * @return whether or not it is valid
     */
    private boolean validFirstChar(char c) {
        return Character.isLetter(c) || c == '_';
    }
}
