package org.javacs;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.nio.file.Path;

public class StringFileObject extends SimpleJavaFileObject {
    public final String content;
    public final Path path;

    public StringFileObject(String content, Path path) {
        super(path.toUri(), Kind.SOURCE);

        this.content = content;
        this.path = path;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return content;
    }
}
