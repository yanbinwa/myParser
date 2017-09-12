package com.emotibot.parser.service.video;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.emotibot.correction.service.CorrectionService;
import com.emotibot.middleware.context.Context;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.common.SentenceType;
import com.emotibot.parser.service.video.step.CorrectNameEntitiesStep;
import com.emotibot.parser.service.video.step.ParseSentenceTypeStep;
import com.emotibot.parser.service.video.step.ParseVideoNameStep;
import com.emotibot.parser.service.video.step.ParserNameEntitiesStep;
import com.emotibot.parser.service.video.utils.DigitUtil;

@Service("videoService")
@EnableAutoConfiguration
@EnableConfigurationProperties
public class VideoServiceImpl implements VideoService
{
    @Autowired
    private CorrectionService correctionService;
    
    private ExecutorService executorService = Executors.newFixedThreadPool(50);
    
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
//        boolean ret = isNegetiveSentence(context);
//        if(ret)
//        {
//            return "那您想看什么呀~";
//        }
        
        String retStr = parseVideoName(context);
        if (retStr != null)
        {
            return output(context);
        }
        List<String> correctedNameEntities = parserNameEntities(context);
        if (correctedNameEntities == null || correctedNameEntities.isEmpty())
        {
            return output(context);
        }
        retStr = correctVideoName(context);
        return output(context);
    }
    
    @SuppressWarnings("unused")
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
        if (correctVideoNameList == null)
        {
            return null;
        }
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
    
    @SuppressWarnings("unchecked")
    private String output(Context context)
    {
        String videoName = (String) context.getValue(Constants.VIDEO_NAME_KEY);
        if (videoName == null)
        {
            List<String> correctVideoNameList = (List<String>) context.getValue(Constants.CORRECTED_VIDEO_NAME_KEY);
            if (correctVideoNameList != null && correctVideoNameList.size() > 0)
            {
                videoName = correctVideoNameList.get(0);
            }
        }
        String starName = (String) context.getValue(Constants.START_NAME_KEY);
        if (videoName != null)
        {
            Integer season = (Integer) context.getValue(Constants.VIDEO_SEASON_KEY);
            Integer episode = (Integer) context.getValue(Constants.VIDEO_EPISODE_KEY);
            if (season != null)
            {
                videoName = videoName + "第" + DigitUtil.parseDigits(season) + "季"; 
            }
            else if (episode != null)
            {
                videoName = videoName + "第" + DigitUtil.parseDigits(episode) + "集"; 
            }
            return videoName;
//            if (starName != null)
//            {
//                return videoName + " " + starName;
//            }
//            else
//            {
//                return videoName;
//            }
        }
        
        Object obj1 = context.getValue(Constants.VIDEO_KEYWORD_N_KEY);
        Object obj2 = context.getValue(Constants.VIDEO_KEYWORD_A_KEY);
        if (starName == null && obj1 == null && obj2 == null)
        {
            return "找不到相应的电影";
        }
        Set<String> output = new HashSet<String>();
        if (!StringUtils.isEmpty(starName))
        {
            output.add(starName);
        }
        if (obj1 != null)
        {
            output.addAll((List<String>)obj1);
        }
        if (obj2 != null)
        {
            output.addAll((List<String>)obj2);
        }
        return output.toString();
    }
}
