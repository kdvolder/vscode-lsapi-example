package org.javacs.example;

public class AutocompleteOuter {
    public String field;
    public static String fieldStatic;

    public String method() { }
    public static String methodStatic() { }

    static class StaticInner {
        {
            m
        }
    }

    class Inner {
        {
            m
        }
    }
}