package org.javacs;

import com.google.common.io.CharStreams;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents a java source on the system resource path.
 */
public class GetResourceFileObject extends SimpleJavaFileObject {
    public final String path;

    public GetResourceFileObject(String path) {
        super(getResourceUri(path), Kind.SOURCE);

        this.path = path;
    }

    private static URI getResourceUri(String path) {
        try {
            return GetResourceFileObject.class.getResource(path).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        try (Reader r = new InputStreamReader(openInputStream())) {
            return CharStreams.toString(r);
        }
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return GetResourceFileObject.class.getResourceAsStream(path);
    }
}
