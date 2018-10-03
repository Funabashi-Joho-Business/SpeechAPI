package com.example.android.converttext;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TextFragment extends Fragment implements SpeechText.OnSpeechListener {


	public TextFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_text, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String path = getArguments().getString("path");
		SpeechText.convert(getContext(),path,this);

		TextView textPath = getView().findViewById(R.id.path);
		textPath.setText(path);
		TextView textOutput = getView().findViewById(R.id.output);
		textOutput.setText("変換中");
	}

	@Override
	public void OnSpeech(RecognizeResponse response) {
		TextView textView = getView().findViewById(R.id.output);
		if(response == null)
			textView.setText("変換エラー");
		else{
			textView.setText("");
			List<SpeechRecognitionResult> results = response.getResultsList();
			for (SpeechRecognitionResult result : results) {
				// There can be several alternative transcripts for a given chunk of speech. Just use the
				// first (most likely) one here.
				SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
				textView.append(alternative.getTranscript());
			}
		}

	}
}
