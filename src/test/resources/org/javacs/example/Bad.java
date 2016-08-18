import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import io.typefox.lsapi.Location;
import io.typefox.lsapi.LocationImpl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Bad {
    // This is a really evil input that causes the Attr phase to throw a null pointer exception
    public void test(Stream<?> stream) {
        stream.flatMap(compilationUnit -> {
            compilationUnit.accept(new Foo() {
                @Override
                public void callback() {
                    // Do nothing
                }
            });
        });
    }
}
