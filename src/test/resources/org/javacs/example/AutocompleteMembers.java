package org.javacs.example

public class AutocompleteMembers {
    private String field;
    private static String fieldStatic;

    {
        f; // field, fieldStatic, method, methodStatic
        this.f; // field, method
        AutocompleteMembers.f; // fieldStatic, methodStatic
        this::m; // method
        AutocompleteMembers::m; // methodStatic
    }

    static {
        f; // fieldStatic
        AutocompleteMembers.f; // fieldStatic
        AutocompleteMembers::m; // methodStatic
    }

    private void method(String argument) {
        f; // field, fieldStatic, method, methodStatic, argument
        this.f; // field, method
        AutocompleteMembers.f; // fieldStatic, methodStatic
        this::m; // method
        AutocompleteMembers::m; // methodStatic
    }

    private static void methodStatic(String argument) {
        f; // fieldStatic, argument
        AutocompleteMembers.f; // fieldStatic
        AutocompleteMembers::m; // methodStatic
    }
}