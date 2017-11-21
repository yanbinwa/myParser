package com.emotibot.parser.service.video2;

import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(Video2ServiceImpl.class);
    private static final String CONTROLLER_TAG = "video";
    
    @Autowired
    StepController stepController;
    
    @Override
    public String getVideoName(String sentence)
    {
        logger.info("");
        logger.info("-------------- start -------------");
        String ret = stepController.execute(CONTROLLER_TAG, sentence);
        logger.info("-------------- end -------------");
        logger.info("");
        return ret;
    }

}
