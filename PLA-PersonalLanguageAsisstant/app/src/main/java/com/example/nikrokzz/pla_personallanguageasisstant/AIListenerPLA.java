package com.example.nikrokzz.pla_personallanguageasisstant;

import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ai.api.AIListener;
import ai.api.model.AIError;
import ai.api.model.AIResponse;

/**
 * Created by NikRokzz on 10/07/2017.
 */
public class AIListenerPLA implements AIListener {

    private Context context;
    TextToSpeech ttobj;

    AIListenerPLA(Context context){
        this.context = context;
    }

    @Override
    public void onResult(AIResponse aiResponse) {
        String response =  aiResponse.getResult().getFulfillment().getSpeech().toString();
        Log.i("AIListener", "Action: " + response);
        Toast.makeText(context,response,Toast.LENGTH_LONG).show();
        final String toSpeak = response;
        //Text to speech

        //LayoutInflater li = LayoutInflater.from(context.getApplicationContext());
        LayoutInflater li = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TextView tvResponse = (TextView) li.inflate(R.layout.activity_bot_response_screen, null).findViewById(R.id.tvResponse);
        Log.i("AIListener", "Action: " + tvResponse.getText());
        tvResponse.setText(response);
        tvResponse.setTextSize(TypedValue.COMPLEX_UNIT_SP, 54);
        tvResponse.setTextColor(Color.parseColor("#000000"));


        ttobj=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null,null);
            }
        });
        ttobj.setLanguage(Locale.UK);

        //ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null,null);

    }

    @Override
    public void onError(AIError aiError) {
        Toast.makeText(context,"Error Listening..Try Again...",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAudioLevel(float v) {

    }

    @Override
    public void onListeningStarted() {
        Toast.makeText(context,"Listening.....",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onListeningCanceled() {
        Toast.makeText(context,"Listening cancelled.....",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onListeningFinished() {
        Toast.makeText(context,"Listening Finished.....",Toast.LENGTH_LONG).show();
    }
}
