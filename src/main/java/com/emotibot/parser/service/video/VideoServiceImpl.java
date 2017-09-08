package com.emotibot.parser.service.video;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.emotibot.correction.service.CorrectionService;
import com.emotibot.middleware.context.Context;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.common.SentenceType;
import com.emotibot.parser.service.video.step.CorrectNameEntitiesStep;
import com.emotibot.parser.service.video.step.ParseSentenceTypeStep;
import com.emotibot.parser.service.video.step.ParseVideoNameStep;
import com.emotibot.parser.service.video.step.ParserNameEntitiesStep;

@Service("videoService")
@EnableAutoConfiguration
@EnableConfigurationProperties
public class VideoServiceImpl implements VideoService
{
    @Autowired
    private CorrectionService correctionService;
    
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Override
    public String getVideoName(String sentence)
    {
        System.out.println("");
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
        List<String> correctedNameEntities = parserNameEntities(context);
        if (correctedNameEntities == null || correctedNameEntities.isEmpty())
        {
            return "找不到相应的电影";
        }
        retStr = correctVideoName(context);
        if (retStr != null)
        {
            return retStr;
        }
        return "找不到相应的电影";
    }
    
    private boolean isNegetiveSentence(Context context)
    {
        context.clearOutputMap();
        ParseSentenceTypeStep step = new ParseSentenceTypeStep(executorService);
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
        ParseVideoNameStep step = new ParseVideoNameStep(executorService);
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
    
    @SuppressWarnings("unchecked")
    private List<String> parserNameEntities(Context context)
    {
        context.clearOutputMap();
        ParserNameEntitiesStep step = new ParserNameEntitiesStep(executorService);
        long startTime = System.currentTimeMillis();
        step.execute(context);
        long endTime = System.currentTimeMillis();
        System.out.println("CorrectionVideoNameStep: [" + (endTime - startTime) + "]ms");
        List<String> pareserNameEntities = (List<String>) context.getValue(Constants.NAME_ENTITY_LIST_KEY);
        System.out.println(pareserNameEntities);
        return pareserNameEntities;
    }
    
    @SuppressWarnings("unchecked")
    private String correctVideoName(Context context)
    {
        context.clearOutputMap();
        CorrectNameEntitiesStep step = new CorrectNameEntitiesStep(correctionService, executorService);
        long startTime = System.currentTimeMillis();
        step.execute(context);
        long endTime = System.currentTimeMillis();
        System.out.println("CorrectNameEntitiesStep: [" + (endTime - startTime) + "]ms");
        List<String> correctVideoNameList = (List<String>) context.getValue(Constants.CORRECTED_VIDEO_NAME_KEY);
        System.out.println(correctVideoNameList);
        if (correctVideoNameList.isEmpty())
        {
            return null;
        }
        else
        {
            return correctVideoNameList.get(0);
        }
    }
}
