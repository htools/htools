package io.github.htools.io.struct;

import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import java.util.Iterator;

public interface StructuredRecordFile<R extends StructuredFileRecord> extends Iterable<R> {
   public static final Log log = new Log( StructuredRecordFile.class );
   
   public abstract R newRecord();
   
   public abstract R readRecord();
   
   public abstract void openRead();
   
   public abstract void closeRead();
   
   public abstract void openWrite();
   
   public abstract Datafile getDatafile();
   
   public abstract void closeWrite();
   
   public abstract long getLength();
   
   public abstract void delete();
   
   public abstract void write(R record);
   
   public abstract boolean nextRecord();
   
   public abstract long getOffset();
   
   public abstract void setOffset(long offset);
   
   public abstract long getCeiling();
   
   public abstract void setCeiling(long ceiling);
   
   public abstract void setBufferSize(int buffersize);
   
   public abstract boolean hasMore();
   
   public abstract boolean findFirstRecord();
   
   @Override
   public abstract Iterator<R> iterator();
}
