package educator.aid.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private static final String TEXT_KEY = "text";
    private static final String TAG = "TextbookDisplayDialogFragment";


    private String[] text;
    private SoundBarView soundBarView;

    public TextbookDisplayFragmentDialog() {

    }


    public static TextbookDisplayFragmentDialog newInstance(String[] text) {
        TextbookDisplayFragmentDialog fragment = new TextbookDisplayFragmentDialog();
        Bundle args = new Bundle();
        Log.d(TAG, "Creating dialo;");

        args.putStringArray(TEXT_KEY, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            text = getArguments().getStringArray(TEXT_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_textbook_display, null);
        LinearLayout textLayout = view.findViewById(R.id.textFields);
        for (int i = 0; i < text.length; i++)
        {
            TextView current = new TextView(getActivity());
            current.setText(text[i]);
            current.setTextSize(getResources().getDimension(R.dimen.averageText));
            current.setTextColor(getResources().getColor(R.color.secondary));
            textLayout.addView(current);
        }
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