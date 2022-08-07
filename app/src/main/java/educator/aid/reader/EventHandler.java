package educator.aid.reader;

import android.os.Handler;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventHandler {

    private static final String TAG = "Event Handler";
    private ExecutorService executorService;
    private Handler handler;

    private class Request implements Runnable
    {
        private int times;
        private long delay;
        private Runnable r;

        private int i;

        public Request(int times, long delay, Runnable r)
        {
            this.times = times;
            this.delay = delay;
            this.r = r;
            this.i = 0;
        }

        @Override
        public void run() {
            r.run();
            i++;
            if (i < times)
            {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        executorService.execute(Request.this);
                    }
                }, delay);
            }
        }
    }

    public EventHandler()
    {
        handler = new Handler();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void handleTask(Runnable task, int times, long delay)
    {
        Log.d(TAG, "Current thread");

        Request r = new Request(times, delay, task);
        executorService.execute(r);

    }

    public void removeAllRequests()
    {
        handler.removeCallbacks(null);
        executorService.shutdown();
    }
}
