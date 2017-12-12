package com.emotibot.parser.service.video.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.emotibot.correction.element.SentenceElement;
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
import com.emotibot.parser.service.video.utils.CorrectionUtils;


/**
 * 
 * 这里考虑三个维度，进行排序，一个是编辑距离，一个是全文匹配
 * 
 * @author emotibot
 *
 */
public class CorrectNameEntitiesStep extends AbstractStep
{
    private static final Logger logger = Logger.getLogger(CorrectNameEntitiesStep.class);
    private static int selectNum = Constants.CORRECT_SELECT_NUM;
    
    public CorrectNameEntitiesStep()
    {
        
    }
    
    public CorrectNameEntitiesStep(ExecutorService executorService)
    {
        super(executorService);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void beforeRun(Context context)
    {
        Object obj = context.getValue(Constants.NAME_ENTITY_LIST_KEY);
        if (obj == null || !(obj instanceof List))
        {
            List<String> objTmp = new ArrayList<String>();
            objTmp.add((String) context.getValue(Constants.SENTENCE_KEY));
            obj = objTmp;
        }
        List<String> nameEntityList = (List<String>) obj;
        System.out.println(nameEntityList);
        for (String nameEntity : nameEntityList)
        {
            String likelyNameEntity = CorrectionUtils.getLikelyNameEntity(nameEntity);
            Task task = new CorrectionTask(likelyNameEntity);
            Task task2 = new CorrectionTask(likelyNameEntity, true);
            this.addTask(context, task);
            this.addTask(context, task2);
        }
    }

    @Override
    public void afterRun(Context context)
    {
        long start = System.currentTimeMillis();
        List<Response> responseList = this.getOutputMap(context).get(MyResponseType.CORRECTION);
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
        logger.debug(correctedNameEntityList);
        List<String> result = selectFinalResult(correctedNameEntityList);
        
        if(result.isEmpty() && !correctedNameEntityList.isEmpty())
        {
            result.add(correctedNameEntityList.get(0).getNameEntity());
        }
        context.setValue(Constants.CORRECTED_VIDEO_NAME_KEY, result);
        long end = System.currentTimeMillis();
        System.out.println("cost: [" + (end - start) + "]ms");
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
            //TODO  这里不能简单的比较编辑距离，而是要比较连续长度下的编辑距离
            
            double editDistance1 = calculateDistance(element1, element);
            double editDistance2 = calculateDistanceWithOutOrder(element1, element);
            double editDistance3 = entity.getEditDistance();
            double editDistance = editDistance1 * Constants.EDIT_DISTANCE_1_RATE + 
                    editDistance2 * Constants.EDIT_DISTANCE_2_RATE + 
                    editDistance3 * Constants.EDIT_DISTANCE_3_RATE;

            entity.setOriginNameEntity(sentence);
            entity.setEditDistance(editDistance);
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
    
    /**
     * 逻辑是连续的比较，在计算编辑距离时，还要对比两个字段中是否有倒序的情况，所以需要判断打乱顺序后是否也可以match
     * 应该还有一种case是我要看初恋小事 实际是初恋这件小事，这里要参照之前的distance计算
     * 
     */
    @SuppressWarnings("unused")
    private double calculateDistance(SentenceElement element, SentenceElement targetElement)
    {
        int len = element.getLength();
        int targetLen = targetElement.getLength();
        if (len >= targetLen)
        {
            return EditDistanceUtils.getEditDistance(element, targetElement);
        }
        int diffLen = targetLen - len;
        double distance = Double.MAX_VALUE;
        SentenceElement choseElement = null; 
        for (int i = 0; i <= diffLen; i ++)
        {
            SentenceElement element1 = targetElement.subSentenceElement(i, i + len);
            double distanceTmp =  EditDistanceUtils.getEditDistance(element, element1);
            if (distanceTmp < distance)
            {
                distance = distanceTmp;
                choseElement = element1;
            }
        }
        distance += diffLen * Constants.CALCULATE_DIFF_RATE;
        return distance;
    }
    
    /**
     * 这里主要是为了辅助，所以参数不应过高
     * 
     * @param element
     * @param targetElement
     * @return
     */
    @SuppressWarnings("unused")
    private double calculateDistanceWithOutOrder(SentenceElement element, SentenceElement targetElement)
    {
        int len = element.getLength();
        int targetLen = targetElement.getLength();
        if (len >= targetLen)
        {
            return EditDistanceUtils.getEditDistanceWithoutOrder(element, targetElement) / (element.getLength() + targetElement.getLength());
        }
        int diffLen = targetLen - len;
        double distance = Double.MAX_VALUE;
        SentenceElement choseElement = null; 
        for (int i = 0; i <= diffLen; i ++)
        {
            SentenceElement element1 = targetElement.subSentenceElement(i, i + len);
            double distanceTmp =  EditDistanceUtils.getEditDistanceWithoutOrder(element, element1) / (element.getLength() + element1.getLength());
            if (distanceTmp < distance)
            {
                distance = distanceTmp;
                choseElement = element1;
            }
        }
        distance += diffLen * Constants.CALCULATE_DIFF_WITHOUT_ORDER_RATE;
        return distance;
    }
    
    @SuppressWarnings("unused")
    private List<String> selectFinalResult(List<CorrectedNameEntity> correctedNameEntityList)
    {
        List<String> result = new ArrayList<String>();
        if (correctedNameEntityList == null || correctedNameEntityList.isEmpty())
        {
            return result;
        }
        result.add(correctedNameEntityList.get(0).getNameEntity());
        double standardDistance = correctedNameEntityList.get(0).getEditDistance();
        double distanceDiff = Constants.MAX_DIFF_LEN / 2;
        for (int i = 1; i < correctedNameEntityList.size(); i ++)
        {
            CorrectedNameEntity lastEntity = correctedNameEntityList.get(i - 1);
            CorrectedNameEntity currentEntity = correctedNameEntityList.get(i);
            if (currentEntity.getEditDistance() - standardDistance > Constants.MAX_DIFF_LEN)
            {
                break;
            }
            double distanceDiffTmp = currentEntity.getEditDistance() - lastEntity.getEditDistance();
            if (distanceDiffTmp > Constants.MAX_DIFF_RATE * distanceDiff) 
            {
                break;
            }
            result.add(currentEntity.getNameEntity());
            if (distanceDiffTmp < distanceDiff)
            {
                distanceDiff = distanceDiffTmp;
            }
        }
        return result;
    }
    
    @SuppressWarnings("unused")
    private List<String> selectFinalResult1(List<CorrectedNameEntity> correctedNameEntityList, String targetSentence)
    {
        List<String> result = new ArrayList<String>();
        for (CorrectedNameEntity entity : correctedNameEntityList)
        {
            //这里如果匹配度不够的话，就认为没有找到
            int entityLen = entity.getNameEntity().length();
            int sentenceLen = targetSentence.length();
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
            if (errorDistance / (double) entityLen < Constants.CORRECT_CHOOSE_RATE)
            {
                result.add(entity.getNameEntity());
            }
        }
        return result;
    }
}
