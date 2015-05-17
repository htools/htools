package test;

import io.github.repir.tools.lib.ClassTools;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class ClassesInJar {
   public static final Log log = new Log( ClassesInJar.class );

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        ArrayList<String> classes = ClassTools.getClassesFromJars(Class.forName(args[args.length-1]));
        for(String c : classes) {
            log.printf("%s", c);
        }
    }
   
}
