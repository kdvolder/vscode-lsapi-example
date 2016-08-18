package org.javacs;

import io.typefox.lsapi.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class GotoTest extends Fixtures {
    private static final Logger LOG = Logger.getLogger("main");
    private static final String file = "/org/javacs/example/Goto.java";
    private static final URI uri = uri(file);

    @Test
    public void localVariable() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 9, 8);

        assertThat(suggestions, contains(location(uri, 4, 15, 4, 20)));
    }

    @Test
    public void defaultConstructor() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 9, 20);

        assertThat(suggestions, contains(location(uri, 2, 13, 2, 17)));
    }

    @Test
    @Ignore // TODO
    public void constructor() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 10, 20);

        assertThat(suggestions, contains(location(uri, 29, 11, 29, 15)));
    }

    @Test
    public void className() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 15, 8);

        assertThat(suggestions, contains(location(uri, 2, 13, 2, 17)));
    }

    @Test
    public void staticField() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 12, 21);

        assertThat(suggestions, contains(location(uri, 35, 25, 35, 36)));
    }

    @Test
    public void field() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 13, 21);

        assertThat(suggestions, contains(location(uri, 36, 18, 36, 23)));
    }

    @Test
    public void staticMethod() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 15, 13);

        assertThat(suggestions, contains(location(uri, 37, 25, 37, 37)));
    }

    @Test
    public void method() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 16, 13);

        assertThat(suggestions, contains(location(uri, 40, 18, 40, 24)));
    }

    @Test
    public void staticMethodReference() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 18, 26);

        assertThat(suggestions, contains(location(uri, 37, 25, 37, 37)));
    }

    @Test
    public void methodReference() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 19, 26);

        assertThat(suggestions, contains(location(uri, 40, 18, 40, 24)));
    }

    @Test
    @Ignore // TODO
    public void typeParam() throws IOException {
        List<? extends Location> suggestions = doGoto(file, 45, 11);

        assertThat(suggestions, contains(location(uri, 2, 18, 2, 23)));
    }

    private LocationImpl location(URI uri, int startRow, int startColumn, int endRow, int endColumn) {
        PositionImpl start = new PositionImpl();

        start.setLine(startRow);
        start.setCharacter(startColumn);

        PositionImpl end = new PositionImpl();

        end.setLine(startRow);
        end.setCharacter(endColumn);

        RangeImpl range = new RangeImpl();

        range.setStart(start);
        range.setEnd(end);

        LocationImpl location = new LocationImpl();

        location.setUri(uri.toString());
        location.setRange(range);

        return location;
    }

    private List<? extends Location> doGoto(String file, int row, int column) throws IOException {
        TextDocumentIdentifierImpl document = new TextDocumentIdentifierImpl();

        document.setUri(uri(file).toString());

        PositionImpl position = new PositionImpl();

        position.setLine(row);
        position.setCharacter(column);

        TextDocumentPositionParamsImpl p = new TextDocumentPositionParamsImpl();

        p.setTextDocument(document);
        p.setPosition(position);

        JavaLanguageServer server = Fixtures.getJavaLanguageServer();

        return server.gotoDefinition(p);
    }

    private static URI uri(String file) {
        try {
            return GotoTest.class.getResource(file).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
