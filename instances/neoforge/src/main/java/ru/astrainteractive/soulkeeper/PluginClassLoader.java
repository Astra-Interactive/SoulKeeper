package ru.astrainteractive.soulkeeper;

import java.net.URL;
import java.net.URLClassLoader;

public final class PluginClassLoader extends URLClassLoader {

    static {
        // Required since Java 9+
        ClassLoader.registerAsParallelCapable();
    }

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}

