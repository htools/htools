/*
 * Copyright 2015 jeroen.
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
package io.github.htools.hadoop.io.buffered;

import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.WritableComparator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 *
 * @author jeroen
 */
public class Comparator extends WritableComparator {

    public static final Log log = new Log(Comparator.class);
    private ArrayList<ComparatorSub> subcomparators = new ArrayList();
    protected byte[] byte1;
    protected byte[] byte2;
    protected int start1;
    protected int start2;
    protected int end1;
    protected int end2;
    
    public Comparator(Class<? extends ComparatorSub> ... subclasses) {
        for (Class c : subclasses) {
            Constructor<ComparatorSub> constructor = ClassTools.tryGetAssignableConstructor(c, ComparatorSub.class);
            ComparatorSub sub = ClassTools.construct(constructor);
            subcomparators.add(sub);
        }
    }

    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        this.byte1 = b1;
        this.byte2 = b2;
        this.start1 = s1 + 4;
        this.start2 = s2 + 4;
        this.end1 = s1 + l1;
        this.end2 = s2 + l2;
        int comp = 0;
        for (ComparatorSub s : subcomparators) {
            comp = s.compare(this);
            if (comp != 0)
                return comp;
        }
        return comp;
    }
}
