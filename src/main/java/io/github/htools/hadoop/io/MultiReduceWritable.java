package io.github.htools.hadoop.io;

import io.github.htools.hadoop.io.buffered.DelayedWritable;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
/**
 *
 * @author jeroen
 */
public class MultiReduceWritable implements Writable {
   public static final Log log = new Log( MultiReduceWritable.class );
   public static String CLASSCLASS = "multireducewritable.class";
   protected static Class[] classes;
   private static Constructor[] constructors;
   protected DelayedWritable record;
   protected int type;
   protected BufferDelayedWriter writer;
   protected BufferReaderWriter reader;
   
   public void set(DelayedWritable w) {
       record = w;
       type = getType(w);
   }

   public void get(DelayedWritable w) {
       w.readFields(reader);
   }
   
   public static void setClasses(Job job, Class ... clazz) {
       classes = clazz;
       StringBuilder sb = new StringBuilder();
       for (Class c : classes) {
           if (!(DelayedWritable.class.isAssignableFrom(c)))
               log.fatal("Classes used by MultiReduceWritable must implement MultiWritable %s", c.getCanonicalName());
           sb.append(",").append(c.getCanonicalName());
       }
       job.getConfiguration().set(CLASSCLASS, sb.deleteCharAt(0).toString());
   }
   
    @Override
    public void write(DataOutput out) throws IOException {
       if (writer == null)
           writer = new BufferDelayedWriter();
       writer.writeC(type);
       record.write(writer);
       writer.writeBuffer(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        reader = new BufferReaderWriter(in);
        type = reader.readCInt();
    }
    
    public static void setConfiguration(Configuration conf) {
        classes = conf.getClasses(CLASSCLASS);
        constructors = new Constructor[classes.length];
        for (int i = 0; i < classes.length; i++) {
            try {
                Constructor constructor = ClassTools.getAssignableConstructor(classes[i], DelayedWritable.class);
                constructors[i] = constructor;
            } catch (ClassNotFoundException ex) {
                log.exception(ex, "setConfiguration() class %s must extend MultiWritable", classes[i]);
            }
        }
    }

    public static int getType(DelayedWritable w) {
        if (classes == null)
            log.fatal("getType cannot be used before setConfiguration() is used to set the configuration");
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].isInstance(w))
                return i;
        }
        log.info("Unknown class %s", w.getClass());
        for (Class c : classes)
            log.info("Known class %s", c.getCanonicalName());
        log.fatal("");
        return -1;
    }    
    
    public static MultiWritableIterator getIterator(Reducer.Context context, Iterable<? extends MultiReduceWritable> iterator) throws ClassNotFoundException {
        if (classes == null)
            setConfiguration(context.getConfiguration());
        return new MultiWritableIterator(iterator);
    }    
    
    public static class MultiWritableIterator implements Iterator<DelayedWritable>, Iterable<DelayedWritable> {
        Iterator<? extends MultiReduceWritable> iterator;
        
        public MultiWritableIterator(Iterable<? extends MultiReduceWritable> iterator) {
            this.iterator = iterator.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public DelayedWritable next() {
            MultiReduceWritable record = iterator.next();
            DelayedWritable result = (DelayedWritable)ClassTools.construct(constructors[record.type]);
            record.get(result);
            return result;
        }

        @Override
        public void remove() {
            iterator.remove();
        }

        @Override
        public Iterator<DelayedWritable> iterator() {
            return this;
        }
    }
}
