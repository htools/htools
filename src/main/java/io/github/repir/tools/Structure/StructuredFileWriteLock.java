package io.github.repir.tools.Structure;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FileIntegrityException;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.conf.Configuration;

/**
 * Implementation for a file with relatively small headers, to be loaded into
 * memory that contain the location of a larger data record in a different file.
 *
 * @author jer
 */
public abstract class StructuredFileWriteLock extends StructuredFile {

   public Log log = new Log(StructuredFileWriteLock.class);
   public Configuration configuration;
   public StringField id = this.addString("id");
   public LongField datetime = this.addLong("datetime");
   public LongField expires = this.addLong("expires");
   public String jobid;
   public long mylock = 0;
   public long otherlock = 0;

   public StructuredFileWriteLock(Datafile basefile, Configuration conf) {
      super(basefile.getSubFile(".lock"));
      this.configuration = conf;
      jobid = conf.get("mapred.job.id");
   }

   public boolean obtainLock() throws FileIntegrityException {
      if (datafile.exists()) {
         super.openRead();
         if (isReadOpen()) {
            setOffset(0);
            if (nextRecord()) {
               otherlock = datetime.value;
            }
         }
         super.closeRead();
         return false;
      } else {
         super.openWrite();
         mylock = System.currentTimeMillis();
         datetime.write(mylock);
         super.closeWrite();
         return checkLockValidity();
      }
   }

   public boolean checkLockValidity() {
      boolean valid=false;
      if (mylock > 0) {
         setOffset(0);
         openRead();
         if (nextRecord() && datetime.value == mylock) {
            valid=true;
         }
         closeRead();
      }
      if (!valid)
         mylock = 0;
      return valid;
   }
   
}
