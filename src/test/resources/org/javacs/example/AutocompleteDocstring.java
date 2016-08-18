package org.javacs.example

/**
 * A class
 */
public class AutocompleteDocstring {
    public void members() {
        this.
    }

    public void statics() {
        AutocompleteDocstring.
    }

    /**
     * A fieldStatic
     */
    public static String fieldStatic;
    /**
     * A field
     */
    public String field;
    /**
     * A methodStatic
     */
    public static String methodStatic() {
        return "foo";
    }
    /**
     * A method
     */
    public String method() {
        return "foo";
    }
}