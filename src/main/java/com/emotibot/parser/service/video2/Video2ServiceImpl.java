package com.emotibot.parser.service.video2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.emotibot.middleware.controller.controller.StepController;

@Service("video2Service")
@EnableAutoConfiguration
@EnableConfigurationProperties
public class Video2ServiceImpl implements Video2Service
{

    private static final String CONTROLLER_TAG = "video";
    
    @Autowired
    StepController stepController;
    
    @Override
    public String getVideoName(String sentence)
    {
        return stepController.execute(CONTROLLER_TAG, sentence);
    }

}
