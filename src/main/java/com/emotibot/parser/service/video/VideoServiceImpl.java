package com.emotibot.parser.service.video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.emotibot.correction.service.CorrectionService;
import com.emotibot.middleware.context.Context;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.common.SentenceType;
import com.emotibot.parser.service.video.step.CorrectionVideoNameStep;
import com.emotibot.parser.service.video.step.ParseSentenceTypeStep;
import com.emotibot.parser.service.video.step.ParseVideoNameStep;

@Service("videoService")
@EnableAutoConfiguration
@EnableConfigurationProperties
public class VideoServiceImpl implements VideoService
{
    @Autowired
    CorrectionService correctionService;
    
    @Override
    public String getVideoName(String sentence)
    {
        if (sentence == null || sentence.trim().isEmpty())
        {
            return null;
        }
        Context context = new Context();
        context.setValue(Constants.SENTENCE_KEY, sentence);
        boolean ret = isNegetiveSentence(context);
        if(ret)
        {
            return "那您想看什么呀~";
        }
        String retStr = parseVideoName(context);
        if (retStr != null)
        {
            return retStr;
        }
        retStr = correctionVideoName(context);
        if (retStr != null)
        {
            return retStr;
        }
        return "找不到相应的电影";
    }
    
    private boolean isNegetiveSentence(Context context)
    {
        context.clearOutputMap();
        ParseSentenceTypeStep step = new ParseSentenceTypeStep();
        long startTime = System.currentTimeMillis();
        step.execute(context);
        long endTime = System.currentTimeMillis();
        System.out.println("ParseSentenceTypeStep: [" + (endTime - startTime) + "]ms");
        
        SentenceType type = (SentenceType) context.getValue(Constants.SENTENCE_TYPE_KEY);
        if (type == null)
        {
            return false;
        }
        return type == SentenceType.NEGETIVE;
    }

    private String parseVideoName(Context context)
    {
        context.clearOutputMap();
        ParseVideoNameStep step = new ParseVideoNameStep();
        long startTime = System.currentTimeMillis();
        step.execute(context);
        long endTime = System.currentTimeMillis();
        System.out.println("ParseVideoNameStep: [" + (endTime - startTime) + "]ms");
        String videoName = (String) context.getValue(Constants.VIDEO_NAME_KEY);
        if (videoName != null)
        {
            return videoName; 
        }
        String starName = (String) context.getValue(Constants.START_NAME_KEY);
        if (starName != null)
        {
            return starName; 
        }
        return null;
    }
    
    private String correctionVideoName(Context context)
    {
        context.clearOutputMap();
        CorrectionVideoNameStep step = new CorrectionVideoNameStep(correctionService);
        long startTime = System.currentTimeMillis();
        step.execute(context);
        long endTime = System.currentTimeMillis();
        System.out.println("CorrectionVideoNameStep: [" + (endTime - startTime) + "]ms");
        String correctedVideoName = (String) context.getValue(Constants.CORRECTED_VIDEO_NAME_KEY);
        return correctedVideoName;
    }
}
