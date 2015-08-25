package io.github.htools.io.struct;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.BytesOut;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.io.FileIntegrityException;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * The StructuredFile class provides a toolkit to store data in a datafile that
 * extends {@link StructuredDataStream} to implement a table of
 * rows/records/tuples with a fixed order of columns/fields. Accessing the data
 * through the {@link StructuredDataStream} protects data integrity by forcing
 * to read and write records containing all fields in a fixed order as defined
 * in the {@link StructuredDataStream}'s structure. The default implementation
 * uses a {@link Datafile} for storage, but alternatively
 * {@link BufferReaderWriter} can be used to read and write tuples to a memory
 * buffer. When reading tuples, the end is reached on EOF, or when a preset
 * ceiling is encountered.
 * <p>
 * @author jeroen
 */
public abstract class StructuredFile extends StructuredDataStream {

   public Log log = new Log(StructuredFile.class);
   private Datafile datafile;
   public long recordoffset;
   int bucketcapacity = 0;
   double loadfactor = 0.75;             // used for calculation of bucket indexes
   int tupleswritten;
   private long tupleend = 0;
   private long tuplestart = 0;

   /**
    * constructor to read/write tuples to a {@link Datafile}
    * <p>
    * @param datafile
    */
   public StructuredFile(Datafile datafile) {
      super(datafile.rwbuffer);
      setDatafile(datafile);
   }

   /**
    * constructor to read tuples from a byte array, using
    * {@link BufferReaderWriter}
    * <p>
    * @param bytes a byte[] that contains the tuple table to be read.
    */
   public StructuredFile(byte[] bytes) {
      super(new BufferReaderWriter(bytes));
   }

   /**
    * constructor to write tuples to a byte array, using
    * {@link BufferReaderWriter} and {@link BytesOut}
    * <p>
    * @param bytes the {@link BytesOut} object that will contain the byte output
    * of tuples written.
    */
   public StructuredFile(BytesOut bytes) {
      super(new BufferReaderWriter(bytes));
   }

   protected void setDatafile(Datafile df) {
      this.datafile = df;
      if (df != null) {
         this.writer = df.rwbuffer;
         if (df.exists()) {
            this.reader = df.rwbuffer;
         }
      } else {
         this.writer = null;
         this.reader = null;
      }
   }

    public void write() {
       if (writer != null) {
         try {
            Field first = start.nextField();
            if (nextField == first) {
               this.recordoffset = getOffset();
               //log.info("read field %s offset %d", nextField.getLabel(), this.getOffset());
               nextField.write();

            }
            while (nextField != first) {
               //log.info("read field %s offset %d", nextField.getLabel(), this.getOffset());
               nextField.write();
            }
         } catch (EOCException ex) {
             //log.info("Ex");
         }
      }
      this.resetNextField();
   }   
   
   /**
    * @return {@link Datafile} that the StructuredFile class read/writes to.
    */
   public Datafile getDatafile() {
      return this.datafile;
   }

   public boolean isClosed() {
      return datafile.isClosed();
   }

   public boolean isReadOpen() {
      return datafile.isReadOpen();
   }

   public boolean exists() {
      return datafile.exists();
   }
   
    public void delete() {
        datafile.delete();
    }
    
    public boolean hasMore() {
        return datafile.hasMore();
    }    

   public boolean isWriteOpen() {
      return datafile.isWriteOpen();
   }

   public long getLength() {
      return datafile.getLength();
   }

   /**
    * moves the offset of the file/memory buffer being read. Only useful if you
    * know the exact offset at which a StructuredFile starts.
    * <p>
    * @param offset
    */
   public void setOffset(long offset) {
      reader.setOffset(offset);
   }

   /**
    * Setting a ceiling ensures that no data is read beyond that point, as if
    * the tuple table ends there.
    * <p>
    * @param ceiling
    */
   public void setCeiling(long ceiling) {
      reader.setCeiling(ceiling);
   }

   /**
    * The size of the memory buffer used. For large sequential tables, a larger
    * buffer size reduces the amount of read actions, increasing performance. No
    * checking is done with respect to available memory.
    * <p>
    * @param size
    */
   public void setBufferSize(int size) {
      //log.info("setBufferSize() size %d reader %s writer %s", size, reader, writer);
      if (writer != null) {
         writer.setBufferSize(size);
      } else if (reader != null) {
         reader.setBufferSize(size);
      }
   }

   public int getBufferSize() {
      //log.info("setBufferSize() size %d reader %s writer %s", size, reader, writer);
      if (datafile != null) {
         return datafile.getBufferSize();
      }
      return 0;
   }

   public void fillBuffer() throws EOCException {
      if (reader != null) {
         reader.fillBuffer();
      }
   }

   /**
    * For the support of parallel processing into sorted tuple array's, multiple
    * reducers process the data into temporary file segments, so that all tuples
    * written by reducer0 are smaller than reducer1's. Upon job completion, the
    * temporary tuple segments are concatenated on job completion to create a
    * single sorted tuple file. This function provides a temporary file to a
    * reducer based on their partition number.
    * <p>
    * @param partition
    * @return
    */
   public Datafile getSegment(int partition) {
      return datafile.getSubFile(io.github.htools.lib.PrintTools.sprintf(".%04d", partition));
   }

   /**
    * For the support of parallel processing into sorted tuple array's, multiple
    * reducers process the data into temporary file segments, so that all tuples
    * written by reducer0 are smaller than reducer1's. Upon job completion, this
    * function concatenates the temporary tuple segments into a single sorted
    * tuple file.
    * <p>
    * @return array of offsets of the original temporary segments.
    */
   public long[] mergeSegments() throws IOException {
      HDFSPath dir = (HDFSPath) datafile.getDir();
      //log.info("%s %s %s", datafile.getFullPath(), datafile.getDir().getCanonicalPath(), datafile.getFilename());
      long offsets[] = dir.mergeFiles(new Datafile(datafile.getFileSystem(), datafile.getCanonicalPath()), datafile.getName());
      return offsets;
   }

   public Datafile getTempfile() {
      //log.info("getTempfile( %s )", datafile);
      return datafile.getSubFile(".temp");
   }

   /**
    * clones the StructuredFile structure from another StructuredFile instance
    * <p>
    * @param source tuple whose structure is being cloned
    */
   public void copyStructure(StructuredFile source) {
      this.start = this.addStart();
      for (StructuredStream.Field e = start.next; e != null; e = e.next) {
         e.clone(this);
      }
   }

   /**
    * clones this StructuredFile object, by simply instantiating a new
    * StructuredFile object using the same Datafile
    * <p>
    * @return a clone of this StructuredFile object
    */
   @Override
   public StructuredFile clone() {
      StructuredFile tuple = null;
      try {
         Constructor<? extends StructuredFile> declaredConstructor = this.getClass().getDeclaredConstructor(Datafile.class);
         tuple = declaredConstructor.newInstance(new Datafile(this.getDatafile()));
      } catch (Exception ex) {
         log.exception(ex, "clone()");
      }
      return tuple;
   }

   /**
    * current contains the next field in the structure, if this is null, it
    * means a whole row was read/written. In StructuredFile the structure
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
    * Record the current offset. This is mainly used to compute the actual size
    * of tuples, as they are read or written.
    */
   public void setTupleOffset() {
      tuplestart = tupleend;
      tupleend = getOffset();
   }

   /**
    * @return the last recorded offset
    */
   public long getOffsetTupleStart() {
      return tuplestart;
   }

   public void setOffsetTupleStart(long offset) {
      tuplestart = offset;
   }

   /**
    * @return the last recorded offset
    */
   public long getOffetTupleEnd() {
      return tupleend;
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
    * for internal use only
    */
   @Override
   public void writeDone(Field f) {
      if (f == start.next) {
         this.tuplestart = f.lastoffset;
      }
      if (f == this.last) {
         this.tupleend = getOffset();
         hookRecordWritten();
         tupleswritten++;
      }
   }

   /**
    * hook to view back on the record that is last written. This is manly used
    * for sorted implementations of StructuredFile, allowing them to cache and
    * sort records before being written.
    * <p>
    */
   public void hookRecordWritten() {
   }

   /**
    * open a StructuredFile in writing mode.
    */
   public void openWrite() {
      if (datafile != null) {
         datafile.openWrite();
         this.writer = datafile.rwbuffer;
         this.reader = null;
      }
      tupleswritten = 0;
   }

   public void openAppend() {
      if (datafile != null) {
         datafile.openAppend();
         this.writer = datafile.rwbuffer;
         this.reader = null;
      }
      tupleswritten = 0;
   }

   /**
    * open a StructuredFile in reading mode
    */
   public void openRead() throws FileIntegrityException {
      if (datafile != null && datafile.isClosed() && datafile.exists()) {
         datafile.openRead();
         this.reader = datafile.rwbuffer;
         this.writer = null;
      } else {
      }
   }
   
   public void reuseBuffer() {
       if (datafile != null)
           datafile.reuseBuffer();
   }

   public void resetStart() {
       if (datafile != null)
           datafile.resetStart();
   }

   /**
    * closes the StructuredFile stream.
    */
   public void closeWrite() {
      if (nextField != start.next) {
         log.fatal("Cannot close a Tuple without completing the current record");
      }
      if (writer instanceof StructureData) {
         ((StructureData) writer).closeWrite();
      }
      datafile.status = Datafile.STATUS.CLOSED;
      if (reader != null) {
         reader.setOffset(0);
         reader.setCeiling(Long.MAX_VALUE);
      }
   }

   /**
    * closes the StructuredFile stream.
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
    * <p>
    * @return true is a record was read, or false if EOF or ceiling was reached.
    */
   public boolean nextRecord() {
      //log.info("next() %d", this.getOffset());
      if (reader != null && reader.hasMore()) {
         try {
            Field first = start.nextField();
            if (nextField == first) {
               this.recordoffset = getOffset();
               //log.info("read field %s offset %d", nextField.getLabel(), this.getOffset());
               nextField.readNoReturn();

            }
            while (nextField != first) {
               //log.info("read field %s offset %d", nextField.getLabel(), this.getOffset());
               nextField.readNoReturn();
            }
            return true;
         } catch (EOCException ex) {
             //log.info("Ex");
         }
      }
      this.resetNextField();
      return false;
   }

   /**
    * reads the next record, storing the read values in the structure fields.
    * <p>
    * @return true is a record was read, or false if EOF or ceiling was reached.
    */
   public boolean skipRecord() {
      //log.info("next() %s", this.toString());
      if (reader.hasMore()) {
         try {
            Field first = start.nextField();
            if (nextField == first) {
               nextField.skip();
            }
            while (nextField != first) {
               nextField.skip();
            }
            return true;
         } catch (Exception ex) {
         }
      }
      return false;
   }

   /**
    * @param loadfactor determines the capacity needed for a table that contains
    * n tuples to avoid too many collisions in hash tables.
    */
   public void setLoadFactor(double loadfactor) {
      this.loadfactor = loadfactor;
   }

   /**
    * calculates a table size as the power of two that is greater or equal to
    * the number of required buckets (tuples / loadfactor)
    * <p>
    * @param tuples
    */
   protected void setCapacity(int tuples) {
      bucketcapacity = computeBucketCapacity(tuples);
   }

   public int computeBucketCapacity(int tuples) {
      int target = (int) (tuples / loadfactor);
      int bucketcapacity = 2;
      while (bucketcapacity < target) {
         bucketcapacity <<= 1;
      }
      return bucketcapacity;
   }

   public int getBucketCapacity() {
      return bucketcapacity;
   }
}
