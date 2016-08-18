package org.javacs.example

public class AutocompleteStaticMember {
    public static void test() {
        AutocompleteStaticMember.
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