package org.javacs;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.Test;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class RecompileTest extends Fixtures {
    static {
        Fixtures.init();
    }

    @Test
    public void compileTwice() {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/CompileTwice.java");
        JavacHolder compiler = newCompiler();
        List<String> visits = new ArrayList<>();
        GetClass getClass = new GetClass(compiler.context, visits);

        compiler.onError(errors);

        JCTree.JCCompilationUnit tree = compiler.parse(file);

        compiler.compile(tree);

        tree.accept(getClass);

        assertThat(errors.getDiagnostics(), empty());
        assertThat(visits, hasItems("CompileTwice", "NestedStaticClass", "NestedClass"));

        // Compile again
        tree = compiler.parse(file);

        compiler.compile(tree);

        tree.accept(getClass);

        assertThat(errors.getDiagnostics(), empty());
        assertThat(visits, hasItems("CompileTwice", "NestedStaticClass", "NestedClass",
                                    "CompileTwice", "NestedStaticClass", "NestedClass"));
    }

    @Test
    public void fixParseError() {
        Path path = Paths.get("org/javacs/example/FixParseError.java");
        StringFileObject bad = new StringFileObject("public class FixParseError { public String foo() { return \"foo\"; }", path);
        StringFileObject good = new StringFileObject("public class FixParseError { public String foo() { return \"foo\"; } }", path);
        JavacHolder compiler = newCompiler();
        DiagnosticCollector<JavaFileObject> badErrors = new DiagnosticCollector<>();

        compiler.onError(badErrors);
        compiler.parse(bad);

        DiagnosticCollector<JavaFileObject> goodErrors = new DiagnosticCollector<>();

        assertThat(badErrors.getDiagnostics(), not(empty()));

        // Parse again
        List<String> parsedClassNames = new ArrayList<>();
        GetClass getClass = new GetClass(compiler.context, parsedClassNames);

        compiler.onError(goodErrors);

        JCTree.JCCompilationUnit tree = compiler.parse(good);

        tree.accept(getClass);

        assertThat(goodErrors.getDiagnostics(), empty());
        assertThat(parsedClassNames, contains("FixParseError"));
    }

    @Test
    public void fixTypeError() {
        Path path = Paths.get("org/javacs/example/FixTypeError.java");
        StringFileObject bad = new StringFileObject("public class FixTypeError { public String foo() { return 1; } }", path);
        StringFileObject good = new StringFileObject("public class FixTypeError { public String foo() { return \"foo\"; } }", path);
        JavacHolder compiler = newCompiler();
        DiagnosticCollector<JavaFileObject> badErrors = new DiagnosticCollector<>();

        compiler.onError(badErrors);
        compiler.compile(compiler.parse(bad));

        DiagnosticCollector<JavaFileObject> goodErrors = new DiagnosticCollector<>();

        assertThat(badErrors.getDiagnostics(), not(empty()));

        // Parse again
        List<String> parsedClassNames = new ArrayList<>();

        GetClass getClass = new GetClass(compiler.context, parsedClassNames);

        compiler.onError(goodErrors);

        JCTree.JCCompilationUnit tree = compiler.parse(good);

        compiler.compile(tree);

        tree.accept(getClass);

        assertThat(goodErrors.getDiagnostics(), empty());
        assertThat(parsedClassNames, contains("FixTypeError"));
    }

    private static JavacHolder newCompiler() {
        return new JavacHolder(Collections.emptySet(),
                               Collections.singleton(Paths.get("src/test/resources")),
                               Paths.get("out"));
    }

    @Test
    public void keepTypeError() throws IOException {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/UndefinedSymbol.java");
        JavacHolder compiler = newCompiler();

        // Compile once
        compiler.onError(errors);
        compiler.compile(compiler.parse(file));

        assertThat(errors.getDiagnostics(), not(empty()));

        // Compile twice
        errors = new DiagnosticCollector<>();
        compiler.onError(errors);

        compiler.compile(compiler.parse(file));

        assertThat(errors.getDiagnostics(), not(empty()));
    }

    private static class GetClass extends BaseScanner {
        private final List<String> visits;

        public GetClass(Context context, List<String> visits) {
            super(context);

            this.visits = visits;
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl tree) {
            super.visitClassDef(tree);

            visits.add(tree.getSimpleName().toString());
        }
    }
}
