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
package io.github.htools.lib;

import io.github.htools.lib.RandomTools;
import io.github.htools.lib.CollectionTools;
import io.github.htools.lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeroen
 */
public class CollectionToolsTest {
    public static Log log = new Log(CollectionToolsTest.class);

    @Test
    public void testCluster() {
        HashSet<Integer> check = new HashSet();
        ArrayList<HashSet<Integer>> c = new ArrayList();
        for (int i = 0; i < 100; i++) {
            HashSet<Integer> h = new HashSet();
            for (int j = 0; j < 100; j++) {
                int nr = RandomTools.getInt(10000000);
                h.add(nr);
                check.add(nr);
            }
            c.add(h);
        }
        CollectionTools.cluster(c);
        log.info("size %d", c.size());
        for (Integer i : check) {
            int count = 0;
            for (HashSet<Integer> d : c) {
                if (d.contains(i))
                    count++;
                if (count > 1) {
                    log.info("%d", i);
                    for (HashSet<Integer> e : c)
                        log.info("%s", e);
                }
            }
            assertEquals(1, count);
        }
    }
    
    @Test
    public void test1() {
        HashSet<Integer> check = new HashSet();
        for (int i = 0; i < 2; i++) {
            HashSet<Integer> h = new HashSet();
            for (int j = 0; j < 100; j++) {
                int nr = RandomTools.getInt(100);
                h.add(nr);
            }
            check.addAll(h);
        }
        log.info("size %s", check);
    }
    
}
