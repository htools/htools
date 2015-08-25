package io.github.htools.thread;

/**
 * Used to supply a method that will call back the caller after a task has finished
 * @author jeroen
 */
public interface Callback<O> {

    public void finished(O o);
    
}
