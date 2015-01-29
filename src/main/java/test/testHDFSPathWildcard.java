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
package test;

import io.github.repir.tools.io.DirComponent;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.Log;
import org.apache.hadoop.conf.Configuration;
/**
 *
 * @author jeroen
 */
public class testHDFSPathWildcard {
   public static final Log log = new Log( testHDFSPathWildcard.class );

    public static void main(String[] args) {
        HDFSPath p = new HDFSPath(new Configuration(), args[0]);
        for (DirComponent d : p.wildcardIterator()) { 
             log.info("%s", d.getCanonicalPath());
        }
    }
}
