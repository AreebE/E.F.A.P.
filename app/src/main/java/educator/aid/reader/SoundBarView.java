package educator.aid.reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;

public class SoundBarView extends View {

    private static final String TAG = "SoundBarView";
    private boolean isPlaying;

    public interface Listener
    {
        void positionIsSet(int newTime);
    }

    private static final double PLAY_HEIGHT_RATIO = 1.0 / 4;
    private static final double SLIDER_RATIO = 1.0 / 2;
    private Listener listener;
    private int totalLength;
    private int currentTime;


    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public SoundBarView(Context context) {
        super(context);
        this.listener = new Listener() {
            @Override
            public void positionIsSet(int newTime) {

            }

        };
        this.totalLength = 7;
        this.currentTime = 0;
    }

    public SoundBarView(Context context, AttributeSet set)
    {
        super(context, set);
        this.listener = new Listener() {
            @Override
            public void positionIsSet(int newTime) {

            }

        };
        this.totalLength = 7;
        this.currentTime = 7;
    }

    public void setCurrentLength(long length)
    {
        this.totalLength = (int) (length / 1000);
        currentTime = 0;
        this.invalidate();
        this.postInvalidate();
    }

    public void setCurrentPlayTime(long millis)
    {
        currentTime = (int) (millis / 1000);
        Log.d(TAG, "calling the set --" + currentTime);
        this.invalidate();
        this.postInvalidate();
    }

    public int getCurrentPlayTime()
    {
        return currentTime;
    }

    public int getTotalLength()
    {
        return totalLength;
    }
    /**
     * Manually render this view (and all of its children) to the given Canvas.
     * The view must have already done a full layout before this function is
     * called.  When implementing a view, implement
     * {@link #onDraw(Canvas)} instead of overriding this method.
     * If you do need to override this method, call the superclass version.
     *
     * @param canvas The Canvas to which the View is rendered.
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Log.d(TAG, "redrawing");
        Context context = getContext();
        Paint background = new Paint();
        background.setColor(context.getColor(R.color.soundBackground));
        Paint secondary = new Paint();
        secondary.setColor(context.getColor(R.color.secondary));
        Paint primary = new Paint();
        primary.setColor(context.getColor(R.color.primary));
        Paint fadedSecondary = new Paint();
        fadedSecondary.setColor(context.getColor(R.color.fadedSecondary));

        int height = getHeight();
        int width = getWidth();
        int radius = height / 2;

        int playWidth = width - radius * 2;
        int playHeight = (int) (height * PLAY_HEIGHT_RATIO);


        int playTop = radius - playHeight / 2;
        int playBottom = playTop + playHeight;
        int playLeft = radius;
        int playRight = playLeft + playWidth;
        int currentPosition = (int) ((1.0 * currentTime / totalLength) * playWidth) + radius;
        Log.d(TAG, currentPosition + " + curr");
        // background
        canvas.drawCircle(radius, radius, radius, background);
        canvas.drawRect(radius, 0, width - radius, height, background);
        canvas.drawCircle(width - radius, radius, radius, background);

        // playtime view
        canvas.drawRect(playLeft, playTop, currentPosition, playBottom, fadedSecondary);
        canvas.drawRect(currentPosition, playTop, playRight, playBottom, primary);

        // slider
        canvas.drawCircle(currentPosition, radius, (int) (height * SLIDER_RATIO), secondary);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int xPoint = (int) event.getX();

        int height = getHeight();
        int width = getWidth();
        int radius = height / 2;

        int playWidth = width - radius * 2;
        int currentPosition = 0;
        int interval = playWidth / totalLength;
        double effectivePosition = xPoint - radius;
        Log.d(TAG, effectivePosition + "====");
        if (effectivePosition >  0)
        {
            currentTime = (int) Math.round(effectivePosition / interval);
            Log.d(TAG,currentPosition + ".");
        }
        if (currentPosition > totalLength)
        {
            currentTime = totalLength;
        }
        if (currentTime < 0)
        {
            currentTime = 0;
        }

        listener.positionIsSet(currentTime);
        Log.d(TAG, "listener should be called");

        this.invalidate();
        return super.onTouchEvent(event);
    }


    public void setListener(Listener l)
    {
        this.listener = l;

    }
}
