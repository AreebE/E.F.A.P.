package educator.aid.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class LanguageChangeFragment extends DialogFragment {

    public interface Listener
    {
        void onLanguageChange(String from, String to);
    }
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String FROM_LANG = "from";
    private static final String TO_LANG = "to";

    public static final List<String> languageTags = new ArrayList<String>(){{
        Iterator<String> languages = APIUnderstander.locality.keySet().iterator();
        while(languages.hasNext())
        {
            add(languages.next());
        }
        Collections.sort(this);
    }};
    private static final String TAG = "LanguageChangeFrag";
    public static final List<String> languageNames = new ArrayList<String>(){
        {
            for (int i = 0; i < languageTags.size(); i++)
            {
                Log.d(TAG, languageTags.get(i) + " -- " + TranslateLanguage.zza(languageTags.get(i)) + " -- " + TranslateLanguage.fromLanguageTag(languageTags.get(i)));
                add(TranslateLanguage.fromLanguageTag(languageTags.get(i)));
            }
        }
    };

    private Listener listener;
    private int fromIndex;
    private int toIndex;

    public LanguageChangeFragment() {
        // Required empty public constructor
    }


    public static LanguageChangeFragment newInstance(String from, String to) {
        LanguageChangeFragment fragment = new LanguageChangeFragment();
        Bundle args = new Bundle();
        args.putString(FROM_LANG, from);
        args.putString(TO_LANG, to);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.listener = (Listener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String from = getArguments().getString(FROM_LANG);
            String to = getArguments().getString(TO_LANG);
            int numFound = 0;
            for (int i = 0; i < languageTags.size() && numFound < 2; i++)
            {
                if (languageTags.get(i).equals(from))
                {
                    fromIndex = i;
                    numFound++;
                }
                if (languageTags.get(i).equals(to))
                {
                    toIndex = i;
                    numFound++;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_language_change, container, false);

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = onCreateView(getLayoutInflater(), null, savedInstanceState);


        Spinner toPrompts = (Spinner) view.findViewById(R.id.toText);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, languageNames);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toPrompts.setAdapter(toAdapter);
        toPrompts.setSelection(toIndex);

        Spinner fromPrompts = (Spinner) view.findViewById(R.id.fromText);
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, languageNames);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromPrompts.setAdapter(fromAdapter);
        fromPrompts.setSelection(fromIndex);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String toTag = languageTags.get(toPrompts.getSelectedItemPosition());
                        String fromTag = languageTags.get(fromPrompts.getSelectedItemPosition());
                        Log.d(TAG, "from-to == " + fromTag + toTag);
                        listener.onLanguageChange(fromTag, toTag);
                        dismiss();
                    }
                })
                .create();
    }
}