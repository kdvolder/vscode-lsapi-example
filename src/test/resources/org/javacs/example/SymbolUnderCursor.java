package org.javacs.example;

public class SymbolUnderCursor {
    public String field;

    public String method(String methodParameter) {
        String localVariable;

        localVariable = field;
        localVariable = methodParameter;

        method(SymbolUnderCursor.class.getName());
        this.method("foo");
        Function<String, String> m = this::method;
    }

    public SymbolUnderCursor(String constructorParameter) {
        
    }
}