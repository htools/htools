package io.github.repir.tools.hadoop;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import java.io.IOException;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
/**
 *
 * @author jeroen
 */
public class LogFile extends LogFileAbstract {
   public static final Log log = new Log( LogFile.class );
   StringField message = this.addString("label");

    public LogFile(TaskAttemptContext context) {
        super(context);
    }
    
    public LogFile(Datafile outputfile) throws IOException {
        super(outputfile);
    }
    
    public void write(String message) {
        this.message.set(message);
        this.write();
    }
    
    public void write(String message, Object ... params) {
        this.message.set(PrintTools.sprintf(message, params));
        this.write();
    }
}
