package io.github.htools.io.buffer;

import io.github.htools.lib.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BufferSerialization {

   public static Log log = new Log(BufferSerialization.class);
   ByteArrayOutputStream bos;
   ObjectOutputStream oos;
   ByteArrayInputStream bis;
   ObjectInputStream ois;

   public ObjectOutputStream writer() {
      if (bos == null) {
         try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
         } catch (IOException ex) {
            log.fatalexception(ex, "writer() bos %s", bos);
         }
      }
      return oos;
   }

   public byte[] close() {
      byte[] yourBytes = null;
      try {
         yourBytes = bos.toByteArray();
         oos.close();
         bos.close();
         oos = null;
         bos = null;
      } catch (IOException ex) {
      }
      return yourBytes;
   }

   public void open(byte b[]) {
      try {
         bis = new ByteArrayInputStream(b);
         ois = new ObjectInputStream(bis);
      } catch (IOException ex) {
         log.fatalexception(ex, "open( %s )", b);
      }
   }

   public ObjectInputStream reader() {
      return ois;
   }
}
