package org.javacs;

import com.sun.tools.javac.util.Context;

/**
 * Useful for debugging what regular javac is doing with context.
 */
public class TestContext extends Context {
    @Override
    public <T> T get(Class<T> clazz) {
        return super.get(clazz);
    }

    @Override
    public <T> void put(Class<T> clazz, T data) {
        super.put(clazz, data);
    }

    @Override
    public <T> void put(Class<T> clazz, Factory<T> fac) {
        super.put(clazz, fac);
    }

    @Override
    public <T> T get(Key<T> key) {
        return super.get(key);
    }

    @Override
    public <T> void put(Key<T> key, T data) {
        super.put(key, data);
    }

    @Override
    public <T> void put(Key<T> key, Factory<T> fac) {
        super.put(key, fac);
    }
}
