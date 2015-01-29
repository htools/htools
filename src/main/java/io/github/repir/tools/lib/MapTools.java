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

import java.util.Collection;
import java.util.Map;
/**
 *
 * @author jeroen
 */
public enum MapTools {
    ;
   public static final Log log = new Log( MapTools.class );

    public static <K,V> String toString(Collection<Map.Entry<K,V>> i) {
        StringBuilder sb = null;
        for (Map.Entry<K,V> e : i) {
            if (sb == null)
                sb = new StringBuilder().append('{');
            else
                sb.append(',').append(' ');
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key   == i ? "(this)" : key);
            sb.append('=');
            sb.append(value == i ? "(this)" : value);     
        }
        return sb == null?"{}":sb.append('}').toString();
    }   
}
