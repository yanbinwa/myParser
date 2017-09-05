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
        ParseSentenceTypeStep step = new ParseSentenceTypeStep();
        step.setContext(context);
        step.execute();
        SentenceType type = step.getSentenceType();
        if (type == null)
        {
            return false;
        }
        return type == SentenceType.NEGETIVE;
    }

    private String parseVideoName(Context context)
    {
        ParseVideoNameStep step = new ParseVideoNameStep();
        step.setContext(context);
        step.execute();
        String videoName = (String) context.getValue(Constants.VIDEO_NAME_KEY);
        //String starName = (String) context.getValue(Constants.START_NAME_KEY);
        return videoName;
    }
    
    private String correctionVideoName(Context context)
    {
        CorrectionVideoNameStep step = new CorrectionVideoNameStep(correctionService);
        step.setContext(context);
        step.execute();
        String correctedVideoName = (String) context.getValue(Constants.CORRECTED_VIDEO_NAME_KEY);
        return correctedVideoName;
    }
}
