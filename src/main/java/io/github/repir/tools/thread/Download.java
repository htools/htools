package io.github.repir.tools.thread;

import io.github.repir.tools.io.web.WebTools;
import io.github.repir.tools.lib.Log;
import java.net.URL;
/**
 *
 * @author jeroen
 */
public abstract class Download extends RunnableCallback {
    public static Log log = new Log(Download.class);
    public URL url;
    public int timeout;
    public WebTools.UrlResult content;

    public Download(ThreadedScheduler scheduler, Callback callback, URL url, int timeout) {
        super(scheduler, callback);
        this.url = url;
        this.timeout = timeout;
    }

    public Download(ThreadedScheduler scheduler, URL url, int timeout) {
        this(scheduler, null, url, timeout);
    }

    public Download(ThreadedScheduler scheduler, URL url) {
        this(scheduler, url, 5000);
    }

    public WebTools.UrlResult getResult() {
        return content;
    }

    @Override
    public void task() {
        log.info("%s", url.toString());
        content = WebTools.getContent(url, timeout);
        log.info("finished %s", url.toString());
    }

}
