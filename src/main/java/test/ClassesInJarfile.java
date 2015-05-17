package test;

import io.github.repir.tools.lib.ClassTools;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class ClassesInJarfile {
   public static final Log log = new Log( ClassesInJarfile.class );

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        ArrayList<String> classes = ClassTools.getClassesFromJars(args[args.length-1]);
        for(String c : classes) {
            log.printf("%s", c);
        }
    }
   
}
