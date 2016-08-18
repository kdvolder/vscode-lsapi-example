package org.javacs;

import javax.lang.model.type.*;
import javax.lang.model.util.AbstractTypeVisitor8;
import java.util.List;

public class BridgeTypeVisitor extends AbstractTypeVisitor8<Void, Void> {

    public void scan(List<? extends TypeMirror> types) {
        for (TypeMirror t : types)
            visit(t);
    }

    @Override
    public final Void visitPrimitive(PrimitiveType t, Void aVoid) {
        visitPrimitive(t);

        return null;
    }

    public void visitPrimitive(PrimitiveType t) {

    }

    @Override
    public final Void visitNull(NullType t, Void aVoid) {
        visitNull(t);

        return null;
    }

    public void visitNull(NullType t) {

    }

    @Override
    public final Void visitArray(ArrayType t, Void aVoid) {
        visitArray(t);

        return null;
    }

    public void visitArray(ArrayType t) {

    }

    @Override
    public final Void visitDeclared(DeclaredType t, Void aVoid) {
        visitDeclared(t);

        return null;
    }

    public void visitDeclared(DeclaredType t) {

    }

    @Override
    public final Void visitError(ErrorType t, Void aVoid) {
        visitError(t);

        return null;
    }

    public void visitError(ErrorType t) {

    }

    @Override
    public final Void visitTypeVariable(TypeVariable t, Void aVoid) {
        visitTypeVariable(t);

        return null;
    }

    public void visitTypeVariable(TypeVariable t) {

    }

    @Override
    public final Void visitWildcard(WildcardType t, Void aVoid) {
        visitWildcard(t);

        return null;
    }

    public void visitWildcard(WildcardType t) {

    }

    @Override
    public final Void visitExecutable(ExecutableType t, Void aVoid) {
        visitExecutable(t);

        return null;
    }

    public void visitExecutable(ExecutableType t) {

    }

    @Override
    public final Void visitNoType(NoType t, Void aVoid) {
        visitNoType(t);

        return null;
    }

    public void visitNoType(NoType t) {

    }

    @Override
    public final Void visitUnknown(TypeMirror t, Void aVoid) {
        visitUnknown(t);

        return null;
    }

    public void visitUnknown(TypeMirror t) {

    }

    @Override
    public final Void visitUnion(UnionType t, Void aVoid) {
        visitUnion(t);

        return null;
    }

    public void visitUnion(UnionType t) {

    }

    @Override
    public final Void visitIntersection(IntersectionType t, Void aVoid) {
        visitIntersection(t);

        return null;
    }

    public void visitIntersection(IntersectionType t) {

    }
}
