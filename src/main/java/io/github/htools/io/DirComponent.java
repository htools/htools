package io.github.htools.io;

/**
 *
 * @author jeroen
 */
public interface DirComponent {

   String getCanonicalPath();
   
   String getName();

   boolean exists();
}
