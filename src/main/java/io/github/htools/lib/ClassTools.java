package io.github.htools.lib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * These tools are wrappers around Java Reflection to get Constructors and
 * Methods described in Strings, without having to handle Exceptions. The static
 * functions have a normal type (e.g. getMthod() and invoke()) that trigger a
 * fatal exception if an exception occurs (to indicate results cannot be trusted
 * and errors must be fixed before tried again). If this is undesirable, the
 * try-methods (e.g. tryGetMethod(), tryInvoke()) will return null if an
 * exception occur.
 * <p>
 * @author jeroen
 */
public enum ClassTools {

    ;

   public static Log log = new Log(ClassTools.class);

    /**
     * @param methodclass Class for which the Method is requested
     * @param method Name of the method
     * @param parameters Classes of the parameters to use
     * @return A Method that can be invoked on an object of methodclass using
     * the given parameters. This method will exit with a fatalexception if the
     * requested method does not exist.
     */
    public static Method getMethod(Class methodclass, String method, Class... parameters) {
        Method m = null;
        while (m == null && !methodclass.equals(Object.class)) {
            try {
                //log.info("getMethod %s %s %s", methodclass, methodclass.getCanonicalName(), method);
                m = methodclass.getMethod(method, parameters);
                return m;

            } catch (NoSuchMethodException ex) {
                Logger.getLogger(ClassTools.class.getName()).log(Level.SEVERE, null, ex);
                methodclass = methodclass.getSuperclass();
            } catch (SecurityException ex) {
                Logger.getLogger(ClassTools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return m;
    }

    /**
     * @param methodclass Class for which the Method is requested
     * @param method Name of the method
     * @param parameters Classes of the parameters to use
     * @return A Method that can be invoked on an object of methodclass using
     * the given parameters or null if the requested method does not exist.
     */
    public static Method tryGetMethod(Class methodclass, String method, Class... parameters) {
        Method m = null;
        while (m == null && !methodclass.equals(Object.class)) {
            try {
                //log.info("getMethod %s %s %s", methodclass, methodclass.getCanonicalName(), method);
                m = methodclass.getMethod(method, parameters);
                return m;

            } catch (NoSuchMethodException ex) {
                Logger.getLogger(ClassTools.class.getName()).log(Level.SEVERE, null, ex);
                methodclass = methodclass.getSuperclass();
            } catch (SecurityException ex) {
                Logger.getLogger(ClassTools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return m;
    }

    /**
     * @param clazz Name of the class for which the constructor is requested
     * @param parent The Class that s constructed must be assignable from this
     * parent class otherwise the method exits with a fatal exception
     * @param parameters Classes that represent the parameters to be used for
     * construction.
     * @return see {@link #getConstructor(java.lang.Class, java.lang.Class[])},
     * or triggers a fatal exception if clazz in not assignable from parent.
     */
    public static Constructor getAssignableConstructor(Class clazz, Class parent, Class... parameters) throws ClassNotFoundException {
        if (!parent.isAssignableFrom(clazz)) {
            throw new ClassNotFoundException(clazz.getCanonicalName());
        }
        return getConstructor(clazz, parameters);
    }

    /**
     * This method exists with a fatal exception if clazz is not assignable from
     * parent.
     * <p>
     * @param clazz Name of the class for which the constructor is requested
     * @param parent The Class that s constructed must be assignable from this
     * parent class otherwise the method exits with a fatal exception
     * @param parameters Classes that represent the parameters to be used for
     * construction.
     * @return see {@link #getConstructor(java.lang.Class, java.lang.Class[])}
     * or null if not assignable.
     */
    public static Constructor tryGetAssignableConstructor(Class clazz, Class parent, Class... parameters) {
        if (!parent.isAssignableFrom(clazz)) {
            return null;
        }
        return tryGetConstructor(clazz, parameters);
    }

    /**
     * @param clazz Class for which the constructor is requested
     * @param parameters Classes of the parameters to be used for construction
     * @return A Constructor that can be used to create a new instance using 
    * {@link java.lang.reflect.Constructor#newInstance(java.lang.Object[]) }
     * this method will exit with a fatal exception if the constructor does not
     * exist.
     */
    public static <O> Constructor<O> getConstructor(Class<O> clazz, Class... parameters) {
        Constructor constructor = null;
        try {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                log.error("getConstructor %s is abstract", clazz.getCanonicalName());
            } else {
                constructor = clazz.getDeclaredConstructor(parameters);
                constructor.setAccessible(true);
            }
        } catch (NoSuchMethodException ex) {
            log.fatalexception(ex, "getConstructor( %s, %s )", clazz, parameters);
        }
        return constructor;
    }

    /**
     * @param clazz Class for which the constructor is requested
     * @param parameters Classes of the parameters to be used for construction
     * @return A Constructor that can be used to create a new instance using 
    * {@link java.lang.reflect.Constructor#newInstance(java.lang.Object[]) }
     * or null if fails.
     */
    public static <O> Constructor<O> tryGetConstructor(Class<O> clazz, Class... parameters) {
        Constructor constructor = null;
        try {
            constructor = clazz.getDeclaredConstructor(parameters);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ex) {
        }
        return constructor;
    }

    /**
     * @param c Constructor that is used to instantiate a new Object
     * @param params Parameters to pass to the constructor
     * @return The constructed Object, or triggers a fatal exception when
     * instantiation fails
     */
    public static <O> O construct(Constructor<O> c, Object... params) {
        O o = null;
        try {
            o = c.newInstance(params);
        } catch (Exception ex) {
            log.fatalexception(ex, "construct( %s, %s )", c, ArrayTools.toString(params));
        }
        return o;
    }

    /**
     * @param c Constructor that is used to instantiate a new Object
     * @param params Parameters to pass to the constructor
     * @return The constructed Object or null if construction fails
     */
    public static Object tryConstruct(Constructor c, Object... params) {
        Object o = null;
        try {
            o = c.newInstance(params);
        } catch (Exception ex) {
        }
        return o;
    }

    /**
     * @param m Method to invoke using reflection on an object with given
     * parameters
     * @param object The object to invoke the method on, or null if it is a
     * static method.
     * @param params The parameters to pass to the method.
     * @return The object returned by the invoked method, or triggers a fatal
     * exception if invoke fails.
     */
    public static Object invoke(Method m, Object object, Object... params) {
        try {
            return m.invoke(object, params);
        } catch (Exception ex) {
            log.fatalexception(ex, "invoke( %s, %s, %s)", m, object, params);
        }
        return null;
    }

    /**
     * @param m Method to invoke using reflection on an object with given
     * parameters
     * @param object The object to invoke the method on, or null if it is a
     * static method.
     * @param params The parameters to pass to the method.
     * @return The object returned by the invoked method or null of invoke
     * fails.
     */
    public static Object tryInvoke(Method m, Object object, Object... params) {
        try {
            return m.invoke(object, params);
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * @param clazz simple or canonical name of the Class Object to obtain
     * @param packagenames optionally, names of packages to scan for a Class
     * with the given simple name.
     * @return Class object that corresponds to the clazz parameters, optionally
     * in one of the packages, or gives a fatal exception if it doesn't exist.
     */
    public static Class toClass(String clazz, String... packagenames) {
        Exception exception;
        try {
            return Class.forName(clazz);
        } catch (Exception ex) {
            exception = ex;
            if (ex instanceof ClassNotFoundException) {
                for (String packagename : packagenames) {
                    try {
                        String newclazz = packagename + "." + clazz;
                        //log.info("try %s", newclazz);
                        return Class.forName(newclazz);
                    } catch (Exception ex1) {
                    }
                }
            }
        }
        log.fatalexception(exception, "getClassName( %s, %s )", clazz, ArrayTools.toString(packagenames));
        return null;
    }

    public static void preLoad(Class... classes) {
        for (Class c : classes) {
            try {
                Class.forName(c.getCanonicalName());
            } catch (ClassNotFoundException ex) {
                log.fatalexception(ex, "preLoad %s", c.getCanonicalName());
            }
        }
    }

    /**
     * @param clazz simple or canonical name of the Class Object to obtain
     * @param packagenames optionally, names of packages to scan for a Class
     * with the given simple name.
     * @return if a packagename matches, the SimpleClassName, otherwise the
     * CanonicalName.
     */
    public static String stripPackageNames(String clazz, String... packagenames) {
        for (String packagename : packagenames) {
            int l = packagename.length();
            if (clazz.length() > l && clazz.charAt(l) == '.' && clazz.startsWith(packagename) && clazz.indexOf('.', l + 1) == -1) {
                return clazz.substring(l + 1);
            }
        }
        return clazz;
    }

    /**
     * @param clazz simple or canonical name of the Class Object to obtain
     * @param packagenames optionally, names of packages to scan for a Class
     * with the given simple name.
     * @return Class object that corresponds to the clazz parameters, optionally
     * in one of the packages, or null if it doesn't exist.
     */
    public static Class tryToClass(String clazz, String... packagenames) {
        try {
            return Class.forName(clazz);
        } catch (Exception ex) {
            for (String packagename : packagenames) {
                try {
                    String newclazz = packagename + "." + clazz;
                    return Class.forName(newclazz);
                } catch (Exception ex1) {
                }
            }
        }
        return null;
    }

    public static Class[] objectsToClass(Object... objects) {
        Class clazz[] = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            clazz[i] = objects[i].getClass();
        }
        return clazz;
    }

    public static Object newInstance(Object o, Object... parameters) {
        if (o == null) {
            return null;
        }
        Class clazz = o.getClass();
        Constructor cons = getConstructor(clazz, objectsToClass(parameters));
        return construct(cons, parameters);
    }

    public static Class getGenericType2(Object c) {
        Class generic = (Class) ((ParameterizedType) c.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return generic;
    }

    // David: I didn't know how to fix this, but it seems that this method is not used anywhere
//    public static Class getGenericType(Object c) {
//        String generic = ((ParameterizedType) c.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
//        return toClass(generic);
//    }
    public static <S, B extends S> Class[] findTypeParameters(Class<B> base, Class<S> superClass) {
        Class[] actuals = new Class[0];
        for (Class clazz = base; !clazz.equals(superClass); clazz = clazz.getSuperclass()) {
            if (!(clazz.getGenericSuperclass() instanceof ParameterizedType) && !clazz.equals(superClass)) {
                continue;
            }

            Type[] types = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
            Class[] nextActuals = new Class[types.length];
            for (int i = 0; i < types.length; i++) {
                log.info("findType i, %s %s", i, nextActuals[i], types[i]);
                if (types[i] instanceof Class) {
                    nextActuals[i] = (Class) types[i];
                } else {
                    nextActuals[i] = map(clazz.getTypeParameters(), types[i], actuals);
                }
            }
            actuals = nextActuals;
        }
        return actuals;
    }

    private static Class map(Object[] variables, Object variable, Class[] actuals) {
        for (int i = 0; i < variables.length && i < actuals.length; i++) {
            if (variables[i].equals(variable)) {
                return actuals[i];
            }
        }
        return null;
    }

    public static ArrayList<Class> getAssignableClassesFromJar(Class superclass) throws IOException {
        ArrayList<Class> list = new ArrayList();
        for (String c : getClassesFromJars(superclass)) {
            try {
                Class clazz = Class.forName(c);
                if (clazz.isAssignableFrom(superclass)) {
                    list.add(clazz);
                }
            } catch (Exception ex) {
            }
        }
        return list;
    }

    public static ArrayList<String> getClassesInPackage(Class superclass) throws IOException {
        String packagepath = superclass.getPackage().getName();
        ArrayList<String> list = new ArrayList();
        for (String c : getClassesFromJars(superclass)) {
            if (c.startsWith(packagepath)) {
                list.add(c);
            }
        }
        return list;
    }

    public static ArrayList<String> getClassesFromJars(Class superclass) throws IOException {
        ArrayList<String> list = new ArrayList();
        URL jar = getJarLocation(superclass);
        if (jar != null) {
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            for (ZipEntry ze = zip.getNextEntry(); ze != null; ze = zip.getNextEntry()) {
                String entryName = ze.getName();
                if (entryName.endsWith(".class") && !entryName.contains("$")) {
                    String classname = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                    list.add(classname);
                }
            }
        }
        return list;
    }

    public static ArrayList<String> getFilesFromJars(Class superclass) throws IOException {
        ArrayList<String> list = new ArrayList();
        URL jar = getJarLocation(superclass);
        if (jar != null) {
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            for (ZipEntry ze = zip.getNextEntry(); ze != null; ze = zip.getNextEntry()) {
                String entryName = ze.getName();
                list.add(entryName);
            }
        }
        return list;
    }

    public static ArrayList<String> getClassesFromJars(String jarfile) throws IOException {
        ArrayList<String> list = new ArrayList();
        URL jar = new URL(jarfile);
        if (jar != null) {
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            for (ZipEntry ze = zip.getNextEntry(); ze != null; ze = zip.getNextEntry()) {
                String entryName = ze.getName();
                if (entryName.endsWith(".class") && !entryName.contains("$")) {
                    String classname = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                    list.add(classname);
                }
            }
        }
        return list;
    }

    /**
     * @return A URL that points to the .jar file containing clazz
     */
    public static URL getJarLocation(Class clazz) {
        CodeSource src = clazz.getProtectionDomain().getCodeSource();
        if (src != null) {
            return src.getLocation();
        }
        return null;
    }

    public static URL getJarLocation(String clazz) {
        ClassLoader loader = ClassTools.class.getClassLoader();
        return loader.getResource(clazz);
    }

    public static Class getMainClass() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement main = stack[stack.length - 1];
        String mainClass = main.getClassName();
        return ClassTools.tryToClass(mainClass);
    }
}
