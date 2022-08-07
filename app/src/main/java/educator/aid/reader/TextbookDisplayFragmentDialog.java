package educator.aid.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioProfile;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TextbookDisplayFragmentDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextbookDisplayFragmentDialog extends DialogFragment {

    public interface Listener
    {
        String getOutputLang();
    }

    private static final long FRAME_DELAY = 1000/ 60;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TEXT_KEY = "text";
    private static final String TAG = "TextbookDisplayFragment";

    private String[] text;
    //    private SoundBarView soundBarView;
    private MediaPlayer mediaPlayer;
    private EventHandler handler;
    private APIUnderstander apiUnderstander;
    private ImageButton playButton;
    private boolean isPlaying = false;
    private TextbookDisplayFragment.Listener listener;
    private Handler h;

    public TextbookDisplayFragmentDialog() {
        // Required empty public constructor
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
        listener = (TextbookDisplayFragment.Listener) getActivity();
    }

    public static TextbookDisplayFragmentDialog newInstance(String[] text) {
        TextbookDisplayFragmentDialog fragment = new TextbookDisplayFragmentDialog();
        Bundle args = new Bundle();
        args.putStringArray(TEXT_KEY, text);
        Log.d(TAG, "Creating norma;");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiUnderstander = new APIUnderstander();

        if (getArguments() != null) {
            text = getArguments().getStringArray(TEXT_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_textbook_display, container, false);
        playButton = (ImageButton) view.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = !isPlaying;
                if (mediaPlayer != null)
                {
                    if (isPlaying)
                    {
                        playButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_play));
                        mediaPlayer.start();
                    }
                    else
                    {
                        playButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.ic_media_pause));
                        mediaPlayer.pause();
                    }
                    playButton.postInvalidate();
                    playButton.invalidate();
                }
            }
        });
        LinearLayout textLayout = view.findViewById(R.id.textFields);
        for (int i = 0; i < text.length; i++)
        {
            TextView current = new TextView(getActivity());
            current.setText(text[i]);
            current.setTextSize(getResources().getDimension(R.dimen.averageText));
            current.setTextColor(getResources().getColor(R.color.secondary));
            current.setTextAppearance(R.style.normalFont);
            textLayout.addView(current);
        }
//        soundBarView = view.findViewById(R.id.soundbar);
//        soundBarView.setListener(new SoundBarView.Listener() {
//            @Override
//            public void positionIsSet(int newTime) {
//                Log.d(TAG, "called");
//                if (mediaPlayer != null)
//                {
//                    mediaPlayer.pause();
//                    playButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
//                    playButton.postInvalidate();
//                    isPlaying = false;
//                    if (newTime * 1000 < mediaPlayer.getCurrentPosition())
//                    {
//                        mediaPlayer.seekTo(newTime * 1000);
//                    }
//                }
//
//            }
//
//
//        });
        handler = new EventHandler();
        handler.handleTask(new Runnable() {
            @Override
            public void run() {
                StringBuilder queryBuilder = new StringBuilder();
                for (int i = 0; i < text.length; i++)
                {
                    queryBuilder.append(text[i]);
                }
                apiUnderstander.createAudioClip(queryBuilder.toString(), listener.getOutputLang(), getActivity(), new APIUnderstander.Listener<MediaPlayer>() {
                    @Override
                    public void onSuccess(MediaPlayer item) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Audio Clip Loaded!", Toast.LENGTH_SHORT).show();
                                isPlaying = false;
                                mediaPlayer = item;
//                                soundBarView.setCurrentLength(mediaPlayer.getDuration());
                            }
                        });


                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
            }
        }, 1, 0);
        h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null
                ) {
                    if (isPlaying)
                    {
//                        soundBarView.setCurrentPlayTime(mediaPlayer.getCurrentPosition());
                    }
//                    Log.d(TAG, mediaPlayer.getCurrentPosition() + ", " + mediaPlayer.getDuration());
                }
                h.postDelayed(this,FRAME_DELAY);
            }
        }, FRAME_DELAY);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = onCreateView(getLayoutInflater(), null, savedInstanceState);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }
}