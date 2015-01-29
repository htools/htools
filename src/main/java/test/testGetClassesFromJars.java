package test;

import io.github.repir.tools.io.DataIn;
import io.github.repir.tools.extract.modules.ExtractorProcessor;
import io.github.repir.tools.lib.ClassTools;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class testGetClassesFromJars {
   public static final Log log = new Log( testGetClassesFromJars.class );

    public static void main(String[] args) throws IOException, ClassNotFoundException {
       ArrayList<Class> classesFromJars = ClassTools.getClassesFromJars(DataIn.class);
       for (Class c : classesFromJars) {
           log.info(c.getCanonicalName());
       }
    }
}
