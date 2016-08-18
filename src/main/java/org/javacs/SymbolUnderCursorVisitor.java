package org.javacs;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

import javax.tools.JavaFileObject;
import java.util.*;
import java.util.logging.Logger;

/**
 * Finds symbol under the cursor
 */
public class SymbolUnderCursorVisitor extends CursorScanner {
    private static final Logger LOG = Logger.getLogger("main");
    
    public Optional<Symbol> found = Optional.empty();

    public SymbolUnderCursorVisitor(JavaFileObject file, long cursor, Context context) {
        super(file, cursor, context);
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        super.visitMethodDef(tree);

        boolean containsCursorAnywhere =
            containsCursor(tree.mods) ||
            containsCursor(tree.restype) ||
            containsCursor(tree.typarams) ||
            containsCursor(tree.recvparam) ||
            containsCursor(tree.params) ||
            containsCursor(tree.thrown) ||
            containsCursor(tree.defaultValue) ||
            containsCursor(tree.body);

        if (!containsCursorAnywhere) // TODO deal with spaces
            found(tree.sym);
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
        super.visitVarDef(tree);

        boolean containsCursorAnywhere =
            containsCursor(tree.mods) ||
            containsCursor(tree.vartype) ||
            containsCursor(tree.nameexpr) ||
            containsCursor(tree.init);

        if (containsCursor(tree.nameexpr))
            found(tree.sym);
        else if (tree.nameexpr == null && !containsCursorAnywhere)
            found(tree.sym); // TODO deal with spaces
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl tree) {
        super.visitClassDef(tree);

        boolean containsCursorAnywhere =
          containsCursor(tree.mods) ||
          containsCursor(tree.typarams) ||
          containsCursor(tree.extending) ||
          containsCursor(tree.implementing) ||
          containsCursor(tree.defs);

        if (!containsCursorAnywhere) // TODO deal with spaces
            found(tree.sym);
    }

    @Override
    public void visitIdent(JCTree.JCIdent id) {
        super.visitIdent(id);

        if (!containsCursor(id))
            return;

        Symbol symbol = id.sym;

        found(symbol);
    }

    @Override
    public void visitSelect(JCTree.JCFieldAccess tree) {
        super.visitSelect(tree);

        // Given a member reference [expr]::[name]
        // expr is taken care of by visitIdentifier
        // Check cursor is in name
        if (!containsCursor(tree.getExpression())) {
            Symbol symbol = tree.sym;

            found(symbol);
        }
    }

    @Override
    public void visitReference(JCTree.JCMemberReference tree) {
        super.visitReference(tree);

        // Given a member reference [expr]::[name]
        // expr is taken care of by visitIdentifier
        // Check cursor is in name
        if (!containsCursor(tree.getQualifierExpression())) {
            Symbol symbol = tree.sym;

            found(symbol);
        }
    }

    private void found(Symbol symbol) {
        found = Optional.ofNullable(symbol);
    }
}
