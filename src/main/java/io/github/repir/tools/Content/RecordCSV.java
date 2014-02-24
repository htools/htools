package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import io.github.repir.tools.ByteRegex.ByteRegex;

/**
 * The TupleData class provides a toolkit to store data in a datafile that
 * extends {@link StructuredDataStream} to implement a table of
 * rows/records/tuples with a fixed order of columns/fields. Accessing the data
 * through the {@link StructuredDataStream} protects data integrity by forcing
 * to read and write records containing all fields in a fixed order as defined
 * in the {@link StructuredDataStream}'s structure. The default implementation
 * uses a {@link Datafile} for storage, but alternatively
 * {@link BufferReaderWriter} can be used to read and write tuples to a memory
 * buffer. When reading tuples, the end is reached on EOF, or when a preset
 * ceiling is encountered.
 * <p/>
 * To use: create a new class that extends TupleData. For the fields, create
 * class variables of any type that extends one of the nested classes of
 * {@link StructuredDataStream.Element}. Override {@link #initStructure()} to
 * add the fields to the TupleData structure, in that order.
 * <p/>
 * @author jeroen
 */
public abstract class RecordCSV extends StructuredCSVStream {

   public Log log = new Log(RecordCSV.class);
   Datafile datafile;
   ByteRegex whitespace = new ByteRegex("\\s+");
   int tupleswritten;

   /**
    * constructor to read/write tuples to a {@link Datafile}
    * <p/>
    * @param datafile
    */
   public RecordCSV(Datafile datafile) {
      super();
      setDatafile(datafile);
   }

   /**
    * constructor to read tuples from a byte array, using
    * {@link BufferReaderWriter}
    * <p/>
    * @param bytes a byte[] that contains the tuple table to be read.
    */
   public RecordCSV(byte[] bytes) {
      super(new BufferReaderWriter(bytes));
   }

   /**
    * constructor to write tuples to a byte array, using
    * {@link BufferReaderWriter} and {@link BytesOut}
    * <p/>
    * @param bytes the {@link BytesOut} object that will contain the byte output
    * of tuples written.
    */
   public RecordCSV(BytesOut bytes) {
      super(new BufferReaderWriter(bytes));
   }

   protected void setDatafile(Datafile df) {
      this.datafile = df;
      if (df != null) {
         this.writer = df.rwbuffer;
         this.reader = df.rwbuffer;
      } else {
         this.writer = null;
         this.reader = null;
      }
   }

   /**
    * @return {@link Datafile} that the TupleData class read/writes to.
    */
   public Datafile getDatafile() {
      return this.datafile;
   }

   /**
    * moves the offset of the file/memory buffer being read. Only useful if you
    * know the exact offset at which a TupleData starts.
    * <p/>
    * @param offset
    */
   public void setOffset(long offset) {
      if (datafile != null) {
         this.datafile.setOffset(offset);
      } else {
         reader.setOffset(offset);
      }
   }

   /**
    * Setting a ceiling ensures that no data is read beyond that point, as if
    * the tuple table ends there.
    * <p/>
    * @param ceiling
    */
   public void setCeiling(long ceiling) {
      if (datafile != null) {
         this.datafile.setCeiling(ceiling);
      } else {
         reader.setCeiling(ceiling);
      }
   }

   /**
    * The size of the memory buffer used. For large sequential tables, a larger
    * buffer size reduces the amount of read actions, increasing performance. No
    * checking is done with respect to available memory.
    * <p/>
    * @param size
    */
   public void setBufferSize(int size) {
      if (writer != null) {
         writer.setBufferSize(size);
      }
      if (reader != null) {
         reader.setBufferSize(size);
      }
   }

   /**
    * current contains the next field in the structure, if this is null, it
    * means a whole row was read/written. In TupleData the structure
    * automatically resets to the beginning to allow iterative reading/writing
    * of rows.
    */
   @Override
   public void posMoved() {
      if (nextField == null) {
         resetNextField();
      }
   }

   /**
    * @return the current offset in the file or buffer that is being
    * read/written
    */
   public long getOffset() {
      if (reader != null) {
         return reader.getOffset();
      }
      if (writer != null) {
         return writer.getOffset();
      }
      return -1;
   }

   /**
    * @return the current ceiling that is set for reading Tuples
    */
   public long getCeiling() {
      return reader.getCeiling();
   }

   /**
    * hook to view back on the record that is last written. This is manly used
    * for sorted implementations of TupleData, allowing them to cache & sort
    * records before being written.
    * <p/>
    * @param tuple
    */
   public void hookRecordWritten(RecordCSV tuple) {
   }

   /**
    * resets the structure pointer to the first field, to start reading/writing
    * a record at he current offset.
    */
   @Override
   public void resetNextField() {
      nextField = start.next();
   }

   /**
    * open a TupleData in writing mode.
    */
   public void openWrite() {
      if (datafile != null) {
         datafile.openWrite();
         this.writer = datafile.rwbuffer;
      }
   }

   /**
    * open a TupleData in reading mode
    */
   public void openRead() {
      if (datafile != null) {
         datafile.openRead();
         this.reader = datafile.rwbuffer;
      }
   }

   /**
    * closes the TupleData stream.
    */
   public void closeWrite() {
      if (nextField != start.next) {
         log.fatal("Cannot close a Tuple without completing the current record");
      }
      if (writer instanceof StructureData) {
         ((StructureData) writer).closeWrite();
      }
      datafile.status = Datafile.Status.CLOSED;
      if (reader != null) {
         reader.setOffset(0);
         reader.setCeiling(Long.MAX_VALUE);
      }
   }

   /**
    * closes the TupleData stream.
    */
   public void closeRead() {
      if (datafile != null) {
         datafile.closeRead();
      } else {
         reader.closeRead();
      }
   }

   /**
    * @return true if EOF has not been reached. Warning, not all filing systems
    * support file lengths, and reading has to continue until an EOF exception
    * is thrown. Therefore, hasNext() may return a false positive after the last
    * record has been read. It is therefore recommended to use the return value
    * of the next() function to determine if EOF has been reached.
    */
   public boolean hasNext() {
      return reader.hasMore();
   }

   /**
    * reads the next record, storing the read values in the structure fields.
    * <p/>
    * @return true is a record was read, or false if EOF or ceiling was reached.
    */
   public boolean next() {
      if (reader.hasMore()) {
         //log.info("%d", reader.getOffset());
         try {
            reader.skipFirst(whitespace);
            StructuredStream.Field first = start.next();
            if (nextField == first) {
               nextField.readNoReturn();
            }
            while (nextField != first) {
               nextField.readNoReturn();
            }
            return true;
         } catch (EOFException ex) {
            //log.exception(ex, "%d", reader.getOffset());
         }
      }
      return false;
   }
}
