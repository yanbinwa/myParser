package com.emotibot.parser.service.video2.output;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.controller.output.AbstractStepOutput;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.service.video.utils.DigitUtil;

public class VideoStepOutput extends AbstractStepOutput
{

    @SuppressWarnings("unchecked")
    @Override
    public String execute(Context context)
    {
        String videoName = (String) context.getValue(Constants.VIDEO_NAME_KEY);
        if (videoName == null)
        {
            List<String> correctVideoNameList = (List<String>) context.getValue(Constants.CORRECTED_VIDEO_NAME_KEY);
            if (correctVideoNameList != null && correctVideoNameList.size() > 0)
            {
                videoName = correctVideoNameList.get(0);
            }
        }
        String starName = (String) context.getValue(Constants.START_NAME_KEY);
        if (videoName != null)
        {
            Integer season = (Integer) context.getValue(Constants.VIDEO_SEASON_KEY);
            Integer episode = (Integer) context.getValue(Constants.VIDEO_EPISODE_KEY);
            if (season != null)
            {
                videoName = videoName + "第" + DigitUtil.parseDigits(season) + "季"; 
            }
            else if (episode != null)
            {
                videoName = videoName + "第" + DigitUtil.parseDigits(episode) + "集"; 
            }
            return videoName;
//            if (starName != null)
//            {
//                return videoName + " " + starName;
//            }
//            else
//            {
//                return videoName;
//            }
        }
        
        Object obj1 = context.getValue(Constants.VIDEO_KEYWORD_N_KEY);
        Object obj2 = context.getValue(Constants.VIDEO_KEYWORD_A_KEY);
        if (starName == null && obj1 == null && obj2 == null)
        {
            return "找不到相应的电影";
        }
        Set<String> output = new HashSet<String>();
        if (!StringUtils.isEmpty(starName))
        {
            output.add(starName);
        }
        if (obj1 != null)
        {
            output.addAll((List<String>)obj1);
        }
        if (obj2 != null)
        {
            output.addAll((List<String>)obj2);
        }
        return output.toString();
    }

}
