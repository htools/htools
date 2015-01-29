package io.github.repir.tools.hadoop;

import io.github.repir.tools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
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

    public void write(String label, int counter) {
        this.label.set(label);
        this.counter.set(counter);
        this.write();
    }
}
