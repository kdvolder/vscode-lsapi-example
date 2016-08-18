package org.javacs.example

public class AutocompleteReference {
    public void test() {
        this::
    }

    private static String fieldStatic;
    private String field;
    private static String methodStatic() {
        return "foo";
    }
    private String method() {
        return "foo";
    }
}