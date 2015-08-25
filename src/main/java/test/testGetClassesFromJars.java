package test;

import io.github.htools.io.DataIn;
import io.github.htools.extract.modules.ExtractorProcessor;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class testGetClassesFromJars {
   public static final Log log = new Log( testGetClassesFromJars.class );

    public static void main(String[] args) throws IOException, ClassNotFoundException {
       ArrayList<String> classesFromJars = ClassTools.getClassesFromJars(DataIn.class);
       for (String c : classesFromJars) {
           log.printf(c);
       }
    }
}
