/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.repir.tools.Excel;

import static io.github.repir.tools.Lib.PrintTools.*;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import io.github.repir.tools.DataTypes.Tuple2Comparable;

/**
 *
 * @author jbpvuurens
 */
public class ExcelSheet {

   public Sheet sheet;
   String name;
   ExcelDoc doc;

   public ExcelSheet(ExcelDoc doc, String name, Sheet sheet) {
      this.doc = doc;
      this.name = name;
      this.sheet = sheet;
   }

   public Cell getCell(int row, int column) {
      Row rr = sheet.getRow(row);
      if (rr == null) {
         rr = sheet.createRow(row);
      }
      Cell cell = rr.getCell(column);
      if (cell == null) {
         cell = rr.createCell(column);
      }
      return cell;
   }

   public int findColumn(int row, String value) {
      Row rr = sheet.getRow(row);
      for (Cell cell : rr) {
         if (cell.getCellType() == Cell.CELL_TYPE_STRING
                 && cell.getStringCellValue().equals(value)) {
            return cell.getColumnIndex();
         }
      }
      return -1;
   }

   public void setRow(int row, int startcolumn, Object... value) {
      ExcelCell cell = this.createCell(row, startcolumn);
      for (int i = 0; i < value.length; i++) {
         cell.set(value[i]);
         cell = cell.addcol(1);
      }
   }

   public void setRow(int row, int startcolumn, Map<Integer, Integer> map) {
      ExcelCell keycell = this.createCell(row, startcolumn);
      ExcelCell valuecell = this.createCell(row + 1, startcolumn);
      for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
         keycell.set(entry.getKey());
         valuecell.set(entry.getValue());
         keycell.column++;
         valuecell.column++;
      }
   }

   public void setColumn(int startrow, int column, Map<Integer, Integer> map) {
      ExcelCell keycell = this.createCell(startrow, column);
      ExcelCell valuecell = this.createCell(startrow, column+1);
      for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
         keycell.set(entry.getKey());
         valuecell.set(entry.getValue());
         keycell.row++;
         valuecell.row++;
      }
   }

   public static char columnToChar(int col) {
      return (char) ('A' + (col));
   }
   
   public ExcelCell createCell( int row, int col ) {
      return new ExcelCell( this, row, col );
   }
   
   public ExcelRange createRange( int row, int col, int rowend, int colend ) {
      return new ExcelRange( this, row, col, rowend, colend );
   }
   
   public ExcelCell createCell( String cell ) {
      return new ExcelCell( this, cell );
   }
   
   public ExcelRange createRange( ExcelCell start, ExcelCell end ) {
      return new ExcelRange( start, end );
   }
   
}
