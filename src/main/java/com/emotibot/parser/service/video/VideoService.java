package com.emotibot.parser.service.video;

public interface VideoService
{
    /**
     * 通过用户的话来解析出电影名称
     * 
     * @param sentence
     * @return
     */
    public String getVideoName(String sentence);
}
