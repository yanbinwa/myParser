package com.emotibot.parser.service.video.response.correction;

import java.util.List;

import com.emotibot.middleware.response.AbstractResponse;
import com.emotibot.parser.service.video.response.MyResponseType;

public class CorrectionResponse extends AbstractResponse
{

    private List<CorrectedNameEntity> correctedNameEntityList = null;
    
    public CorrectionResponse(List<CorrectedNameEntity> correctedNameEntityList)
    {
        super(MyResponseType.CORRECTION);
        this.correctedNameEntityList = correctedNameEntityList;
    }

    public List<CorrectedNameEntity> getCorrectedNameEntityList()
    {
        return this.correctedNameEntityList;
    }
}
