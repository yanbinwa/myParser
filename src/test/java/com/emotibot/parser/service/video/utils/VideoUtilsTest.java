package com.emotibot.parser.service.video.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class VideoUtilsTest
{

    @Test
    public void test()
    {
        List<String> matchTokens = new ArrayList<String>();
        
        String text = "一下伪装者最后一集";
        
        int ret = VideoUtils.getVideoPlayIndex(text, matchTokens);
        
        System.out.println("index is: " + ret);
        System.out.println("matchTokens is: " + matchTokens);
    }

}
