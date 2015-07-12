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
package io.github.repir.tools.thread;

import io.github.repir.tools.lib.Log;
import java.lang.Thread.State;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jeroen
 */
public class ThreadedScheduler extends ThreadPoolExecutor {

    public static final Log log = new Log(ThreadedScheduler.class);
    Thread currentthread;
    int size = 0;

    public ThreadedScheduler(Collection<? extends Runnable> downloads) {
        this(downloads.size());
        currentthread = Thread.currentThread();
        for (Runnable d : downloads) {
            execute(d);
        }
    }

    public ThreadedScheduler(int size) {
        super(8, 96, 60, TimeUnit.SECONDS, new BlockingQueue<Runnable>(size, 10000));
    }

    public ThreadedScheduler(int size, int concurrent) {
        super(8, concurrent, 60, TimeUnit.SECONDS, new BlockingQueue<Runnable>(size, 10000));
    }

    @Override
    public Future<ThreadedScheduler> submit(Callable r) {
        size++;
        return super.submit(r);
    }

    public Future<ThreadedScheduler> submit(RunnableCallback r) {
        r.future = this.submit((Callable) r);
        return r.future;
    }

    public void waken() {
        if (currentthread.getState() == State.TIMED_WAITING) {
            currentthread.interrupt();
        }
    }

    public boolean waitSecondsToFinish(int seconds) {
        this.shutdown();
        try {
            return this.awaitTermination(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            log.exception(ex, "waitSecondsToFinish(%d) tasks %d completed %d", seconds, this.getTaskCount(), this.getCompletedTaskCount());
        }
        return false;
    }

    public boolean waitSecondsAfterLastFinish(int seconds) {
        this.shutdown();
        long oldtaskcompleted = 0;
        long oldtaskcompletedtime = System.currentTimeMillis();
        while (getCompletedTaskCount() < size && !isTerminated()) {
            if (getCompletedTaskCount() > oldtaskcompleted) {
                oldtaskcompletedtime = System.currentTimeMillis();
                oldtaskcompleted = getCompletedTaskCount();
            }
            if (System.currentTimeMillis() - oldtaskcompletedtime > seconds * 1000) {
                this.shutdownNow();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        return false;
    }

    public boolean waitUntil(long maxtime) {
        this.shutdown();
        while (System.currentTimeMillis() < maxtime && getCompletedTaskCount() < size && !isTerminated()) {
            log.info("%d %d", System.currentTimeMillis(), maxtime);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        if (getCompletedTaskCount() < size && !isTerminated()) {
            log.info("shutdownnow %d", System.currentTimeMillis());
            this.shutdownNow();
            log.info("shutdownnow %d", System.currentTimeMillis());
        }
        return true;
    }

}
