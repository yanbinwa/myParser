package com.emotibot.parser.service.video.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correction.utils.EditDistanceUtils;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.task.AbstractTask;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.service.video.response.correction.CorrectedNameEntity;
import com.emotibot.parser.service.video.response.correction.CorrectionResponse;
import com.emotibot.parser.service.video.utils.CorrectionUtils;

public class CorrectionTask extends AbstractTask
{
    private static Logger logger = Logger.getLogger(CorrectionTask.class); 
    private String nameEntity;
    private boolean isByPinyin = false;
    
    public CorrectionTask(String nameEntity)
    {
        this.nameEntity = nameEntity;
    }
    
    public CorrectionTask(String nameEntity, boolean isByPinyin)
    {
        this.nameEntity = nameEntity;
        this.isByPinyin = isByPinyin;
    }
    
    @Override
    public Response call() throws Exception
    {
        List<CorrectedNameEntity> correctedNameEntityList = new ArrayList<CorrectedNameEntity>();
        List<String> candidateList = null;
        if (!isByPinyin)
        {
            candidateList = CorrectionUtils.correction(nameEntity);
        }
        else
        {
            candidateList = CorrectionUtils.correctWithPinyin(nameEntity);
        }
        if (candidateList == null)
        {
            logger.error("can not get candidate from " + nameEntity);
            return new CorrectionResponse(correctedNameEntityList);
        }
        SentenceElement element = new SentenceElement(nameEntity);
        for (String candidate : candidateList)
        {
            SentenceElement candidateElement = new SentenceElement(candidate);
            double distance = getAdjustDistance(element, candidateElement);
            CorrectedNameEntity correctedNameEntity = new CorrectedNameEntity(candidate, nameEntity, distance);
            correctedNameEntityList.add(correctedNameEntity);
        }
        return new CorrectionResponse(correctedNameEntityList);
    }

    private double getAdjustDistance(SentenceElement element, SentenceElement candidateElement)
    {
        double distance = EditDistanceUtils.getEditDistance(element, candidateElement);
        //TODO
        int minLenght = Math.min(element.getSentence().length(), candidateElement.getSentence().length());
        int maxLenght = Math.max(element.getSentence().length(), candidateElement.getSentence().length());
        int diffLength = maxLenght - minLenght;
        //如果匹配到的长度只占一小部分，distance就要比较大
        double rate = (maxLenght - distance) / (double) maxLenght;
        if (rate < Constants.DIFF_LEN_RATE_LEVEL_1)
        {
            distance -= diffLength * Constants.DIFF_LEN_ADJUST_RATE_1;
        }
        else if (rate < Constants.DIFF_LEN_RATE_LEVEL_2)
        {
            distance -= diffLength * Constants.DIFF_LEN_ADJUST_RATE_2;
        }
        else if (rate < Constants.DIFF_LEN_RATE_LEVEL_3)
        {
            distance -= diffLength * Constants.DIFF_LEN_ADJUST_RATE_3;
        }
        else if (rate < Constants.DIFF_LEN_RATE_LEVEL_4)
        {
            distance -= diffLength * Constants.DIFF_LEN_ADJUST_RATE_4;
        }
        else
        {
            distance -= diffLength;
        }
        
//        rate = distance / (double) minLenght;
//        if (rate > 1)
//        {
//            distance += rate * 10;
//        }
//        else if (rate > 0.75)
//        {
//            distance += rate * 5;
//        }
//        else if (rate > 0.5)
//        {
//            distance += rate * 2;
//        }
//        else 
//        {
//            distance += rate;
//        }
        //TODO 根据整体长度调整distance
        if (candidateElement.getSentence().length() > Constants.DIFF_LEN_THRESHOLD)
        {
            distance -= (candidateElement.getSentence().length() - Constants.DIFF_LEN_THRESHOLD) * Constants.DIFF_LEN_THRESHOLD_RATE;
        }
        return distance;
    }
}
