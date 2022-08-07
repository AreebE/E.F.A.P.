package educator.aid.reader;

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

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TextbookDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextbookDisplayFragment extends Fragment {

    private static final long FRAME_DELAY = 1000/ 60;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TEXT_KEY = "text";
    private static final String TAG = "TextbookDisplayFragment";

    private String[] text;
    private SoundBarView soundBarView;
    private MediaPlayer mediaPlayer;
    private EventHandler handler;
    private APIUnderstander apiUnderstander;
    private ImageButton playButton;
    private boolean isPlaying = false;
    private Handler h;

    public TextbookDisplayFragment() {
        // Required empty public constructor
    }


    public static TextbookDisplayFragment newInstance(String[] text) {
        TextbookDisplayFragment fragment = new TextbookDisplayFragment();
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
                        mediaPlayer.stop();
                    }
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
            textLayout.addView(current);
        }
        soundBarView = (SoundBarView) view.findViewById(R.id.soundbar);
        handler = new EventHandler();
        handler.handleTask(new Runnable() {
            @Override
            public void run() {
                StringBuilder queryBuilder = new StringBuilder();
                for (int i = 0; i < text.length; i++)
                {
                    queryBuilder.append(text[i]);
                }
                apiUnderstander.createAudioClip(queryBuilder.toString(), getActivity(), new APIUnderstander.Listener<MediaPlayer>() {
                    @Override
                    public void onSuccess(MediaPlayer item) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Audio Clip Loaded!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        isPlaying = false;
                        mediaPlayer = item;
                        soundBarView.setCurrentPlayTime(mediaPlayer.getDuration());
                        soundBarView.setListener(new SoundBarView.Listener() {
                            @Override
                            public void positionIsSet(int newTime) {
                                mediaPlayer.seekTo(newTime * 1000);
                            }

                            @Override
                            public void positionIsChanging() {
                                mediaPlayer.pause();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
            }
        }, 1, 0);
        h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlaying)
                {
                    soundBarView.setCurrentPlayTime(mediaPlayer.getCurrentPosition());
                }
            }
        }, FRAME_DELAY);
        return view;
    }
}