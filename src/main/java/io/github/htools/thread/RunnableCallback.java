package io.github.htools.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class RunnableCallback implements Callable {
    protected Callback callback;
    protected ThreadedScheduler scheduler;
    protected Future<ThreadedScheduler> future;

    public RunnableCallback(ThreadedScheduler scheduler, Callback callback) {
        this.scheduler = scheduler;
        this.callback = callback;
    }

    public void cancel(boolean mayinterruptrunning) {
        future.cancel(mayinterruptrunning);
    }
    
    public void submit() {
        scheduler.submit(this);
    }
    
    public boolean isCancelled() {
        return future.isCancelled();
    }
    
    public boolean isDone() {
        return future.isDone();
    }
    
    public abstract void task();
    
    @Override
    public final Object call() {
        task();
        if (callback != null) {
            callback.finished(this);
        }
        //scheduler.waken();
        return this;
    }

}
