/*
 * Copyright 2013 jeroen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.repir.tools.Lib;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.ArgsParser;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeroen
 */
public class ArgsParserTest {
   ArgsParser parsedargs;
   
   public ArgsParserTest() {
   }

  @Test
   public void testFatal() {
      ArgsParser.log.setLevel(Log.NONE);
      System.out.println("dont worry, checking fatal exception, should return: run with parameter: param");
      parsedargs = new ArgsParser( new String[0], "param");
      Assert.assertEquals(parsedargs.parsedargs.size(), 0);
      parsedargs = new ArgsParser( new String[0], "[param]");
      Assert.assertNull(parsedargs.getRepeatedGroup());
   }

   @Test
   public void testNonFatal() {
      System.out.println("checking non fatal exceptions");      
      parsedargs = new ArgsParser( new String[0], "[param]" );
      Assert.assertFalse(parsedargs.exists("param"));
      Assert.assertTrue(!parsedargs.exists("other"));
      parsedargs = new ArgsParser( new String[0], "{param}" );
      Assert.assertNotNull(parsedargs.getRepeatedGroup());
      Assert.assertTrue(!parsedargs.exists("param"));
      parsedargs = new ArgsParser( new String[]{"aap"}, "param [opt]" );
      Assert.assertTrue(parsedargs.exists("param"));
      Assert.assertFalse(parsedargs.exists("opt"));
      parsedargs = new ArgsParser( new String[]{ "aap", "noot" }, "param [opt]" );
      Assert.assertEquals(parsedargs.get("param"), "aap");
      Assert.assertEquals(parsedargs.get("opt"), "noot");
      parsedargs = new ArgsParser( new String[]{ "aap", "1", "2.5" }, "param int double [opt]" );
      Assert.assertEquals(parsedargs.getInt("int"), 1);
      Assert.assertEquals(parsedargs.getDouble("double"), 2.5, 0.001);
   }
}
