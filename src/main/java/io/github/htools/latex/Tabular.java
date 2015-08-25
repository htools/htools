package io.github.htools.latex;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;

/**
 *
 * @author Jeroen Vuurens
 */
public class Tabular {

   public static Log log = new Log(Tabular.class);
   public TableTemplate template;
   public ArrayList<Column> columns = new ArrayList<Column>();
   public String caption;
   public ArrayList<Row> rows = new ArrayList<Row>();
   public ByteRegex sep = new ByteRegex("[,\\|]");

   public Tabular( String columnids ) {
     set( columnids, new TableDefault(this)); 
   }
   
   public Tabular(String columnids, TableTemplate template) {
      set( columnids, template);
   }

   public void set(String columnids, TableTemplate template) {
      this.template = template;
      ArrayList<ByteSearchPosition> findAll = sep.findAllPos(columnids);
      ByteSearchPosition last = null;
      for (ByteSearchPosition p : findAll) {
         boolean right = (columnids.charAt(p.start) == '|');
         if (p.start == 0) {
            last = p;
         } else if (last == null) {
            addColumn(columnids.substring(0, p.start), false, right);
         } else {
            boolean left = (columnids.charAt(last.start) == '|');
            addColumn(columnids.substring(last.end, p.start), left, right);
         }
         last = p;
      }
      if (last.end < columnids.length()) {
         addColumn(columnids.substring(last.end), columnids.charAt(last.start) == '|', false);
      }
   }

   public void addColumn(String s, boolean left, boolean right) {
      s += "::::";
      String p[] = s.split(":", -1);
      Column c = new Column(columns.size(), p[0], p[1], p[2], left, right);
      columns.add(c);
      if (p[3].length() > 0) {
         Class clazz = ClassTools.toClass(p[3], ColumnFormatter.class.getPackage().getName());
         Constructor cons = ClassTools.tryGetAssignableConstructor(clazz, ColumnFormatter.class, Tabular.class, Integer.TYPE);
         ColumnFormatter cf = (ColumnFormatter) ClassTools.construct(cons, this, columns.size()-1);
         c.setFormatter(cf);
      }
   }

   public Cell set(int rownr, String column, Object value) {
      while (rownr >= rows.size()) {
         rows.add(new Row());
      }
      Row row = rows.get(rownr);
      Cell cell = new Cell(value, row, columns.get(getColumnID(column)));
      row.cells[getColumnID(column)] = cell;
      return cell;
   }

   public Column getColumn(String id) {
      return columns.get(getColumnID(id));
   }

   public Row getRow(int id) {
      return rows.get(id);
   }

   private int getColumnID(String id) {
      for (int i = 0; i < columns.size(); i++) {
         if (id.equals(columns.get(i).id)) {
            return i;
         }
      }
      return -1;
   }

   @Override
   public String toString() {
      StringBuilder doc = new StringBuilder();
      addHLine(doc);
      doc.append(getTop());
      doc.append(getColumnHeaders());
      addHLine(doc);
      for (Row row : rows) {
         StringBuilder r = new StringBuilder();
         for (int i = 0; i < columns.size(); i++) {
            if (row.cells[i] != null) {
               Cell c = row.cells[i];
               Column column = columns.get(i);
               for (CellModifier m : c.modifiers) {
                  m.modify(c);
               }
               if (c.userowmodifier) {
                  for (RowModifier m : row.modifiers) {
                     m.modify(c);
                  }
               }
               if (c.usecolumnmodifier) {
                  for (ColumnModifier m : column.modifiers) {
                     m.modify(c);
                  }
               }
               int columnspan = column.formatter.getColumnWidth(c.value);
               for (int j = 1; j < c.colspan; j++)
                  columnspan += columns.get(i+j).formatter.getColumnWidth("");
               if (columnspan > 1) {
                  Column last = columns.get(i + columnspan - 1);
                  String columndef = (i == 0 && column.leftbar ? "|" : "") + getAlign(c.align()) + (last.rightbar ? "|" : "");
                  r.append(PrintTools.sprintf("\\multicolumn{%d}{%s}{%s}", columnspan, columndef, c.value));
                  i += c.colspan - 1;
               } else {
                  r.append(c.value);
               }
            }
            if (i < columns.size() - 1) {
               r.append(" & ");
            }
         }
         r.append("\\\\\n");
         for (RowModifier m : row.modifiers) {
            m.modify(row, r);
         }
         doc.append(r);
      }

      doc.insert(0, template.header(this));
      doc.append(template.footer(this));
      return doc.toString();
   }

   public void addHLine(StringBuilder sb) {
      sb.append("\\hline\n");
   }

   private String getTop() {
      boolean hastop = false;
      for (Column c : columns) {
         if (c.top != null) {
            hastop = true;
         }
      }
      if (hastop) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < columns.size(); i++) {
            Column c = columns.get(i);
            if (c.top != null) {
               int end = i + 1;
               for (; end < columns.size() && columns.get(end).top == null; end++);
               String columndef = (i == 0 && c.leftbar ? "|" : "") + "c" + (columns.get(end - 1).rightbar ? "|" : "");
               int columnspan = c.formatter.getColumnWidth(c.top);
               for (int j = i + 1; j < end; j++)
                  columnspan += columns.get(j).formatter.getColumnWidth("");
               sb.append(PrintTools.sprintf("\\multicolumn{%d}{%s}{%s}", columnspan, columndef, c.top));
               if ((end - i) > 1) {
                  i = end - 1;
               }
               if (i + 1 < columns.size()) {
                  sb.append(" & ");
               }
            }
         }
         return sb.append("\\\\\n").toString();
      }
      return "";
   }

   private String getColumnHeaders() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < columns.size(); i++) {
         Column c = columns.get(i);
         if (c.header != null) {
            int end = i + 1;
            for (; end < columns.size() && columns.get(end).header == null; end++);
            String columndef = (i == 0 && c.leftbar ? "|" : "") + "c" + (columns.get(end - 1).rightbar ? "|" : "");
            int columnspan = c.formatter.getColumnWidth(c.header);
            for (int j = i + 1; j < end; j++)
               columnspan += columns.get(j).formatter.getColumnWidth("");
            sb.append(PrintTools.sprintf("\\multicolumn{%d}{%s}{%s}", columnspan, columndef, c.header));
            if ((end - i) > 1) {
               i = end - 1;
            }
            if (i + 1 < columns.size()) {
               sb.append(" & ");
            }
         }
      }
      return sb.append("\\\\\n").toString();
   }

   private String getAlign(ALIGN a) {
      switch (a) {
         case LEFT:
            return "l";
         case RIGHT:
            return "r";
         case CENTER:
            return "c";
      }
      return "";
   }

   String getColumnSpecs() {
      StringBuilder sb = new StringBuilder();
      boolean bar = false;
      for (Column c : columns) {
         bar |= c.leftbar;
         if (bar) {
            sb.append("|");
         }
         sb.append(c.formatter.getColumnSpec());
         bar = c.rightbar;
      }
      if (bar) {
         sb.append("|");
      }
      return sb.toString();
   }

   enum TYPE {

      STRING,
      DECIMAL,
      INT
   }

   enum ALIGN {

      LEFT,
      RIGHT,
      CENTER
   }

   public class Column {
      int sequence;
      String id, header, top;
      boolean leftbar, rightbar;
      ALIGN align = ALIGN.LEFT;
      TYPE type = TYPE.STRING;
      ColumnFormatter formatter;

      public Column(int sequence, String id, String header, String top, boolean leftbar, boolean rightbar) {
         this.sequence = sequence;
         this.id = id;
         this.leftbar = leftbar;
         this.rightbar = rightbar;
         this.header = (header == null || header.length() == 0) ? null : (header.equals("-")) ? "" : header;
         this.top = (top == null || top.length() == 0) ? null : (top.equals("-")) ? "" : top;
         formatter = template.columDefault(sequence);
      }

      public void format(Cell cell, Object value) {
         formatter.format(cell, value);
      }

      public void setFormatter(ColumnFormatter c) {
         formatter = c;
      }
      ArrayList<ColumnModifier> modifiers = new ArrayList<ColumnModifier>();
   }

   public class Row {

      Cell cells[];
      String pre, post;
      ArrayList<RowModifier> modifiers = new ArrayList<RowModifier>();

      public Row() {
         cells = new Cell[columns.size()];
      }

      public void addModifier(RowModifier r) {
         modifiers.add(r);
      }
   }

   public class Cell {
      String value;
      private TYPE type;
      private ALIGN align;
      Row row;
      Column column;
      int colspan = 0;
      boolean userowmodifier = true;
      boolean usecolumnmodifier = true;
      ArrayList<CellModifier> modifiers = new ArrayList<CellModifier>();

      public Cell(Object value, Row row, Column column) {
         this.row = row;
         this.column = column;
         column.format(this, value);
      }

      public ALIGN align() {
         return (align == null) ? column.align : align;
      }

      public TYPE type() {
         return (type == null) ? column.type : type;
      }

      public void setAlign(ALIGN align) {
         this.align = align;
      }

      public void setType(TYPE type) {
         this.type = type;
      }
   }
}
