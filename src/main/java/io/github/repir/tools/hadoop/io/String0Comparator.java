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
package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.lib.Log;
import org.apache.hadoop.io.WritableComparator;
/**
 *
 * @author jeroen
 */
public class String0Comparator extends WritableComparator {
   public static final Log log = new Log( String0Comparator.class );
     
    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        int end1 = s1 + l1;
        int end2 = s2 + l2;
        for ( s1 += 4, s2 += 4; s1 < end1 && s2 < end2; s1++, s2++)
           if (b1[s1] != b2[s2] || b1[s1] == 0)
               return b1[s1] - b2[s2];
        return l1 - l2;
    }
}
