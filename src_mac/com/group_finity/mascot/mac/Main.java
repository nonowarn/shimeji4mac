package com.group_finity.mascot.mac;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;

/**
   Dynamically loads the jar file and executes main method to run the
   application in background on Mac.
 */

public class Main {
  public static void main(String args[]) throws Exception {
    File file = new File("../Shimeji.jar");
    ClassLoader loader = URLClassLoader.newInstance(
      new URL[]{ file.toURI().toURL() }
    );
    Class<?> clazz = loader.loadClass("com.group_finity.mascot.Main");
    String[] dummy = {};
    Method main = clazz.getDeclaredMethod("main", String[].class);
    main.invoke(null, (Object) dummy);
  }
}
