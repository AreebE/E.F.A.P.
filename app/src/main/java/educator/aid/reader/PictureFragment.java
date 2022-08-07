package educator.aid.reader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.metrics.Event;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.text.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PictureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PictureFragment extends Fragment {

    public interface Listener
    {
        void pictureTaken(String[] text);
        String[] getLanguages();
    }

    private static final String TAG = "PictureFragment";

    private static final int REQUEST_GET_PICTURE = 2031;
    private static final int REQUEST_UPLOAD_PICTURE = 2134;
    private static final int REQUEST_ASK_FOR_CAMERA = 401;

    private final Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    private ImageButton cameraButton;
    private ImageButton uploadButton;
    private ImageButton rotateButton;
    private EventHandler eventHandler;
    private String language;

    private ImageView textbookView;

    private APIUnderstander apiUnderstander;
    private String[] currentList;
    private Listener listener;


    public PictureFragment()
    {
        super();
    }

    public static Fragment newInstance() {
        return new PictureFragment();
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (Listener) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_picture, null);
        apiUnderstander = new APIUnderstander();
        textbookView = (ImageView) v.findViewById(R.id.textbookImage);
        cameraButton = v.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "is clicked");
                if (ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED)
                {
                    startActivityForResult(pictureIntent, REQUEST_GET_PICTURE);
                }
                else
                {
                    Log.d(TAG,"Is printed");

                    requestPermissions(
                            new String[] { Manifest.permission.CAMERA },
                            REQUEST_ASK_FOR_CAMERA);
                }

            }
        });

        uploadButton = (ImageButton) v.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select an image to read."), REQUEST_UPLOAD_PICTURE);
            }
        });

        rotateButton = (ImageButton) v.findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable) textbookView.getDrawable()).getBitmap();
                Matrix rotation = new Matrix();
                rotation.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotation, true);
                processImage(bitmap);
            }
        });

        Log.d(TAG, cameraButton.hasOnClickListeners() + "  EEEE");

        return v;
    }



    @Override
    public void onStart() {
        super.onStart();
        eventHandler = new EventHandler();
    }


    @Override
    public void onStop() {
        super.onStop();
        eventHandler.removeAllRequests();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_UPLOAD_PICTURE:
                if (resultCode == Activity.RESULT_OK)
                {
                    try
                    {
                        InputStream stream = getActivity().getContentResolver().openInputStream(data.getData());
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        processImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case REQUEST_GET_PICTURE:

                if (resultCode == Activity.RESULT_OK)
                {
                    Log.d(TAG, data.getExtras() + "");
                    Bitmap image = (Bitmap) data.getParcelableExtra("data");
                    processImage(image);
                }
                break;

        }
    }

    public void processImage()
    {
        processImage(((BitmapDrawable) ((ImageView) getView().findViewById(R.id.textbookImage)).getDrawable()).getBitmap());
    }

    public void processImage(Bitmap b)
    {
        Bitmap copy = b.copy(b.getConfig(), false);

        EventHandler current = new EventHandler();
        current.handleTask(new Runnable() {
            @Override
            public void run() {
                apiUnderstander.readText(copy, getActivity(), new APIUnderstander.Listener<String>() {

                    @Override
                    public void onSuccess(String item) {
                        current.handleTask(new Runnable() {
                            @Override
                            public void run() {
                                String[] languages = listener.getLanguages();
                                apiUnderstander.translate(item, languages[0], languages[1], getActivity(), new APIUnderstander.Listener<String[]>() {
                                    @Override
                                    public void onSuccess(String[] item) {
                                        currentList = item;
                                        listener.pictureTaken(currentList);
                                        current.removeAllRequests();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        current.removeAllRequests();
                                    }
                                });
                            }
                        }, 1, 0);

                    }

                    @Override
                    public void onFailure(Exception e) {
                        current.removeAllRequests();
                    }
                });

            }
        }, 1, 0);
        textbookView.setImageBitmap(b);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_ASK_FOR_CAMERA:
                if (ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED)
                {
                    startActivityForResult(pictureIntent, REQUEST_GET_PICTURE);
                }
                else
                {
                    Toast.makeText(getContext(), R.string.askToEnable, Toast.LENGTH_SHORT);
                }
        }
    }

    /*
super.onResume();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = (ImageView) findViewById(R.id.sampleImage);
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                new APIUnderstander().readText(bitmap, new APIUnderstander.Listener<String[]>() {
                    @Override
                    public void onSuccess(String[] item) {
                        Log.d(TAG,"success!");
                        LinearLayout layout = (LinearLayout) findViewById(R.id.textFields);
                        for (int i = 0; i < item.length; i++)
                        {
                            String currentItem = item[i];
                            TextView textView = new TextView(MainActivity.this);
                            textView.setText(currentItem);
                            textView.setTextSize(getResources().getDimension(R.dimen.averageText));
                            textView.setTextColor(getResources().getColor((i % 2 == 0) ? R.color.red : R.color.blue));
                            layout.addView(textView);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "drgnmlkdfmgl. :( " + e.toString());
                    }
                });
            }
        });

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                new APIUnderstander().createAudioClip("I want to see how this works, ok? OK!!!! Alright. O... who?", MainActivity.this, new APIUnderstander.Listener<MediaPlayer>() {
                    @Override
                    public void onSuccess(MediaPlayer item) {
                        item.start();
                        item.getCurrentPosition();
                        item.getDuration();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
            }
        });
 */

}