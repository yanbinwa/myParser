package com.emotibot.parser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emotibot.parser.service.video.VideoService;
import com.emotibot.parser.service.video2.Video2Service;

@RestController
@RequestMapping("/video")
public class VideoController
{
    @Autowired
    VideoService videoService;
    
    @Autowired
    Video2Service video2Service;
    
    @RequestMapping(value="/getVideoName", method = RequestMethod.GET)
    public String getVideoName(@RequestParam(value="text", required=true) String sentence)
    {
        return videoService.getVideoName(sentence);
    }
    
    @RequestMapping(value="/getVideo2Name", method = RequestMethod.POST)
    public String getVideo2Name(@RequestBody String sentence)
    {
        return video2Service.getVideoName(sentence);
    }
}
