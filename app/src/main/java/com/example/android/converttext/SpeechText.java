package com.example.android.converttext;

import android.content.Context;
import android.os.Handler;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class SpeechText {
    final static String CREDENTIALS = "credentials.json";
    public interface OnSpeechListener{
        public void OnSpeech(RecognizeResponse response);
    }

    public static void convert(final Context context, final String filePath,final OnSpeechListener listener) {
        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                try {
                    convertText(context,filePath,handler,listener);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void convertText(final Context context, String filePath, Handler handler, final OnSpeechListener listener) throws IOException {

        try {

            InputStream credentialsStream = context.getAssets().open(CREDENTIALS);
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
            SpeechSettings speechSettings =
                    SpeechSettings.newBuilder()
                            .setCredentialsProvider(credentialsProvider)
                            .build();

            SpeechClient speech = SpeechClient.create(speechSettings);

            FileInputStream input = new FileInputStream(filePath);
            ByteString audioBytes = ByteString.readFrom(input, input.available());

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.AMR_WB)
                    .setLanguageCode("ja-JP")
                    .setSampleRateHertz(16000)
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // Use blocking call to get audio transcript
            final RecognizeResponse response = speech.recognize(config, audio);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.OnSpeech(response);
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.OnSpeech(null);
                }
            });
        }

    }
}
