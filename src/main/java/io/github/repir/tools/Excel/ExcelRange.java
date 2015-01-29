package io.github.repir.tools.Excel;

import org.apache.poi.ss.usermodel.Cell;
import io.github.repir.tools.lib.PrintTools;

public class ExcelRange {

   public ExcelCell start;
   public ExcelCell end;

   public ExcelRange(ExcelCell start, ExcelCell end) {
      this.start = start;
      this.end = end;
   }

   public ExcelRange(ExcelSheet sheet, int row, int col, int rowend, int colend) {
      this.start = new ExcelCell(sheet, row, col);
      this.end = new ExcelCell(sheet, rowend, colend);
   }

   public ExcelRange(ExcelSheet sheet, String range) {
      String[] cells = range.split(":");
      this.start = new ExcelCell(sheet, cells[0]);
      this.end = new ExcelCell(sheet, cells[1]);
   }

   public String toString() {
      return start + ":" + end;
   }

   public String toSheetString() {
      return start.sheet.name + "!" + start + ":" + end;
   }

   public void setAvg(ExcelCell result) {
      setAvg( result.getCell() );
   }

   public void setAvg(Cell result) {
      if (result.getSheet() == this.start.sheet.sheet) {
         result.setCellFormula(PrintTools.sprintf("AVERAGE(%s)", this));
      } else {
         result.setCellFormula(PrintTools.sprintf("AVERAGE(%s)", this.toSheetString()));
      }
   }

   public void setSum(ExcelCell result) {
      setSum( result.getCell() );
   }

   public void setSum(Cell result) {
      if (result.getSheet() == this.start.sheet.sheet) {
         result.setCellFormula(PrintTools.sprintf("SUM(%s)", this));
      } else {
         result.setCellFormula(PrintTools.sprintf("SUM(%s)", this.toSheetString()));
      }

   }
}
