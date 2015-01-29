/*
 * Copyright 2014 jeroen.
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
package io.github.repir.tools.io.buffer;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeroen
 */
public class BufferReaderWriterTest {
    public static Log log = new Log(BufferReaderWriterTest.class);

    @Test
    public void testReadBytes() {
        Datafile df = new Datafile("/Users/jeroen/bufferreaderwritertest");
        df.openWrite();
        for (int i = 0; i < 10; i++)
            df.write(getSample(i));
        df.closeWrite();
        df.openRead();
        for (int i = 0; i < 10; i++) {
            byte[] readByteArray = df.readByteArray();
            //log.printf("");
            //log.memoryDump(readByteArray);
            assertEquals(true, checkSample(i, readByteArray));
        }
        df.closeRead();
        df.delete();
    }
    
    private byte[] getSample(int i) {
        byte b[] = new byte[20000 - i];
        for (int p = 0 ; p < 20000 - i; p++) {
            b[p] = (byte)(p & 0xff);
        }
        //log.memoryDump(b);
        return b;
    }
    
    private boolean checkSample(int i, byte b[]) {
        if (b.length != 20000 - i) {
            log.memoryDump(b);
            log.info("length %d %d", i, b.length);
            return false;
        }
        for (int p = 0 ; p < 20000 - i; p++) {
            if (b[p] != (byte)(p & 0xff)) {
                log.memoryDump(b);
                log.info("%d %d %d", p, b[p], (byte)(p & 0xff));
                return false;
                
            }
        }
        return true;
    }
}
