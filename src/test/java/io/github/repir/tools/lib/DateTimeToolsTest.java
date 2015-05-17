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
package io.github.repir.tools.lib;

import io.github.repir.tools.lib.DateTools;
import java.text.ParseException;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeroen
 */
public class DateTimeToolsTest {
    
    @Test
    public void testToY_M_D() throws ParseException {
        String time = "2013-02-13T23:54:58.000000Z";
        Date parse = DateTools.FORMAT.DATETIMETSZ6.toDate(time);
        assertEquals(1360797066, parse.getTime());
    }
    
}
