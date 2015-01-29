package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.RandomTools;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * Pairs an int that indicates the partition number with a of long used for secondary sorting.
 *
 * @author jeroen
 */
public class RandomWritable extends IntWritable {

    public static Log log = new Log(RandomWritable.class);
    private Context context;

    public RandomWritable() {
        super();
    }
    
    public RandomWritable(Context context) {
        super();
        this.context = context;
        generateKey();
    }
    
    public void generateKey() {
        int numReduceTasks = context.getNumReduceTasks();
        set( RandomTools.getInt(numReduceTasks) );
    }
}
