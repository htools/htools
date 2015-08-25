package io.github.htools.io;

import io.github.htools.lib.Log;
import static io.github.htools.lib.PrintTools.sprintf;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * An StringFile holds a piece of memory, and comes with a whole set of methods
 * to access that memory like reading from it and writing to it, like you can do
 * with files. StringFile is more or less intended as a primitive version that
 * keeps the data in a String in memory, but this can be overriden in a subclass
 * that implements storage in any form, and uses the same interface, like RFile.
 * <br><br> The main reason to create this class is to remove all Java fuzz
 * regarding the use of the correct input and output classes. From an StringFile
 * you can just read a line, read the whole, or you can write stuff to it. It
 * automatically creates all objects created to complete the task. If required,
 * you can still obtain objects such as the InputStream and BufferedWriter to
 * access the memory
 * <p>
 * @author jbpvuurens
 */
public class StringFile extends FileGeneric {

   public static Log log = new Log(StringFile.class);
   public String content = null;

   private enum Status {

      CLOSED, // The content is not readable or writeable
      SCANNER, // The content is readable as a Scanner
      BUFFEREDWRITER, // The content is writeable as a BufferedWriter
      BUFFEREDWRITERUTF8, // The content is writeable as a BufferedWriter
      INCHANNEL, // The content is readable as an InputStream
      OUTCHANNEL                  // The content is writeable as an OutputStream
   }
   private Status status = Status.CLOSED;            // initialize the content as closed
   public Scanner scanner = null;
   public Writer writer = null;
   public InputStream in = null;
   public BufferedWriter bufferedwriter = null;

   public StringFile(String content) {
      this.content = content;
   }

   /**
    * Closes the buffer
    */
   public void close() {
      try {
         switch (status) {
            case SCANNER:
               scanner.close();
               in.close();
               in = null;
               scanner = null;
               status = Status.CLOSED;
               break;
            case BUFFEREDWRITER:
               content = writer.toString();
               bufferedwriter.close();
               writer.close();
               bufferedwriter = null;
               writer = null;
               status = Status.CLOSED;
               break;
         }
      } catch (IOException e) {
      }
   }

   /**
    * opens the buffer for reading (SCANNER) or writing (bufferedwriter).
    * <p>
    * @param s
    * @return True if opened
    */
   public boolean open(Status s) {
      log.debug("open( status = %s )", s.toString());
      if (status == s) {
         return true;
      }
      if (status != Status.CLOSED) {
         close();
      }
      switch (s) {
         case SCANNER:
            scanner = new Scanner(content);
            status = s;
            return true;
         case BUFFEREDWRITER:
            writer = new StringWriter();
            bufferedwriter = new BufferedWriter(writer);
            status = s;
            return true;
      }
      return false;
   }

   /**
    * Puts the content in the Scanner state (if not already) and returns the
    * Scanner.
    * <p>
    * @return
    */
   public Scanner getScanner() {
      open(Status.SCANNER);
      return scanner;
   }

   /**
    * Puts the content in the Scanner state (if not already) and returns the
    * Scanner.
    * <p>
    * @return
    */
   public BufferedWriter getBufferedWriter() {
      open(Status.BUFFEREDWRITER);
      return bufferedwriter;
   }

   /**
    * print a string to the content storage and flush the buffer.
    * <p>
    * @param str
    */
   public void print(String str, Object... o) {
      if (status != Status.BUFFEREDWRITER) {
         open(Status.BUFFEREDWRITER);
      }
      try {
         String out = "";
         String lineseperator = System.getProperty("line.seperator");
         if (o.length == 0) {
            bufferedwriter.write(str);
         } else {
            bufferedwriter.write(sprintf(str, o));
         }
      } catch (IOException ex) {
         log.exception(ex, "print( %s )", str);
      }
   }

   /**
    * return the SHA1 code for the content.
    * <p>
    * @return
    */
   public String getSHA1() {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA"); // SHA or MD5
         String hash = "";

         this.open(Status.INCHANNEL);
         byte[] data = this.readBytes();
         md.update(data); // Reads it all at one go. Might be better to chunk it.

         byte[] digest = md.digest();

         for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(digest[i]);
            if (hex.length() == 1) {
               hex = "0" + hex;
            }
            hex = hex.substring(hex.length() - 2);
            hash += hex;
         }

         return hash;

      } catch (NoSuchAlgorithmException ex) {
         log.exception(ex, "getSHA1()");
      }
      return null;
   }

   /**
    * Puts the content in the Scanner state (if not already) and returns the
    * InputStream.
    * <p>
    * @return
    */
   public InputStream getInputStream() {
      open(Status.SCANNER);
      return in;
   }
}
