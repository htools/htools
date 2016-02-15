package test;

import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;

import java.io.IOException;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class FilesInJar {
   public static final Log log = new Log( FilesInJar.class );

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        ArrayList<String> classes = ClassTools.getFilesFromJars(Class.forName(args[args.length-1]));
        for(String c : classes) {
            log.printf("%s", c);
        }
    }
   
}
