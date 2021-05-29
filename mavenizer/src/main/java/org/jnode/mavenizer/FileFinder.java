package org.jnode.mavenizer;

import java.io.File;

import static org.jnode.mavenizer.FileFinder.Action.CONTINUE;
import static org.jnode.mavenizer.FileFinder.Action.STOP;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @param <T> type of result
 * 
 */
public class FileFinder<T> {
    public static void delete(final File root) {
        new FileFinder<Object>() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            protected Action processFile(File f) {
                f.delete();
                return CONTINUE;
            }
            @SuppressWarnings("ResultOfMethodCallIgnored")
            protected Action processDirectory(File dir) {
                if (!dir.equals(root)) {
                    dir.delete();
                }
                return CONTINUE;
            };
        }.iterate(root);
    }
    
    T result;

    public final T iterate(File f) {
        iterateImpl(f);
        return result;
    }
    
    protected final Action iterateImpl(File f) {
        Action action = CONTINUE;
        
        if (f.isFile()) {
            processFile(f);
        } else if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File child : files) {
                    action = iterateImpl(child);

                    if (STOP.equals(action)) {
                        break;
                    }
                }
            }
            processDirectory(f);
        }
        
        return action;
    }
    
    protected Action processFile(File file) {
        return CONTINUE;
    }
    protected Action processDirectory(File dir) {
        return CONTINUE;
    }
    
    protected void setResult(T result) {
        this.result = result;
    }

    protected enum Action {
        STOP,
        CONTINUE;
    }
}
