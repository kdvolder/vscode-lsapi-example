package org.javacs.example

public class AutocompleteStaticReference {
    public static void test() {
        AutocompleteStaticReference::
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