package educator.aid.reader;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIUnderstander
{
    private static final String TAG = "APIUnderstander";
    private static final String BREAK_CHARACTERS = "<>";

    public static final HashMap<String, String> locality = new HashMap<String, String>()
    {
        {
            put(TranslateLanguage.ARABIC, "eg");
            put(TranslateLanguage.BULGARIAN, "bg");
            put(TranslateLanguage.CATALAN, "es");
            put(TranslateLanguage.CHINESE, "cn");
            put(TranslateLanguage.CROATIAN, "hr");
            put(TranslateLanguage.CZECH, "cz");
            put(TranslateLanguage.DANISH, "dk");
            put(TranslateLanguage.DUTCH, "be");
            put(TranslateLanguage.ENGLISH, "us");
            put(TranslateLanguage.FINNISH, "fi");
            put(TranslateLanguage.FRENCH, "fr");
            put(TranslateLanguage.GERMAN, "de");
            put(TranslateLanguage.GREEK, "gr");
            put(TranslateLanguage.HEBREW, "il");
            put(TranslateLanguage.HINDI, "in");
            put(TranslateLanguage.HUNGARIAN, "hu");
            put(TranslateLanguage.INDONESIAN, "id");
            put(TranslateLanguage.ITALIAN, "it");
            put(TranslateLanguage.JAPANESE, "jp");
            put(TranslateLanguage.KOREAN, "kr");
            put(TranslateLanguage.MALAY, "my");
            put(TranslateLanguage.NORWEGIAN, "no");
            put(TranslateLanguage.POLISH, "pl");
            put(TranslateLanguage.PORTUGUESE, "pt");
            put(TranslateLanguage.ROMANIAN, "ro");
            put(TranslateLanguage.RUSSIAN, "ru");
            put(TranslateLanguage.SLOVAK, "sk");
            put(TranslateLanguage.SLOVENIAN, "si");
            put(TranslateLanguage.SPANISH, "es");
            put(TranslateLanguage.SWEDISH, "se");
            put(TranslateLanguage.TAMIL, "in");
            put(TranslateLanguage.THAI, "th");
            put(TranslateLanguage.TURKISH, "tr");
            put(TranslateLanguage.VIETNAMESE, "vn");
        }

    };

    public interface Listener<T>
    {
        public void onSuccess(T item);
        public void onFailure(Exception e);
    }

    private TextRecognizer reader;
    public APIUnderstander()
    {
        reader = TextRecognition.getClient(new TextRecognizerOptions.Builder()
                .build());
    }

    private void understandImage(Bitmap image, Listener<Text> listener)
    {
        InputImage processedImg = InputImage.fromBitmap(image, Surface.ROTATION_0);
        reader.process(processedImg)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        listener.onSuccess(text);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure(e);
                    }
                });
    }

    public void readText(Bitmap image, Context c, Listener<String> fragListener)
    {
        understandImage(image, new Listener<Text>() {
            @Override
            public void onSuccess(Text item) {
                StringBuilder queryBuilder = new StringBuilder();

//                ArrayList<String> items = new ArrayList<>();
                for (Text.TextBlock block: item.getTextBlocks())
                {
                    for (Text.Line line: block.getLines()) {
                        queryBuilder.append(line.getText())
                                .append(BREAK_CHARACTERS);
                    }
                }
                Log.d(TAG, "----" + queryBuilder.toString());
                fragListener.onSuccess(queryBuilder.toString());
            }

            @Override
            public void onFailure(Exception e) {
                fragListener.onFailure(e);
            }
        });
    }

    public void translate(String query, String from, String to, Context c, Listener<String[]> listener)
    {

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(from))
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(to))
                .build();
        Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                translator.translate(query)
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String s) {
                                                Log.d(TAG, "Result -- " + s);
                                                listener.onSuccess(s.split(BREAK_CHARACTERS));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "failed -- " + e.toString());
                                                listener.onFailure(e);
                                            }
                                        });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "failed :((" + e.toString());
                                listener.onFailure(e);
                            }
                        });




    }

    public void createAudioClip(String text, String lang, Context context, Listener<MediaPlayer> listener)  {
        try
        {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("src", text)
                    .add("hl", lang + "-" + locality.get(lang))
                    .add("r", "0")
                    .add("c", "mp3")
                    .add("f", "8khz_8bit_mono")
                    .build();

            Request request = new Request.Builder()
                    .url("https://voicerss-text-to-speech.p.rapidapi.com/?key=" + context.getString(R.string.API_KEY))
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("X-RapidAPI-Key", context.getString(R.string.RAPID_API_KEY))
                    .addHeader("X-RapidAPI-Host", context.getString(R.string.RAPID_API_HOST))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                byte[] audioOutputs = response.body().bytes();
//                AudioTrack track = new AudioTrack(
//                        AudioManager.STREAM_MUSIC,
//                        44100,
//                        AudioFormat.CHANNEL_IN_DEFAULT,
//                        AudioFormat.ENCODING_MP3,
//                        audioOutputs.length * 2,
//                        AudioTrack.MODE_STREAM);
//
//                boolean canPlay = false;
//                while (!canPlay)
//                {
//                    Log.d(TAG,"EEEEEE");
//                    try
//                    {
//                        track.play();
//                        canPlay = true;
//                    } catch (IllegalStateException iae)
//                    {
//                        canPlay = false;
//                    }
//                }
//                track.write(audioOutputs, 0, audioOutputs.length);

                File path=new File(context.getCacheDir()+"/text.3gp");

                FileOutputStream fos = new FileOutputStream(path);
                fos.write(audioOutputs);
                fos.close();

                MediaPlayer mediaPlayer = new MediaPlayer();

                mediaPlayer.setDataSource(context.getCacheDir()+"/text.3gp");

                mediaPlayer.prepare();

                listener.onSuccess(mediaPlayer);
            }
            else
            {
                listener.onFailure(new Exception("Could not read data."));
            }
        } catch (IOException ioe)
        {
            listener.onFailure(ioe);
        }

    }


}
