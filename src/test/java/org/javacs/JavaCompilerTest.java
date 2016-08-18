package org.javacs;

import com.google.common.collect.ImmutableList;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.tree.JCTree;
import org.junit.Ignore;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

// TODO java compiler can fail badly, handle somehow
@Ignore
public class JavaCompilerTest {
    private static final Logger LOG = Logger.getLogger("main");

    @Test
    public void javacTool() throws IOException {
        JavaCompiler javaCompiler = JavacTool.create();
        StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(this::reportError, null, Charset.defaultCharset());
        List<String> options = ImmutableList.of("-sourcepath", Paths.get("src/test/resources").toAbsolutePath().toString());
        List<String> classes = Collections.emptyList();
        File file = Paths.get("src/test/resources/org/javacs/example/Bad.java").toFile();
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Collections.singleton(file));
        JavacTask task = (JavacTask) javaCompiler.getTask(null, fileManager, this::reportError, options, classes, compilationUnits);

        Iterable<? extends CompilationUnitTree> parsed = task.parse();
        Iterable<? extends Element> typed = task.analyze();

        LOG.info(typed.toString());
    }

    @Test
    public void javacHolder() {
        JavacHolder javac = new JavacHolder(Collections.emptySet(), Collections.singleton(Paths.get("src/test/resources")), Paths.get("target"));
        File file = Paths.get("src/test/resources/org/javacs/example/Bad.java").toFile();
        JCTree.JCCompilationUnit parsed = javac.parse(javac.fileManager.getRegularFile(file));

        javac.compile(parsed);

        LOG.info(parsed.toString());
    }

    private void reportError(Diagnostic<? extends JavaFileObject> error) {
        LOG.severe(error.getMessage(null));
    }
}
