package io.github.repir.tools.io;

import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;
import java.io.InputStream;

/**
 * This class reads contents from a resource in a jar file. The resource should be
 * an absolute path, without a leading slash, e.g. "resource/settings.txt".
 * <p/>
 * @author jbpvuurens
 */
public class ResourceDataIn extends ISDataIn {

   public static Log log = new Log(ResourceDataIn.class);
   private Class clazz;
   private String path;

   /**
    * Possibly, if multiple jars contain the same resource, a class from the
    * correct jar should be passed. Have not tested this though.
    * @param clazz
    * @param resourcepath 
    */
   public ResourceDataIn(Class clazz, String resourcepath) {
       this.clazz = clazz;
       this.path = resourcepath;
       reopen();
   }

   public ResourceDataIn(String resourcepath) {
       this(ResourceDataIn.class, resourcepath);
   }

   private void reopen() {
      resetOffset();
      this.close();
      getInputStream();
   }
   
   @Override
   public void mustMoveBack() {
      reopen();
   }
   
    @Override
    public InputStream getInputStream() {
        if (inputstream == null)
           inputstream = clazz.getClassLoader().getResourceAsStream(path);
        return inputstream;
    }
}
