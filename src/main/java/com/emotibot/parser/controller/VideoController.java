package com.emotibot.parser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.emotibot.parser.service.video.VideoService;

@RestController
@RequestMapping("/video")
public class VideoController
{
    @Autowired
    VideoService videoService;
    
    @RequestMapping(value="/getVideoName", method = RequestMethod.POST)
    public String getVideoName(@RequestBody String sentence)
    {
        return videoService.getVideoName(sentence);
    }
}
