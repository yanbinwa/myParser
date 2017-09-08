package com.emotibot.parser.service.video.task;

import java.util.ArrayList;
import java.util.List;

import com.emotibot.correction.element.SentenceElement;
import com.emotibot.correction.service.CorrectionService;
import com.emotibot.correction.utils.EditDistanceUtils;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.task.AbstractTask;
import com.emotibot.parser.service.video.response.correction.CorrectedNameEntity;
import com.emotibot.parser.service.video.response.correction.CorrectionResponse;

public class CorrectionTask extends AbstractTask
{

    private CorrectionService correctionService;
    private String nameEntity;
    private boolean isByPinyin = false;
    
    public CorrectionTask(CorrectionService correctionService, String nameEntity)
    {
        this.correctionService = correctionService;
        this.nameEntity = nameEntity;
    }
    
    public CorrectionTask(CorrectionService correctionService, String nameEntity, boolean isByPinyin)
    {
        this.correctionService = correctionService;
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
            candidateList = correctionService.correct(nameEntity);
        }
        else
        {
            candidateList = correctionService.correctWithPinyin(nameEntity);
        }
        SentenceElement element = new SentenceElement(nameEntity);
        for (String candidate : candidateList)
        {
            SentenceElement candidateElement = new SentenceElement(candidate);
            double distance = getAdjustDistance(element, candidateElement);
            CorrectedNameEntity correctedNameEntity = new CorrectedNameEntity(candidate, distance);
            correctedNameEntityList.add(correctedNameEntity);
        }
        return new CorrectionResponse(correctedNameEntityList);
    }

    private double getAdjustDistance(SentenceElement element, SentenceElement candidateElement)
    {
        double distance = EditDistanceUtils.getEditDistance(element, candidateElement);
        int minLenght = Math.min(element.getSentence().length(), candidateElement.getSentence().length());
        double rate = distance / (double) minLenght;
        if (rate > 1)
        {
            distance += rate * 10;
        }
        else if (rate > 0.75)
        {
            distance += rate * 5;
        }
        else if (rate > 0.5)
        {
            distance += rate * 2;
        }
        else 
        {
            distance += rate;
        }
        return distance;
    }
    
}
