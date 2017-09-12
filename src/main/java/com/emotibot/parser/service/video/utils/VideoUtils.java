package com.emotibot.parser.service.video.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoUtils
{
    private static final String PART_NUMBER = "((\\d+)|([零,一,二,三,四,五,六,七,八,九,十,百])+)";
    private static String PATTERN_EXCLUDE = "([开,播,放])?(一下)";
    private static String PATTERN_BACKWORD = "(最后|倒数)";
    private static String PATTERN_BACKWORD_1 = "(%s)(第)?" + PART_NUMBER + "(集|部|季)";
    private static String PATTERN_CATCH = "(第)" + PART_NUMBER + "(集|部|季)";
    //private static String PATTERN_CATCH_1 = PART_NUMBER + "(集|部|季)";
    
    public static int getVideoPlayIndex(String text, List<String> matchToken)
    {
        if (text == null || text.trim().isEmpty())
        {
            return Integer.MIN_VALUE;
        }
        String textTmp = adjustText(text, matchToken);
        //先通过关键字“最后or倒数”来判断是否从后播放
        int ret = Integer.MIN_VALUE;
        String matchStr = matched(PATTERN_BACKWORD, textTmp);
        if (matchStr != null)
        {
            ret = getPlayIndexFromBack(matchStr, textTmp, matchToken);
        }
        else
        {
            ret = getPlayIndexFromBegin(textTmp, matchToken);
        }
        return ret;
    }
    
    public static int getVideoPlayIndex(String text)
    {
        return getVideoPlayIndex(text, new ArrayList<String>());
    }
    
    /**
     * 要排除放一下、播一下这类的数字
     * 
     * @param text
     * @return
     */
    private static String adjustText(String text, List<String> matchToken)
    {
        if (text == null)
        {
            return null;
        }
        String match = matched(PATTERN_EXCLUDE, text);
        if (match != null)
        {
            text = text.replaceAll(match, "");
            matchToken.add(match);
        }
        return text;
    }
    
    private static String matched(String patternStr, String text)
    {
        if (patternStr == null || text == null)
        {
            return null;
        }
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find())
        {
            return null;
        }
        return matcher.group(0);
    }
    
    private static int getPlayIndexFromBack(String backword, String text, List<String> matchToken)
    {
        String patternStr = String.format(PATTERN_BACKWORD_1, backword);
        //如果提取到最后或倒数，有限读取其后面的数字作为结果
        String match = matched(patternStr, text);
        if (match != null)
        {
            matchToken.add(match);
            return getNumFromString(match) * -1;
        }
        else
        {
            return getPlayIndexFromBegin(text, matchToken);
        }
    }
    
    private static int getPlayIndexFromBegin(String text, List<String> matchToken)
    {
        int playIndex = getPlayIndexGenerate(text, matchToken);
        if (playIndex != Integer.MIN_VALUE)
        {
            return playIndex;
        }
        else
        {
            return Integer.MIN_VALUE;
        }
    }
    
    private static int getPlayIndexGenerate(String text, List<String> matchToken)
    {
        //优先匹配含有“第N个”或“第N部”的字段
        String match = matched(PATTERN_CATCH, text);
        if (match == null)
        {
            //match = matched(PATTERN_CATCH_1, text);
            return Integer.MIN_VALUE;
        }
        else
        {
            matchToken.add(match);
            return getNumFromString(match);
        }
    }
    
    private static int getNumFromString(String text, List<String> matchToken)
    {
        Pattern pattern = Pattern.compile(PART_NUMBER);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
        {
            String numStr = matcher.group(0);
            matchToken.add(numStr);
            return DigitUtil.parseDigits(numStr);
        }
        else
        {
            return Integer.MIN_VALUE;
        }
    }
    
    private static int getNumFromString(String text)
    {
        return getNumFromString(text, new ArrayList<String>());
    }
}
