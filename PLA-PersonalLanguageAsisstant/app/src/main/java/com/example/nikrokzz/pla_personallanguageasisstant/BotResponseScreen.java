package com.example.nikrokzz.pla_personallanguageasisstant;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.ResponseMessage;


public class BotResponseScreen extends ActionBarActivity implements AIListener, TextToSpeech.OnInitListener {

    AIListener aiListenerpla;
    AIService aiService;
    AIDataService aiDataService;
    AIRequest aiRequest;
    SystemGlobal systemGlobal;
    TextToSpeech ttobj;
    ImageView ivSarahBot;
    ImageView ivSarahBotStill;
    TextView tvResponse;
    String toSpeak;
    Handler handler;
    Runnable refresh;
    EditText etQuery;
    ImageButton ibMicrophone;
    ImageButton ibSend;
    List<ResponseMessage> responseMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_response_screen);

        //set the height of microphone to inout query box
        etQuery = (EditText) findViewById(R.id.etQuery);
        ibMicrophone = (ImageButton) findViewById(R.id.ibMicrophone);
        ibSend = (ImageButton) findViewById(R.id.ibSend);



        //systemGlobal = ((SystemGlobal)getApplicationContext());

        //aiService = systemGlobal.getGlobalAIService();

        final AIConfiguration config = new AIConfiguration("9de52425d6fc4fa19101039dc49d6bd5",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(BotResponseScreen.this, config);
        aiDataService = new AIDataService(BotResponseScreen.this,config);


        aiService.setListener(this);

        ivSarahBot = (ImageView) findViewById(R.id.ivSarahBot);
        ivSarahBotStill = (ImageView) findViewById(R.id.ivSarahBotStill);
        tvResponse = (TextView) findViewById(R.id.tvResponse);


        //set on long click listener on Microphone icon
        ibMicrophone.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                aiService.startListening();

                return true;
            }
        });

        //set edit text query listener
        etQuery.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("Response: " ,"beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("Response: " ,"onTextChanged");
                //after input entered , make send button visible
                if (s.length() == 0)
                {
                    ibSend.setVisibility(View.GONE);
                    ibMicrophone.setVisibility(View.VISIBLE);
                }else
                {
                    ibSend.setVisibility(View.VISIBLE);
                    ibMicrophone.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("Response: " ,"afterTextChanged");
            }
        });



        handler = new Handler();
        aiListenerpla = new AIListenerPLA(getApplicationContext());


        //GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(imageView);
        //Glide.with(this).load(R.mipmap.sarah_bot).into(imageViewTarget);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bot_response_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonClicked(View view) throws AIServiceException {


        int id = view.getId();

        switch (id) {

            case R.id.ibSend: {

                String s = etQuery.getText().toString();

                etQuery.setText("");
                etQuery.clearFocus();

                //Hide the keyboard
                // Check if no view has focus:
                View viewFocus = this.getCurrentFocus();
                if (viewFocus != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                }



                AIRequest aiRequest = new AIRequest();

                aiRequest.setQuery(s);

                if(aiRequest==null) {
                    throw new IllegalArgumentException("aiRequest must be not null");
                }

                final AsyncTask<AIRequest, Integer, AIResponse> task =
                        new AsyncTask<AIRequest, Integer, AIResponse>() {
                            private AIError aiError;

                            @Override
                            protected AIResponse doInBackground(final AIRequest... params) {
                                final AIRequest request = params[0];
                                try {
                                    final AIResponse response =    aiDataService.request(request);
                                    // Return response
                                    return response;
                                } catch (final AIServiceException e) {
                                    aiError = new AIError(e);
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(final AIResponse response) {
                                if (response != null) {

                                    String responseAPI = response.getResult().getFulfillment().getSpeech().toString();
                                    toSpeak = responseAPI;
                                    Log.i("AIListener", "Action: " + responseAPI);
                                    Toast.makeText(BotResponseScreen.this.getApplicationContext(), responseAPI, Toast.LENGTH_LONG).show();

                                    tvResponse.setText(responseAPI);
                                    tvResponse.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                    tvResponse.setTextColor(Color.parseColor("#000000"));

                                    //audio

                                    ttobj = new TextToSpeech(BotResponseScreen.this.getApplicationContext(), BotResponseScreen.this);

                                    refresh = new Runnable() {
                                        public void run() {
                                            // Do something

                                            boolean isSpeaking =  ttobj.isSpeaking();

                                            if (!isSpeaking){
                                                ivSarahBotStill.setVisibility(View.VISIBLE);
                                                ivSarahBot.setVisibility(View.GONE);

                                                handler.removeCallbacks(refresh);
                                            }

                                            handler.postDelayed(refresh, 100);
                                        }
                                    };
                                    handler.post(refresh);

                                    ttobj.setLanguage(Locale.UK);





                                    if ( !response.getResult().getFulfillment().getMessages().isEmpty()  )
                                    {
                                        responseMessagesList =  response.getResult().getFulfillment().getMessages();
                                        int size = responseMessagesList.size();
                                        String stringJson = null;

                                        for (int l = 0;l <= size;l++ )
                                        {
                                            try{
                                                ResponseMessage responseMessage = responseMessagesList.get(l);
                                                Gson gsonVru = new Gson();
                                                stringJson =  gsonVru.toJson(responseMessage);

                                                JSONObject JSonObj = new JSONObject(stringJson); //messages array

                                                //For reading buttons
                                                if ( Integer.parseInt(JSonObj.get("type").toString())  == 4){
                                                   setButtonsTemplate(JSonObj);
                                                }
                                                else if(Integer.parseInt(JSonObj.get("type").toString())  == 0){
                                                    setTextTemplate(JSonObj);

                                                }
                                                else if (Integer.parseInt(JSonObj.get("type").toString())  == 4){
                                                    setImageButtonTemplate(JSonObj);
                                                }
                                            }
                                            catch (Exception e){ Log.v("BotResponseScreen","Error extracting Json message response api.ai"); }

                                        }

                                    }





                                } else {
                                    Log.v("Error","error");
                                }
                            }
                        };
                task.execute(aiRequest);

                //sendAIQueryTask sendAIQueryTask = new sendAIQueryTask();
                //sendAIQueryTask.execute(aiRequest);


                break;
            }


        }

    }

    private void setTextTemplate(JSONObject JSonObj) throws JSONException {
        String speechString = (String) JSonObj.get("speech");

        tvResponse.setText(speechString);
        tvResponse.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvResponse.setTextColor(Color.parseColor("#000000"));

    }

    private void setButtonsTemplate(JSONObject JSonObj) throws JSONException {

        JSONObject payloadJsonObj = JSonObj.getJSONObject("payload");
        JSONArray buttonsJSonArray =  payloadJsonObj.getJSONArray("buttons");

        LinearLayout l_layout = (LinearLayout) findViewById(R.id.contentLinearLayout);

        //clear linear layout previously populated
        if(((LinearLayout) l_layout).getChildCount() > 0)
            ((LinearLayout) l_layout).removeAllViews();

        l_layout.setOrientation(LinearLayout.HORIZONTAL);

        for (int m=0;m<=buttonsJSonArray.length();m++)
        {
            final JSONObject singleBtnJSon = (JSONObject) buttonsJSonArray.get(m);

            Button btn1 = new Button(BotResponseScreen.this);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AIRequest aiRequest = new AIRequest();

                    try {
                        aiRequest.setQuery(singleBtnJSon.get("title").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(aiRequest==null) {
                        throw new IllegalArgumentException("aiRequest must be not null");
                    }

                    final AsyncTask<AIRequest, Integer, AIResponse> task =
                            new AsyncTask<AIRequest, Integer, AIResponse>() {
                                private AIError aiError;

                                @Override
                                protected AIResponse doInBackground(final AIRequest... params) {
                                    final AIRequest request = params[0];
                                    try {
                                        final AIResponse response =    aiDataService.request(request);
                                        // Return response
                                        return response;
                                    } catch (final AIServiceException e) {
                                        aiError = new AIError(e);
                                        return null;
                                    }
                                }

                                @Override
                                protected void onPostExecute(final AIResponse response) {
                                    if (response != null) {

                                        String responseAPI = response.getResult().getFulfillment().getSpeech().toString();
                                        toSpeak = responseAPI;
                                        Log.i("AIListener", "Action: " + responseAPI);
                                        Toast.makeText(BotResponseScreen.this.getApplicationContext(), responseAPI, Toast.LENGTH_LONG).show();

                                        tvResponse.setText(responseAPI);
                                        tvResponse.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                        tvResponse.setTextColor(Color.parseColor("#000000"));

                                        //audio

                                        try {
                                            if (singleBtnJSon.get("title").toString() == "Proceed"){
                                                ivSarahBotStill.setVisibility(View.GONE);
                                                ivSarahBot.setVisibility(View.GONE);
                                                handler.removeCallbacks(refresh);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                        ttobj = new TextToSpeech(BotResponseScreen.this.getApplicationContext(), BotResponseScreen.this);

                                        refresh = new Runnable() {
                                            public void run() {
                                                // Do something

                                                boolean isSpeaking =  ttobj.isSpeaking();

                                                try {
                                                    if (singleBtnJSon.get("title").toString() == "Proceed"){
                                                        ivSarahBotStill.setVisibility(View.GONE);
                                                        ivSarahBot.setVisibility(View.GONE);
                                                        handler.removeCallbacks(refresh);
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                if (!isSpeaking){
                                                    ivSarahBotStill.setVisibility(View.VISIBLE);
                                                    ivSarahBot.setVisibility(View.GONE);

                                                    handler.removeCallbacks(refresh);
                                                }

                                                handler.postDelayed(refresh, 100);
                                            }
                                        };
                                        handler.post(refresh);

                                        ttobj.setLanguage(Locale.UK);





                                        if ( !response.getResult().getFulfillment().getMessages().isEmpty()  )
                                        {
                                            responseMessagesList =  response.getResult().getFulfillment().getMessages();
                                            int size = responseMessagesList.size();
                                            String stringJson = null;

                                            for (int l = 0;l <= size;l++ )
                                            {
                                                try{
                                                    ResponseMessage responseMessage = responseMessagesList.get(l);
                                                    Gson gsonVru = new Gson();
                                                    stringJson =  gsonVru.toJson(responseMessage);

                                                    JSONObject JSonObj = new JSONObject(stringJson); //messages array

                                                    //For reading buttons
                                                    if ( Integer.parseInt(JSonObj.get("type").toString())  == 4){
                                                        setButtonsTemplate(JSonObj);
                                                    }
                                                    else if(Integer.parseInt(JSonObj.get("type").toString())  == 0){
                                                        setTextTemplate(JSonObj);

                                                    }
                                                    else if (Integer.parseInt(JSonObj.get("type").toString())  == 4){
                                                        setImageButtonTemplate(JSonObj);
                                                    }
                                                }
                                                catch (Exception e){ Log.v("BotResponseScreen","Error extracting Json message response api.ai"); }

                                            }

                                        }





                                    } else {
                                        Log.v("Error","error");
                                    }
                                }
                            };
                    task.execute(aiRequest);



                }
            });
            btn1.setText(singleBtnJSon.get("title").toString());

            l_layout.addView(btn1);
        }
    }


    private void setImageButtonTemplate(JSONObject JSonObj) throws JSONException {

        JSONObject payloadJsonObj = JSonObj.getJSONObject("payload");
        JSONArray buttonsJSonArray =  payloadJsonObj.getJSONArray("images");

        LinearLayout l_layout = (LinearLayout) findViewById(R.id.contentLinearLayout);

        //clear linear layout previously populated
        if(((LinearLayout) l_layout).getChildCount() > 0)
            ((LinearLayout) l_layout).removeAllViews();

        l_layout.setOrientation(LinearLayout.HORIZONTAL);

        for (int p=0;p<=buttonsJSonArray.length();p++)
        {
            JSONObject singleBtnJSon = (JSONObject) buttonsJSonArray.get(p);
            String image_url = singleBtnJSon.get("url").toString();
            //ViewGroup.LayoutParams rlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
            //l_layout.setLayoutParams(rlp);

            //ViewGroup.LayoutParams rlpBtn = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
            ImageButton logoBtn = new ImageButton(BotResponseScreen.this);
            Picasso.with(BotResponseScreen.this).load(image_url).resize(600,200).into(logoBtn);
            //logoBtn.setLayoutParams(rlpBtn);
            //ViewGroup.LayoutParams params =  logoBtn.getLayoutParams();
            //params.height = 60;
            //params.width = 60;
            //logoBtn.setLayoutParams(params);
            l_layout.addView(logoBtn, p);

            //Picasso.with(BotResponseScreen.this).load(image_url).placeholder(l_layout).into(logoBtn);
        }
    }


    @Override
    public void onResult(AIResponse aiResponse) {
        String response = aiResponse.getResult().getFulfillment().getSpeech().toString();
        Log.i("AIListener", "Action: " + response);
        Toast.makeText(BotResponseScreen.this.getApplicationContext(), response, Toast.LENGTH_LONG).show();
        toSpeak = response;
        //Text to speech

        Log.i("AIListener", "Action: " + tvResponse.getText());


        if ( !aiResponse.getResult().getFulfillment().getMessages().isEmpty()  )
        {
            responseMessagesList =  aiResponse.getResult().getFulfillment().getMessages();
            int size = responseMessagesList.size();
            String stringJson = null;

            for (int i = 0;i <= size;i++ )
            {
                try{
                    ResponseMessage responseMessage = responseMessagesList.get(i);
                    Gson gsonVru = new Gson();
                    stringJson =  gsonVru.toJson(responseMessage);

                    JSONObject JSonObj = new JSONObject(stringJson); //messages array

                    //For reading buttons
                    if ( Integer.parseInt(JSonObj.get("type").toString())  == 4){
                        setButtonsTemplate(JSonObj);
                    }
                    else if(Integer.parseInt(JSonObj.get("type").toString())  == 0){
                       setTextTemplate(JSonObj);

                    }
                    else if (Integer.parseInt(JSonObj.get("type").toString())  == 4){
                        setImageButtonTemplate(JSonObj);
                    }






                }
                catch (Exception e){ Log.v("BotResponseScreen","Error extracting Json message response api.ai"); }

            }

        }


        ttobj = new TextToSpeech(BotResponseScreen.this.getApplicationContext(), BotResponseScreen.this);

        refresh = new Runnable() {
            public void run() {
                // Do something

                boolean isSpeaking =  ttobj.isSpeaking();

                if (!isSpeaking){
                    ivSarahBotStill.setVisibility(View.VISIBLE);
                    ivSarahBot.setVisibility(View.GONE);

                    handler.removeCallbacks(refresh);
                }

                handler.postDelayed(refresh, 100);
            }
        };
        handler.post(refresh);

        ttobj.setLanguage(Locale.UK);


        //ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null,null);

    }



    @Override
    public void onError(AIError aiError) {

    }

    @Override
    public void onAudioLevel(float v) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);

            ttobj.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    Log.i("Action:", "TTS finished");
                    ttobj.stop();
                    ttobj.shutdown();
                    ivSarahBotStill.setVisibility(View.VISIBLE);
                    ivSarahBot.setVisibility(View.GONE);
                }

                @Override
                public void onError(String utteranceId) {
                    Log.i("Action:", "TTS error");
                    ttobj.stop();
                    ttobj.shutdown();
                    ivSarahBotStill.setVisibility(View.VISIBLE);
                    ivSarahBot.setVisibility(View.GONE);
                }

                @Override
                public void onStart(String utteranceId) {
                    Log.i("Action:", "TTS on start");
                    ttobj.stop();
                    ttobj.shutdown();
                }
            });

            ivSarahBotStill.setVisibility(View.GONE);
            ivSarahBot.setVisibility(View.VISIBLE);


        } else {
            Log.i("Action:", "Initilization Failed!");
            ttobj.shutdown();
            ivSarahBotStill.setVisibility(View.VISIBLE);
            ivSarahBot.setVisibility(View.GONE);
        }


    }




}


