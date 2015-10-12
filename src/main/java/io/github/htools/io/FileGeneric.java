package io.github.htools.io;

import java.io.FileWriter;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import static io.github.htools.lib.PrintTools.*;

public abstract class FileGeneric {

   public static Log log = new Log(FileGeneric.class);
   public static String ln = "\r\n";                // newline encoding, should be changed to \n for linux

   public abstract InputStream getInputStream() throws IOException;

   public abstract void close();

   public abstract void print(String s, Object... p);

   /**
    * Read the whole content through the Scanner and return the result as a
    * String
    * <p>
    * @return
    */
   public String read() throws IOException {
      String out = "";
      String lineseperator = System.getProperty("line.seperator");
      Scanner scanner = new Scanner(getInputStream());
      while (scanner != null && scanner.hasNext()) {
         out = out + scanner.nextLine() + "\n";
      }
      close();
      return out.substring(0, out.length() - 1);
   }

   /**
    * reads the next line. This method automatically opens the content if it
    * wasn't already, and automatically closes when the end was reached. This
    * means further reading past the end will reopen the content again and start
    * from the beginning.
    * <p>
    * @return line read, or null if there are no more lines.
    */
   public String readLine() throws IOException {
      Scanner scanner = new Scanner(getInputStream());
      if (scanner == null || !scanner.hasNext()) {
         close();
         return null;
      }
      return scanner.nextLine().replaceAll("[\n\r]+", "\n");
   }

   /**
    * reads the whole content, skipping empty lines and replacing Windows
    * newlines with linux ones.
    * <p>
    * @return whole content.
    */
   public String readCSV() throws IOException {
      log.info("readCSV");
      String tekst = "";
      close();
      Scanner scanner = new Scanner(getInputStream());
      while (scanner != null && scanner.hasNext()) {
         tekst = tekst + ((tekst.length() > 0) ? "\n" : "") + scanner.nextLine();
      }
      tekst = tekst.replaceAll("[\n\r]+", "\n") + "\n";
      close();
      log.info("exit readCSV");
      return tekst;
   }

   /**
    * reads the whole content as an array of bytes. This method opens and closes
    * the content.
    * <p>
    * @return
    */
   public byte[] readBytes() throws IOException {
      InputStream in = getInputStream();
      ArrayList<byte[]> blocks = new ArrayList<byte[]>();
      int maxblocksize = 1024 * 1024;                          // read 1MB at a startTime
      byte[] bytes = null, read = null;
      int offset = 0;
      int numRead = maxblocksize;
      try {
         while (numRead == maxblocksize) {
            read = new byte[maxblocksize];
            numRead = in.read(read, offset, maxblocksize);
            if (numRead == maxblocksize) {
               blocks.add(read);
               offset += maxblocksize;
            }
         }
         int length = numRead + blocks.size() * maxblocksize;
         bytes = new byte[length];
         for (int i = 0; i < blocks.size(); i++) {
            System.arraycopy(blocks.get(i), 0, bytes, i * maxblocksize, maxblocksize);
         }
         System.arraycopy(read, 0, bytes, blocks.size() * maxblocksize, numRead);
      } catch (IOException ex) {
         bytes = null;
         log.exception(ex, "readBytes() inputstream %s", in);
      }
      close();
      return bytes;
   }

   /**
    * print a c-format String {@link PrintTools#printf(java.lang.String, java.lang.Object[])
    * }
    * to the content without flushing the buffer.
    * <p>
    * @param str
    * @param o
    */
   public void printf(String str, Object... o) {
      print(sprintf(str, o));
   }

   /**
    * print the content to this file
    * <p>
    * @param filename
    */
   public void printToFile(String filename) {
      printToFile(new FSFile(filename));
   }

   /**
    * print the content to this file
    * <p>
    * @param file
    */
   public void printToFile(FSFile file) {
      FileWriter writer = file.getFileWriter();
      try {
         String line = null;
         while ((line = this.readLine()) != null) {
            writer.write(line);
         }
      } catch (IOException ex) {
         log.exception(ex, "printToFile( %s )", file);
      }
      file.close();
   }
}
