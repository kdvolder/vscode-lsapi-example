package org.javacs;

import java.nio.file.Path;

public class NoJavaConfigException extends RuntimeException {
    public NoJavaConfigException(Path forFile) {
        this("Can't find configuration file for " + forFile);
    }
    
    public NoJavaConfigException(String message) {
        super(message);
    }
}