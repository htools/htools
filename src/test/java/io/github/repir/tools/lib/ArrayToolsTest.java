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

import io.github.repir.tools.lib.ArrayTools;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeroen
 */
public class ArrayToolsTest {
    
    public ArrayToolsTest() {
    }

    @Test
    public void testValues() {
    }

    @Test
    public void testValueOf() {
    }

    @Test
    public void testGroup() {
    }

    @Test
    public void testAddArr() {
    }

    @Test
    public void testDelete() {
        String a[] = {"1", "2", "3", "4", "5"};
        String result[] = {"1", "4", "5"};
        String r[] = ArrayTools.delete(a, 1, 3);
        Assert.assertArrayEquals("not equal ", r, result);
    }

    @Test
    public void testResize() {
    }

    @Test
    public void testAddObjectToArr() {
    }

    @Test
    public void testCopy() {
    }

    @Test
    public void testUnion_GenericType() {
    }

    @Test
    public void testUnion_GenericType_GenericType() {
    }

    @Test
    public void testCreateArray_GenericType_int() {
    }

    @Test
    public void testCreateArray_Class_int() {
    }

    @Test
    public void testIndexOf() {
    }

    @Test
    public void testArrayOfOthers() {
    }

    @Test
    public void testClone_intArr() {
    }

    @Test
    public void testClone_booleanArr() {
    }

    @Test
    public void testClone_StringArr() {
    }

    @Test
    public void testFlatten_longArrArr() {
    }

    @Test
    public void testFlatten_intArrArr() {
    }

    @Test
    public void testFlatten_intArrArrArr() {
    }

    @Test
    public void testToIntArray() {
    }

    @Test
    public void testToLongArray() {
    }

    @Test
    public void testToStringArray() {
    }

    @Test
    public void testToDoubleArray() {
    }

    @Test
    public void testIntersection() {
    }

    @Test
    public void testUnique() {
    }

    @Test
    public void testToString_ObjectArr() {
    }

    @Test
    public void testToString_byteArr() {
    }

    @Test
    public void testToString_intArr() {
    }

    @Test
    public void testToString_intArrArr() {
    }

    @Test
    public void testToString_intArrArrArr() {
    }

    @Test
    public void testToString_longArr() {
    }

    @Test
    public void testToString_doubleArr() {
    }

    @Test
    public void testToString_booleanArr() {
    }

    @Test
    public void testToString_4args_1() {
    }

    @Test
    public void testToString_4args_2() {
    }

    @Test
    public void testToString_Collection() {
    }

    @Test
    public void testToString_Collection_String() {
    }

    @Test
    public void testToString_4args_3() {
    }

    @Test
    public void testToString_4args_4() {
    }

    @Test
    public void testToString_4args_5() {
    }

    @Test
    public void testToString_4args_6() {
    }

    @Test
    public void testEquals_doubleArr_doubleArr() {
    }

    @Test
    public void testEquals_intArr_intArr() {
    }

    @Test
    public void testSubArray_doubleArr_int() {
    }

    @Test
    public void testSubArray_3args_1() {
    }

    @Test
    public void testSubArray_StringArr_int() {
    }

    @Test
    public void testSubArray_ObjectArr_int() {
    }

    @Test
    public void testSubArray_3args_2() {
    }

    @Test
    public void testSubArray_3args_3() {
    }

    @Test
    public void testSubArray_intArr_int() {
    }

    @Test
    public void testSlice_intArrArr_int() {
    }

    @Test
    public void testSlice_doubleArrArr_int() {
    }

    @Test
    public void testSwap() {
    }

    @Test
    public void testContains_GenericType_GenericType() {
    }

    @Test
    public void testContains_int_intArr() {
    }

    @Test
    public void testFill_doubleArr_double() {
    }

    @Test
    public void testFill_intArr_int() {
    }
    
}
