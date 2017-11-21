package com.emotibot.parser.service.video.response.correction;

import com.emotibot.middleware.utils.JsonUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CorrectedNameEntity
{
    @SerializedName("nameEntity")
    @Expose
    private String nameEntity;
    
    @SerializedName("originNameEntity")
    @Expose
    private String originNameEntity;
    
    @SerializedName("editDistance")
    @Expose
    private double editDistance = -1;
        
    public CorrectedNameEntity()
    {
        
    }
    
    public CorrectedNameEntity(String nameEntity, String originNameEntity, double editDistance)
    {
        this.nameEntity = nameEntity;
        this.originNameEntity = originNameEntity;
        this.editDistance = editDistance;
    }
    
    public void setNameEntity(String nameEntity)
    {
        this.nameEntity = nameEntity;
    }
    
    public String getNameEntity()
    {
        return nameEntity;
    }
    
    public void setOriginNameEntity(String originNameEntity)
    {
        this.originNameEntity = originNameEntity;
    }
    
    public String getOriginNameEntity()
    {
        return originNameEntity;
    }
    
    public void setEditDistance(double editDistance)
    {
        this.editDistance = editDistance;
    }
    
    public double getEditDistance()
    {
        return editDistance;
    }
    
    @Override
    public String toString()
    {
        return JsonUtils.getJsonStr(this);
    }
}
