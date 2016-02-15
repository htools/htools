package io.github.htools.hadoop;

import io.github.htools.lib.Log;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
/**
 *
 * @author jeroen
 */
public class LogCounterFile extends LogFileAbstract {
   public static final Log log = new Log( LogCounterFile.class );
   StringField label = this.addString("label");
   IntField counter = this.addInt("counter");

    public LogCounterFile(TaskAttemptContext context) throws IOException {
        super(context);
    }

    public void write(String label, int counter) throws IOException {
        this.label.set(label);
        this.counter.set(counter);
        this.write();
    }
}
