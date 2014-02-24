package io.github.repir.tools.Lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * These tools are wrappers around Java Reflection to get Constructors and
 * Methods described in Strings, without having to handle Exceptions. The static
 * functions have a normal type (e.g. getMthod() and invoke()) that trigger a
 * fatal exception if an exception occurs (to indicate results cannot be trusted
 * and errors must be fixed before tried again). If this is undesirable, the
 * try-methods (e.g. tryGetMethod(), tryInvoke()) will return null if an
 * exception occur.
 * <p/>
 * @author jeroen
 */
public class ClassTools {

   public static Log log = new Log(ClassTools.class);

   /**
    * @param methodclass Class for which the Method is requested
    * @param method Name of the method
    * @param parameters Classes of the parameters to use
    * @return A Method that can be invoked on an object of methodclass using the
    * given parameters. This method will exit with a fatalexception if the
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
    * @return A Method that can be invoked on an object of methodclass using the
    * given parameters or null if the requested method does not exist.
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
    * @param packagename Name of the package, which s used if the classname does
    * not include a package name
    * @param parent The Class that s constructed must be assignable from this
    * parent class otherwise the method exits with a fatal exception
    * @param parameters Classes that represent the parameters to be used for
    * construction.
    * @return see {@link #getConstructor(java.lang.Class, java.lang.Class[])},
    * or triggers a fatal exception if clazz in not assignable from parent.
    */
   public static Constructor getAssignableConstructor(Class clazz, Class parent, Class... parameters) {
      if (!parent.isAssignableFrom(clazz)) {
         log.fatal("Class %s must extend %s", clazz.getCanonicalName(), parent.getCanonicalName());
      }
      return getConstructor(clazz, parameters);
   }

   /**
    * This method exists with a fatal exception if clazz is not assignable from
    * parent.
    * <p/>
    * @param clazz Name of the class for which the constructor is requested
    * @param packagename Name of the package, which s used if the classname does
    * not include a package name
    * @param parent The Class that s constructed must be assignable from this
    * parent class otherwise the method exits with a fatal exception
    * @param parameters Classes that represent the parameters to be used for
    * construction.
    * @return see {@link #getConstructor(java.lang.Class, java.lang.Class[])} or
    * null if not assignable.
    */
   public static Constructor tryGetsignableConstructor(Class clazz, Class parent, Class... parameters) {
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
   public static Constructor getConstructor(Class clazz, Class... parameters) {
      Constructor constructor = null;
      try {
         constructor = clazz.getDeclaredConstructor(parameters);
         constructor.setAccessible(true);
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
   public static Constructor tryGetConstructor(Class clazz, Class... parameters) {
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
   public static Object construct(Constructor c, Object... params) {
      Object o = null;
      try {
         o = c.newInstance(params);
      } catch (Exception ex) {
         log.fatalexception(ex, "construct( %s, %s )", c, params);
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
    * @return The object returned by the invoked method or null of invoke fails.
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
    * @param packagenames optionally, names of packages to scan for a Class with
    * the given simple name.
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

   /**
    * @param clazz simple or canonical name of the Class Object to obtain
    * @param packagenames optionally, names of packages to scan for a Class with
    * the given simple name.
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
}
