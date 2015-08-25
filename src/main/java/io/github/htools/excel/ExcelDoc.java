/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.htools.excel;

import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author jbpvuurens
 */
public class ExcelDoc {
   public static Log log = new Log(ExcelDoc.class);
   HashMap<String, ExcelSheet> sheets = new HashMap<String, ExcelSheet>();
   HashMap<String, XSSFCellStyle> styles = new HashMap<String, XSSFCellStyle>();
   XSSFWorkbook workbook;
   public String filename;

   private ExcelDoc(String filename, XSSFWorkbook wb) {
      this.filename = filename;
      workbook = wb;
   }

   public ExcelDoc(String filename) {
      this.filename = filename;
      workbook = new XSSFWorkbook();
   }

   public ExcelSheet getSheet(String name) {
      if (sheets.containsKey(name)) {
         return sheets.get(name);
      } else {
         Sheet sheet = workbook.createSheet(name);
         ExcelSheet excelsheet = new ExcelSheet(this, name, sheet);
         sheets.put(name, excelsheet);
         return excelsheet;
      }
   }

   public void setSheet(String name, Sheet sheet) {
      ExcelSheet excelsheet = new ExcelSheet(this, name, sheet);
      sheets.put(name, excelsheet);
   }

   public XSSFCellStyle getStyle(String name) {
      if (!styles.containsKey(name)) {
         styles.put(name, workbook.createCellStyle());
      }
      return styles.get(name);
   }

   public XSSFCellStyle getStyle(String name, String format) {
      if (!styles.containsKey(name)) {
         setStyle(name, format );
      }
      return styles.get(name);
   }

   public void setStyle(String name, String format) {
      getStyle(name).setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(format));
   }

   public void write() {
      FileOutputStream fos = null;
      try {
         log.info("filename %s", filename);
         Datafile df = new Datafile(filename);
         if (df.exists())
            df.delete();
         fos = new FileOutputStream(filename);
         log.info("fos %s", fos);
         workbook.write(fos);
         log.info("fos %s", fos);
      } catch (IOException ex) {
         log.fatalexception(ex, "write(%s)", filename);
      } finally {
         try {
            if (fos != null)
            fos.close();
         } catch (IOException ex) {
            log.fatalexception(ex, "write(%s)", filename);
         }
      }
   }

   public static ExcelDoc read(String filename) {
      FileInputStream fis = null;
      ExcelDoc ret = null;
      try {
         fis = new FileInputStream(filename);
         XSSFWorkbook wb = new XSSFWorkbook(fis);
         ret = new ExcelDoc(filename, wb);
         for (int s = 0; s < wb.getNumberOfSheets(); s++) {
            ret.setSheet(wb.getSheetName(s), wb.getSheetAt(s));
         }
         for (int sh = 0; sh < wb.getNumberOfSheets(); sh++) {
            ret.getSheet(wb.getSheetName(sh));
         }
      } catch (IOException ex) {
         log.fatalexception(ex, "read( %s )", filename);
      } finally {
         try {
            fis.close();
         } catch (IOException ex) {
            log.fatalexception(ex, "read( %s )", filename);
         }
      }
      return ret;
   }
}
