package com.emotibot.parser.service.video.response.correction;

public class CorrectedNameEntity
{
    private String nameEntity;
    
    private String originNameEntity;
    
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
}
