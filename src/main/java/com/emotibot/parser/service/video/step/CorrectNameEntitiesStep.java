package com.emotibot.parser.service.video.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.emotibot.correction.service.CorrectionService;
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

    private static int selectNum = 2;
        
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
        List<CorrectedNameEntity> correctedNameEntityList = new ArrayList<CorrectedNameEntity>();
        for (Response response : responseList)
        {
            CorrectionResponse correctionResponse = (CorrectionResponse) response;
            correctedNameEntityList.addAll(correctionResponse.getCorrectedNameEntityList());
        }
        correctedNameEntityList = sortList(correctedNameEntityList);
        List<String> result = new ArrayList<String>();
        for (CorrectedNameEntity entity : correctedNameEntityList)
        {
            if (entity.getEditDistance() < (double) entity.getNameEntity().length() / 2)
            {
                result.add(entity.getNameEntity());
            }
        }
        context.setValue(Constants.CORRECTED_VIDEO_NAME_KEY, result);
    }
    
    private List<CorrectedNameEntity> sortList(List<CorrectedNameEntity> correctedNameEntityList)
    {
        if (correctedNameEntityList == null || correctedNameEntityList.isEmpty())
        {
            return correctedNameEntityList;
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
                
                if (o1.getNameEntity().length() > o2.getNameEntity().length())
                {
                    return 1;
                }
                else if (o1.getNameEntity().length() < o2.getNameEntity().length())
                {
                    return -1;
                }
                return 0;
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
}
