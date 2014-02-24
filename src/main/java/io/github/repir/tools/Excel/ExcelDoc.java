/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.repir.tools.Excel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author jbpvuurens
 */
public class ExcelDoc {

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
         new java.io.File(filename).delete();
         fos = new FileOutputStream(filename);
         workbook.write(fos);
      } catch (IOException ex) {
         Logger.getLogger(ExcelDoc.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         try {
            fos.close();
         } catch (IOException ex) {
            Logger.getLogger(ExcelDoc.class.getName()).log(Level.SEVERE, null, ex);
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
         Logger.getLogger(ExcelDoc.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         try {
            fis.close();
         } catch (IOException ex) {
            Logger.getLogger(ExcelDoc.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
      return ret;
   }
}
