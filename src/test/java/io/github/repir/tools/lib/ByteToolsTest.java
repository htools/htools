package io.github.repir.tools.lib;

import io.github.repir.tools.lib.ByteTools;
import io.github.repir.tools.lib.Log;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jer
 */
public class ByteToolsTest {

   public static Log log = new Log(ByteToolsTest.class);

   public ByteToolsTest() {
   }

   @Test
   public void testToString_byte() {
   }

   @Test
   public void testZeroBytes() {
      String tekst = "aap";
      byte b[] = new byte[9];
      System.arraycopy(tekst.getBytes(), 0, b, 1, 3);
      System.arraycopy(tekst.getBytes(), 0, b, 5, 3);
      String tekst1 = new String(b);
      log.info("tekst %s", tekst1);
      assertEquals(9, tekst1.length()); // standard byte array -> string keeps \0 bytes
   }

   @Test
   public void testToString() {
      byte s[] = "  aap".getBytes();
      byte b[] = new byte[2 * s.length + 3];
      System.arraycopy(s, 0, b, 1, s.length);
      System.arraycopy(s, 0, b, s.length + 2, s.length);
      String tekst1 = ByteTools.toString(b, 0, b.length);
      assertEquals("  aap  aap", tekst1);
   }

   @Test
   public void testToTrimmedString() {
      byte s[] = "  aap".getBytes();
      byte b[] = new byte[2 * s.length + 3];
      System.arraycopy(s, 0, b, 1, s.length);
      System.arraycopy(s, 0, b, s.length + 2, s.length);
      String tekst1 = ByteTools.toTrimmedString(b, 0, b.length);
      assertEquals("aap  aap", tekst1);
   }

   @Test
   public void testToFullTrimmedString() {
      byte s[] = "  aap".getBytes();
      byte b[] = new byte[2 * s.length + 3];
      System.arraycopy(s, 0, b, 1, s.length);
      System.arraycopy(s, 0, b, s.length + 2, s.length);
      String tekst1 = ByteTools.toFullTrimmedString(b, 0, b.length);
      assertEquals("aap aap", tekst1);
   }
}
