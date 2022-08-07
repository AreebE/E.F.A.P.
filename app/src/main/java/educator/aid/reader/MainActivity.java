package educator.aid.reader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
    implements PictureFragment.Listener,
    LanguageChangeFragment.Listener,
        TextbookDisplayFragmentDialog.Listener,
        TextbookDisplayFragment.Listener {

    private String fromText;
    private String toText;

    private static final String TAG = "MainActivity";
    private PictureFragment pictureFragment;
    private String[] text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromText = "en";
        toText = "en";
        pictureFragment = (PictureFragment) PictureFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.pictureFragment, pictureFragment)
                .commit();

    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     *
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     *
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     *
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.information:
                Log.d(TAG, Arrays.toString(text));
                if (findViewById(R.id.textFragment) == null
                    && text != null)
                {
                    TextbookDisplayFragmentDialog.newInstance(text).show(getSupportFragmentManager(), TAG);
                }
                break;
            case R.id.language:
                LanguageChangeFragment.newInstance(fromText, toText).show(getSupportFragmentManager(), TAG);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void pictureTaken(String[] text) {
        if (findViewById(R.id.textFragment) != null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.textFragment, TextbookDisplayFragment.newInstance(text))
                    .commit();
        }
        this.text = text;
    }

    @Override
    public String[] getLanguages() {
        return new String[]{fromText, toText};
    }

    @Override
    public void onLanguageChange(String from, String to) {
        this.fromText = from;
        this.toText = to;
        pictureFragment.processImage();
    }

    @Override
    public String getOutputLang() {
        return toText;
    }
}