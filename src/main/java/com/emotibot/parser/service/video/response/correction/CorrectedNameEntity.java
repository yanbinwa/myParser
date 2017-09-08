package com.emotibot.parser.service.video.response.correction;

public class CorrectedNameEntity
{
    private String nameEntity;
    
    private double editDistance = -1;
    
    public CorrectedNameEntity()
    {
        
    }
    
    public CorrectedNameEntity(String nameEntity, double editDistance)
    {
        this.nameEntity = nameEntity;
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
    
    public void setEditDistance(double editDistance)
    {
        this.editDistance = editDistance;
    }
    
    public double getEditDistance()
    {
        return editDistance;
    }
}
