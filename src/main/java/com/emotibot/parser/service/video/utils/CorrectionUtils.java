package com.emotibot.parser.service.video.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emotibot.correction.service.CorrectionService;
import com.emotibot.correction.service.CorrectionService2Impl;
import com.emotibot.middleware.conf.ConfigManager;

/**
 * 
 * 这里要与consul绑定，从consul中读取片库，写入到片库文件中，之后重新new一个correctionService
 * 
 * 将consul读取同义词的过程封装
 * 
 * @author emotibot
 *
 */

public class CorrectionUtils
{
    //private static CorrectionService correctionService = null;
    private static CorrectionService correctionService = new CorrectionService2Impl();
    private static String MOVIE_INFO = "专有词库>长虹>影视>电影";
    private static String TV_INFO = "专有词库>长虹>影视>电视剧";
    private static String[] LEVEL_INFOS = {MOVIE_INFO, TV_INFO};
    private static Set<String> level_infos = new HashSet<String>();
    
    static
    {
        for (String level_info : LEVEL_INFOS)
        {
            level_infos.add(level_info);
        }
    }
    
    public static List<String> correction(String nameEntity)
    {
        if (correctionService == null)
        {
            return null;
        }
        return correctionService.correct(nameEntity);
    }
    
    public static List<String> correctWithPinyin(String nameEntity)
    {
        if (correctionService == null)
        {
            return null;
        }
        return correctionService.correctWithPinyin(nameEntity);
    }
    
    public static String getLikelyNameEntity(String nameEntity)
    {
        if (correctionService == null)
        {
            return null;
        }
        return correctionService.getLikelyNameEntity(nameEntity);
    }
    
    public static void updateSynonym(String[] lines)
    {
        String originalFile = ConfigManager.INSTANCE.getPropertyString(com.emotibot.correction.constants.Constants.ORIGIN_FILE_PATH);
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(originalFile);
            for (String line : lines)
            {
                String[] elements = line.trim().split("\t");
                if (elements.length < 2)
                {
                    continue;
                }

                String levelInfo = elements[0];
                if (level_infos.contains(levelInfo))
                {
                    String word = elements[1];
                    fw.write(word + "\r\n");
                }
            }
            
            correctionService = new CorrectionService2Impl();           
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return;
        }
        finally
        {
            try
            {
                fw.close();
            } 
            catch (IOException e)
            {
                
            }
        }
    }
    
    public static void test()
    {
        
    }
}
