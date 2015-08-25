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
package io.github.htools.thread;

import io.github.htools.lib.Log;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jeroen
 */
public class BlockingQueue<E> extends ArrayBlockingQueue<E> {

    public static final Log log = new Log(BlockingQueue.class);
    private final long timeoutMS;

    public BlockingQueue(int capacity, long timeoutMS) {
        super(capacity);
        this.timeoutMS = timeoutMS;
    }

    @Override
    public boolean offer(E e) {
        try {
            return super.offer(e, timeoutMS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
