package com.emotibot.parser.service.video.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.springframework.util.StringUtils;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correction.service.CorrectionService;
import com.emotibot.correction.utils.EditDistanceUtils;
import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.step.AbstractStep;
import com.emotibot.middleware.task.Task;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.service.video.response.MyResponseType;
import com.emotibot.parser.service.video.response.correction.CorrectedNameEntity;
import com.emotibot.parser.service.video.response.correction.CorrectionResponse;
import com.emotibot.parser.service.video.task.CorrectionTask;

public class CorrectNameEntitiesStep extends AbstractStep
{

    private static int selectNum = 10;
        
    private CorrectionService correctionService;
        
    public CorrectNameEntitiesStep(CorrectionService correctionService)
    {
        this.correctionService = correctionService;
    }
    
    public CorrectNameEntitiesStep(CorrectionService correctionService, ExecutorService executorService)
    {
        super(executorService);
        this.correctionService = correctionService;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void beforeRun(Context context)
    {
        if (correctionService == null)
        {
            return;
        }
        Object obj = context.getValue(Constants.NAME_ENTITY_LIST_KEY);
        if (obj == null || !(obj instanceof List))
        {
            return;
        }
        List<String> nameEntityList = (List<String>) obj;
        for (String nameEntity : nameEntityList)
        {
            Task task = new CorrectionTask(correctionService, nameEntity);
            Task task2 = new CorrectionTask(correctionService, nameEntity, true);
            context.addTask(task);
            context.addTask(task2);
        }
    }

    @Override
    public void afterRun(Context context)
    {
        context.clearTaskList();
        List<Response> responseList = context.getOutputMap().get(MyResponseType.CORRECTION);
        if (responseList == null)
        {
            return;
        }
        List<CorrectedNameEntity> correctedNameEntityList = new ArrayList<CorrectedNameEntity>();
        for (Response response : responseList)
        {
            CorrectionResponse correctionResponse = (CorrectionResponse) response;
            correctedNameEntityList.addAll(correctionResponse.getCorrectedNameEntityList());
        }
        String sentence = adjustSentence(context);
        correctedNameEntityList = sortList(correctedNameEntityList, sentence);
        List<String> result = new ArrayList<String>();
        for (CorrectedNameEntity entity : correctedNameEntityList)
        {
            //这里如果匹配度不够的话，就认为没有找到
            int entityLen = entity.getNameEntity().length();
            int sentenceLen = sentence.length();
            int diffLen = Math.abs(sentenceLen - entityLen);
            double errorDistance = 0.0;
            //只有当entity名称小于sentence时，才处理
            if (entityLen <= sentenceLen)
            {
                errorDistance = entity.getEditDistance() - diffLen;
            }
            else
            {
                errorDistance = entity.getEditDistance();
            }
            //TODO
            if (errorDistance / (double) entityLen < 0.4)
            {
                result.add(entity.getNameEntity());
            }
        }
        context.setValue(Constants.CORRECTED_VIDEO_NAME_KEY, result);
    }
    
    
    //TODO 排重
    private List<CorrectedNameEntity> sortList(List<CorrectedNameEntity> correctedNameEntityList, String sentence)
    {
        if (correctedNameEntityList == null || correctedNameEntityList.isEmpty())
        {
            return correctedNameEntityList;
        }
        //这里要进行去重，这里的去重是保证CorrectedNameEntity中名称一致，取distance小的CorrectedNameEntity
        Map<String, CorrectedNameEntity> tmpMap = new HashMap<String, CorrectedNameEntity>();
        for (CorrectedNameEntity entity : correctedNameEntityList)
        {
            String nameEntity = entity.getNameEntity();
            CorrectedNameEntity correctedNameEntity = tmpMap.get(nameEntity);
            if (correctedNameEntity == null)
            {
                tmpMap.put(nameEntity, entity);
                continue;
            }
            if (entity.getEditDistance() < correctedNameEntity.getEditDistance())
            {
                tmpMap.put(nameEntity, entity);
            }
        }
        correctedNameEntityList = new ArrayList<CorrectedNameEntity>(tmpMap.values());
        SentenceElement element = new SentenceElement(sentence);

        //这里要计算的时该CorrectedNameEntity与原始句子的编辑长度
        //TODO 如何简化
        for (CorrectedNameEntity entity : correctedNameEntityList)
        {
            SentenceElement element1 = new SentenceElement(entity.getNameEntity());
            double editDistance1 = EditDistanceUtils.getEditDistance(element1, element);
            entity.setOriginNameEntity(sentence);
            entity.setEditDistance(editDistance1);
        }
        
        Collections.sort(correctedNameEntityList, new Comparator<CorrectedNameEntity>()
        {
            @Override
            public int compare(CorrectedNameEntity o1, CorrectedNameEntity o2)
            {
                if (o1.getEditDistance() > o2.getEditDistance())
                {
                    return 1;
                }
                else if (o1.getEditDistance() < o2.getEditDistance())
                {
                    return -1;
                }
                else
                {
                    if (o1.getNameEntity().length() < o2.getNameEntity().length())
                    {
                        return 1;
                    }
                    else if (o1.getNameEntity().length() > o2.getNameEntity().length())
                    {
                        return -1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            }
            
        });
        
        if (correctedNameEntityList.size() > selectNum)
        {
            return correctedNameEntityList.subList(0, selectNum);
        }
        else
        {
            return correctedNameEntityList;
        }
    }
    
    @SuppressWarnings("unchecked")
    private String adjustSentence(Context context)
    {
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        if (sentence == null)
        {
            return null;
        }
        Object obj = context.getValue(Constants.VIDEO_REWRITE_TOKENS_KEY);
        List<String> matchTokens = null;
        if (obj == null)
        {
            matchTokens = new ArrayList<String>();
        }
        else
        {
            matchTokens = (List<String>) obj;
        }
        
        String starName = (String) context.getValue(Constants.START_NAME_KEY);
        if (matchTokens.isEmpty() && starName == null)
        {
            return sentence;
        }
        for (String token : matchTokens)
        {
            sentence = sentence.replaceFirst(token, "");
        }
        if (!StringUtils.isEmpty(starName))
        {
            sentence = sentence.replaceFirst(starName, "");
        }
        return sentence;
    }
}
