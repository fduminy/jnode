package org.jnode.mavenizer;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Log {
    private static final boolean DEBUG = true;
    private static final boolean WARN = true;
    private static final boolean TRACE = false;

    public static void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

    public static void warn(String message) {
        if (WARN) {
            System.err.println(message);
        }
    }

    public static void trace(String message) {
        if (TRACE) {
            System.out.println(message);
        }
    }
}
