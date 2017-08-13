package com.example.nikrokzz.pla_personallanguageasisstant;

import android.app.Application;

import ai.api.android.AIConfiguration;
import ai.api.android.AIService;


/**
 * Created by NikRokzz on 10/07/2017.
 */
public class SystemGlobal  extends Application {

    private final AIConfiguration config;
    private AIService aiService;

    {
        config = new AIConfiguration("9de52425d6fc4fa19101039dc49d6bd5",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
    }

    public AIConfiguration getGlobalAIConfiguration() {
        return config;
    }

    public AIService getGlobalAIService() {
        aiService = AIService.getService(getApplicationContext(), config);
        return aiService;
    }




}
