package org.javacs;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import org.junit.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LinterTest extends Fixtures {
    private static final Logger LOG = Logger.getLogger("main");

    @Test
    public void compile() throws IOException {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/HelloWorld.java");
        JavacHolder compiler = newCompiler();
        compiler.onError(errors);
        compiler.compile(compiler.parse(file));

        assertThat(errors.getDiagnostics(), empty());
    }

    @Test
    public void inspectTree() throws IOException {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/HelloWorld.java");
        JavacHolder compiler = newCompiler();
        CollectMethods scanner = new CollectMethods(compiler.context);

        compiler.onError(errors);

        JCTree.JCCompilationUnit tree = compiler.parse(file);

        compiler.compile(tree);

        tree.accept(scanner);

        assertThat(scanner.methodNames, hasItem("main"));
    }

    @Test
    public void missingMethodBody() throws IOException {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/MissingMethodBody.java");
        JavacHolder compiler = newCompiler();
        CollectMethods scanner = new CollectMethods(compiler.context);

        compiler.onError(errors);

        JCTree.JCCompilationUnit tree = compiler.parse(file);

        compiler.compile(tree);

        tree.accept(scanner);

        assertThat(scanner.methodNames, hasItem("test"));
        assertThat(errors.getDiagnostics(), not(empty()));

        // Lint again
        errors = new DiagnosticCollector<>();

        compiler.onError(errors);

        tree = compiler.parse(file);

        compiler.compile(tree);

        assertThat(errors.getDiagnostics(), not(empty()));
    }

    @Test
    public void incompleteAssignment() throws IOException {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/IncompleteAssignment.java");
        JavacHolder compiler = newCompiler();
        CollectMethods parsed = new CollectMethods(compiler.context);
        CollectMethods compiled = new CollectMethods(compiler.context);

        compiler.onError(errors);

        JCTree.JCCompilationUnit tree = compiler.parse(file);

        tree.accept(parsed);

        compiler.compile(tree);

        tree.accept(compiled);

        assertThat(parsed.methodNames, hasItem("test")); // Error recovery should have worked
        assertThat(compiled.methodNames, hasItem("test")); // Type error recovery should have worked
        assertThat(errors.getDiagnostics(), not(empty()));
    }

    @Test
    public void undefinedSymbol() throws IOException {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/UndefinedSymbol.java");
        JavacHolder compiler = newCompiler();
        CollectMethods scanner = new CollectMethods(compiler.context);

        compiler.onError(errors);

        JCTree.JCCompilationUnit tree = compiler.parse(file);

        compiler.compile(tree);

        tree.accept(scanner);

        assertThat(scanner.methodNames, hasItem("test")); // Type error, so parse tree is present

        Diagnostic<? extends JavaFileObject> d = errors.getDiagnostics().get(0);

        // Error position should span entire 'foo' symbol
        assertThat(d.getLineNumber(), greaterThan(0L));
        assertThat(d.getStartPosition(), greaterThan(0L));
        assertThat(d.getEndPosition(), greaterThan(d.getStartPosition() + 1));
        assertThat(errors.getDiagnostics(), not(empty()));
    }

    @Test
    public void getType() {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/FooString.java");
        JavacHolder compiler = newCompiler();
        MethodTypes scanner = new MethodTypes(compiler.context);

        compiler.onError(errors);

        JCTree.JCCompilationUnit tree = compiler.parse(file);

        compiler.compile(tree);

        tree.accept(scanner);

        assertThat(errors.getDiagnostics(), empty());
        assertThat(scanner.methodTypes, hasKey("test"));

        Type.MethodType type = scanner.methodTypes.get("test");

        assertThat(type.getReturnType().toString(), equalTo("java.lang.String"));
        assertThat(type.getParameterTypes(), hasSize(1));
        assertThat(type.getParameterTypes().get(0).toString(), equalTo("java.lang.String"));
    }

    @Test
    public void notJava() {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/NotJava.java.txt");
        JavacHolder compiler = newCompiler();
        compiler.onError(errors);
        compiler.compile(compiler.parse(file));

        assertThat(errors.getDiagnostics(), not(empty()));
    }

    @Test
    public void errorInDependency() {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/ErrorInDependency.java");
        JavacHolder compiler = newCompiler();
        compiler.onError(errors);
        compiler.compile(compiler.parse(file));

        assertThat(errors.getDiagnostics(), not(empty()));
    }
    
    @Test
    public void deprecationWarning() {
        DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<>();
        GetResourceFileObject file = new GetResourceFileObject("/org/javacs/example/DeprecationWarning.java");
        JavacHolder compiler = newCompiler();
        compiler.onError(errors);
        compiler.compile(compiler.parse(file));

        assertThat(errors.getDiagnostics(), not(empty()));
    }

    public static class MethodTypes extends BaseScanner {
        public final Map<String, Type.MethodType> methodTypes = new HashMap<>();

        public MethodTypes(Context context) {
            super(context);
        }

        @Override
        public void visitMethodDef(JCTree.JCMethodDecl node) {
            super.visitMethodDef(node);

            JavacTrees trees = JavacTrees.instance(super.context);
            TreePath path = trees.getPath(compilationUnit, node);
            Type.MethodType typeMirror = (Type.MethodType) trees.getTypeMirror(path);

            methodTypes.put(node.getName().toString(), typeMirror);
        }
    }

    public static class CollectMethods extends BaseScanner {
        public final Set<String> methodNames = new HashSet<>();

        public CollectMethods(Context context) {
            super(context);
        }

        @Override
        public void visitMethodDef(JCTree.JCMethodDecl tree) {
            super.visitMethodDef(tree);

            methodNames.add(tree.getName().toString());
        }
    }

    private static JavacHolder newCompiler() {
        return new JavacHolder(Collections.emptySet(),
                               Collections.singleton(Paths.get("src/test/resources")),
                               Paths.get("out"));
    }
}
