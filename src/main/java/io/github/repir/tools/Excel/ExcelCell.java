package io.github.repir.tools.Excel;

import org.apache.poi.ss.usermodel.Cell;
import static io.github.repir.tools.Lib.PrintTools.*;

public class ExcelCell {
   ExcelSheet sheet;
   public int row;
   public int column;

   protected ExcelCell( ExcelSheet sheet, int row, int col) {
      this.sheet = sheet;
      this.row = row;
      this.column = col;
   }

   protected ExcelCell( ExcelSheet sheet, String cell) {
      this.sheet = sheet;
      int i = 0;
      for (; i < cell.length(); i++) {
         if (cell.charAt(i) >= 'A' && cell.charAt(i) <= 'Z') {
            column *= 26;
            column += (cell.charAt(i) - 'A');
         }
      }
      for (; i < cell.length(); i++) {
         if (cell.charAt(i) >= '0' && cell.charAt(i) <= '9') {
            row *= 10;
            row += cell.charAt(i) - '0';
         }
      }
      row--;
   }

   public ExcelCell addrow(int row) {
      return new ExcelCell(this.sheet, this.row + row, this.column);
   }

   public ExcelCell addcol(int col) {
      return new ExcelCell(this.sheet, this.row, this.column + col);
   }
   
   public ExcelCell right() {
      return addcol(1);
   }

   public ExcelCell left() {
      return addcol(-1);
   }

   public ExcelCell above() {
      return addrow(-1);
   }

   public ExcelCell below() {
      return addrow(1);
   }

   public String toString() {
      int firstchar = column / 26;
      int secondchar = column % 26;
      if (firstchar > 0) {
         return sprintf("%s%s%d", ExcelSheet.columnToChar(firstchar - 1), ExcelSheet.columnToChar(secondchar), row + 1);
      } else {
         return sprintf("%s%d", ExcelSheet.columnToChar(secondchar), row + 1);
      }
   }
  
   public void set( double d ) {
      getCell().setCellValue(d);
   }
   
   public void set( int i ) {
      getCell().setCellValue(i);
   }
   
   public void set( String s ) {
      getCell().setCellValue( s);
   }
   
   public void format(String name) {
      getCell().setCellStyle(sheet.doc.getStyle(name));
   }

   public void format(String name, String style) {
      getCell().setCellStyle(sheet.doc.getStyle(name, style));
   }

   public void set(Integer i) {
      getCell().setCellValue(i);
   }
   public void set(Long i) {
      getCell().setCellValue(i);
   }
   public void set(Double i) {
      getCell().setCellValue(i);
   }
   
   public void set( Object o ) {
      if (o instanceof Integer)
         set( (Integer) o );
      else if ( o instanceof Double )
         set( (Double) o );
      else if ( o instanceof String )
            set( (String) o );
   }
   
   public void setCellFormula(String value) {
      getCell().setCellFormula(value);
   }

   public void setCellFormula(String value, Object... o) {
      getCell().setCellFormula(io.github.repir.tools.Lib.PrintTools.sprintf(value, o));
   }

   public void setFormatDouble( double d ) {
      getCell().setCellValue(d);
      getCell().setCellStyle(sheet.doc.getStyle("double6", "0.000000"));
   }
   
   public void setFormatPerc( double d ) {
      set(d);
      format("perc2", "0.00%");
   }
   
   public Cell getCell() {
      return sheet.getCell(row, column);
   }
}
