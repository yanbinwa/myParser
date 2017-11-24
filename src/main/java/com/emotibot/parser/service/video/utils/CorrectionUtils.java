package com.emotibot.parser.service.video.utils;

import java.util.List;

import com.emotibot.correction.service.CorrectionService;
import com.emotibot.correction.service.CorrectionService2Impl;

public class CorrectionUtils
{
    private static CorrectionService correctionService = new CorrectionService2Impl();
    
    public static List<String> correction(String nameEntity)
    {
        return correctionService.correct(nameEntity);
    }
    
    public static List<String> correctWithPinyin(String nameEntity)
    {
        return correctionService.correctWithPinyin(nameEntity);
    }
    
    public static void test()
    {
        
    }
}
