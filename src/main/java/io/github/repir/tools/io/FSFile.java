package io.github.repir.tools.io;

import static io.github.repir.tools.lib.Const.*;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is intended to remove all the Java fuzz regarding files. There is
 * just one class FSFile that provides methods to read a line, read the entire
 * thing, write stuff to it, without having bother about which stream to use.
 * However, Java objects like properly opened FileInputStream and FileChannel.
 * <br><br> Some methods are provided that will more easily allow to get
 * information on the file, such as the parent Dir object, the filename,
 * extension, etc. <br><br> Some static methods are provided to do big file
 * operations, such as copying, moving, running and converting a File to a
 * primitive.
 * <p/>
 * @author jbpvuurens
 */
public class FSFile extends FileGeneric {

   private static Log log = new Log(FSFile.class);
   protected FileOutputStream outputstream = null;
   public FileChannel channel;
   private String fullpathname;
   public File file;
   public Scanner scanner = null;
   public FileChannel filechannelin = null;
   public FileChannel filechannelout = null;
   public FileWriter filewriter = null;
   public PrintWriter printwriter = null;
   protected InputStream inputstream = null;

   /**
    *
    * @param fullpathname
    */
   public FSFile(String fullpathname) {
      this(new File(fullpathname));
   }

   public FSFile(InputStream is) {
      this.inputstream = is;
   }

   public static String tempfilename(String filename, String dir) {
      if (dir == null) {
         dir = System.getProperty("java.io.tmpdir");
      }
      return dir + "/" + filename;
   }

   protected FSFile(File file) {
      //super("");
      try {
         this.file = file;
         setFullPathName(file.getCanonicalPath());
      } catch (IOException ex) {
         Logger.getLogger(FSFile.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   @Override
   public void print(String s, Object... p) {
      FileWriter filewriter = getFileWriter();
      try {
         if (p.length == 0) {
            filewriter.write(s);
         } else {
            filewriter.write(PrintTools.sprintf(s, p));
         }
      } catch (IOException ex) {
         log.exception(ex, "print( %s ) when writing file %s", s, filewriter);
      }
   }

   public long getLength() {
      return file.length();
   }

    public byte[] readFully() throws EOCException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = getInputStream().read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }
   /**
    * @return true if file exists
    */
   public boolean exists() {
      return file.isFile();
   }

   public static boolean exists(String filename) {
      return new File(filename).isFile();
   }

   /**
    * @return name of the last component of the path
    */
   public String getFilename() {
      int slashpos = getFullPathName().lastIndexOf('/');
      String f = getFullPathName().substring(slashpos + 1);
      return f;
   }

   /**
    *
    * @param fullpathname
    */
   public void setFullPathName(String fullpathname) {
      this.fullpathname = fullpathname.replace('\\', '/').replace("%20", " ").replace("file:///", "");
   }

   /**
    *
    * @param jfile
    * @return
    */
   public boolean equals(FSFile jfile) {
      return getFullPathName().equalsIgnoreCase(jfile.getFullPathName());
   }

   /**
    *
    * @return
    */
   public String getFullPathName() {
      return fullpathname;
   }

   /**
    *
    * @return
    */
   public String getPath() {
      int slashpos = getFullPathName().lastIndexOf('/');
      String f = getFullPathName().substring(0, slashpos + 1);
      return f;
   }

   /**
    *
    * @return
    */
   public FSPath getFSDir() {
      return new FSPath(this.getPath());
   }

   /**
    *
    * @return
    */
   public String getFilenameWithoutExt() {
      String f = getFilename();
      int periodpos = f.lastIndexOf('.');
      if (periodpos > 0) {
         f = f.substring(0, periodpos);
      }
      return f;
   }

   /**
    *
    * @return
    */
   public String getExtension() {
      int periodpos = getFullPathName().lastIndexOf('.');
      String extension = getFullPathName().substring(periodpos + 1);
      return extension;
   }

   public void close() {
      try {
         if (outputstream != null) {
            outputstream.close();
            outputstream = null;
         }
         if (inputstream != null) {
            inputstream.close();
            inputstream = null;
         }
         if (filewriter != null) {
            filewriter.close();
            filewriter = null;
         }
         if (printwriter != null) {
            printwriter.close();
            printwriter = null;
         }
         if (filechannelin != null) {
            filechannelin.close();
            filechannelin = null;
         }
         if (filechannelout != null) {
            filechannelout.close();
            filechannelout = null;
         }
      } catch (IOException ex) {
         log.exception(ex, "close() when closing");
      }
   }

   public InputStream getInputStream() {
      try {
         if (inputstream == null) {
            inputstream = new FileInputStream(getFullPathName());
         }
      } catch (FileNotFoundException ex) {
         log.exception(ex, "getInputStream()");
      }
      return inputstream;
   }

   public FileWriter getFileWriter() {
      try {
         if (filewriter != null) {
            createIfNotExists();
            outputstream = new FileOutputStream(file);
            filewriter = new FileWriter(getFullPathName());
         }
      } catch (IOException ex) {
         log.exception(ex, "getFileWriter() file %s", file);
      }
      return filewriter;
   }

   public PrintWriter getPrintWriter() {
      try {
         if (printwriter != null) {
            createIfNotExists();
            printwriter = new PrintWriter(file.getCanonicalPath(), "UTF-8");
         }
      } catch (IOException ex) {
         log.exception(ex, "getPrintWriter() file %s", printwriter);
      }
      return printwriter;
   }

   public FileChannel getFileChannelIn() {
      if (filechannelin != null) {
         filechannelin = ((FileInputStream) getInputStream()).getChannel();
      }
      return filechannelin;
   }

   public FileChannel getFileChannelOut() {
      if (filechannelout != null) {
         filechannelout = ((FileOutputStream) getOutputStream()).getChannel();
      }
      return filechannelout;
   }

   public void createIfNotExists() {
      if (!file.isFile()) {
         try {
            this.getFSDir().mkdirs();
            file.createNewFile();
         } catch (IOException ex) {
            log.exception(ex, "createIfNotExists() file %s", file);
         }
      }
   }

   public OutputStream getOutputStream() {
      try {
         if (outputstream == null) {
            createIfNotExists();
            outputstream = new FileOutputStream(file);
         }
      } catch (IOException ex) {
         log.exception(ex, "getOutputStream() file %s", file);
      }
      return outputstream;
   }

   /**
    *
    * @param file
    * @return
    */
   public static String fileToString(java.io.File file) {
      FSFile fc = new FSFile(file);
      if (fc.exists()) {
         return fc.read();
      } else {
         return "";
      }
   }

   /**
    *
    * @param filename
    * @return
    */
   public static String fileToString(String filename) {
      FSFile fc = new FSFile(filename);
      if (fc.exists()) {
         return fc.read();
      } else {
         return "";
      }
   }

   /**
    *
    * @param file
    * @return
    */
   public static byte[] fileToBytes(java.io.File file) {
      FSFile fc = new FSFile(file);
      if (fc.exists()) {
         return fc.readBytes();
      } else {
         return null;
      }
   }

   /**
    *
    * @param file
    * @param contents
    */
   public static void stringToFile(String file, String contents) {
      FSFile fc = new FSFile(file);
      fc.print(contents);
      fc.close();
   }

   /**
    *
    * @param file
    * @param contents
    */
   public static void stringToFile(java.io.File file, String contents) {
      FSFile fc = new FSFile(file);
      fc.print(contents);
      fc.close();
   }

   /**
    *
    * @param target
    * @return
    */
   public boolean copyFile(FSFile target) {
      try {
         FileChannel in = getFileChannelIn();
         FileChannel out = target.getFileChannelOut();
         in.transferTo(0, file.length(), out);
         this.close();
         target.close();
         return true;
      } catch (IOException ex) {
         return false;
      }
   }

   /**
    *
    * @param dest
    * @return
    */
   public boolean moveFile(FSFile dest) {
      boolean success = this.file.renameTo(dest.file);
      return success;
   }

   /**
    *
    * @return
    */
   public boolean delete() {
      boolean succes = this.file.delete();
      return succes;
   }

   /**
    *
    */
   public void run() {
      try {
         if (this.exists()) {
            if (Desktop.isDesktopSupported()) {
               Desktop.getDesktop().open(this.file);
            } else {
               log.warn("Awt Desktop is not supported!");
            }
         } else {
            System.out.println("File is not exists!");
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }
}
