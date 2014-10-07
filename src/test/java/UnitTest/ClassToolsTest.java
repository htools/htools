package UnitTest;

import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 *
 * @author jeroen
 */
public class ClassToolsTest {
    Log log = new Log(ClassToolsTest.class);
    
    public ClassToolsTest() {
    }

    class A extends HashMap<Integer, String> {}
    
    public static void main(String[] args) {
        Class[] findTypeParameters = ClassTools.findTypeParameters(A.class, HashMap.class);
        ClassTools.log.info("%s", ArrayTools.concat(findTypeParameters));
    }
    
}
