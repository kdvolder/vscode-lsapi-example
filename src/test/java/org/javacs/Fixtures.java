package org.javacs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import com.github.kdvolder.lsapi.util.LoggingFormat;

import io.typefox.lsapi.InitializeParamsImpl;

public class Fixtures {
    static {
        try {
            LoggingFormat.startLogging();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() { }

    static JavaLanguageServer getJavaLanguageServer() {
        Set<Path> classPath = Collections.emptySet();
        Set<Path> sourcePath = Collections.singleton(Paths.get("src/test/resources").toAbsolutePath());
        Path outputDirectory = Paths.get("out").toAbsolutePath();
        JavacHolder javac = new JavacHolder(classPath, sourcePath, outputDirectory);
        JavaLanguageServer server = new JavaLanguageServer(javac);

        InitializeParamsImpl init = new InitializeParamsImpl();
        String workspaceRoot = Paths.get(".").toAbsolutePath().toString();

        init.setRootPath(workspaceRoot);

        server.initialize(init);
        return server;
    }
}
