package io.github.htools.io.struct;

import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.Iterator;

public interface StructuredRecordFile<R extends StructuredFileRecord> extends Iterable<R> {
   public static final Log log = new Log( StructuredRecordFile.class );
   
   public abstract R newRecord();
   
   public abstract R readRecord() throws IOException;
   
   public abstract void openRead() throws IOException;
   
   public abstract void closeRead() throws IOException;
   
   public abstract void openWrite() throws IOException;
   
   public abstract Datafile getDatafile();
   
   public abstract void closeWrite() throws IOException;
   
   public abstract long getLength();
   
   public abstract void delete();
   
   public abstract void write(R record) throws IOException;
   
   public abstract boolean nextRecord() throws IOException ;
   
   public abstract long getOffset();
   
   public abstract void setOffset(long offset) throws IOException;
   
   public abstract long getCeiling();
   
   public abstract void setCeiling(long ceiling);
   
   public abstract void setBufferSize(int buffersize);
   
   public abstract boolean hasMore();
   
   public abstract boolean findFirstRecord();
   
   @Override
   public abstract Iterator<R> iterator();
}
