package com.emotibot.parser.service.video.step;


import java.util.List;

import org.springframework.util.StringUtils;

import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.response.ResponseType;
import com.emotibot.middleware.response.commonParser.CommonParserResponse;
import com.emotibot.middleware.response.nlu.NLUResponse;
import com.emotibot.middleware.step.AbstractStep;
import com.emotibot.middleware.task.RestCallTask;
import com.emotibot.middleware.task.restCallTask.CommonParserTask;
import com.emotibot.middleware.task.restCallTask.NLUTask;
import com.emotibot.middleware.utils.UrlUtils;
import com.emotibot.parser.common.Constants;

public class ParseVideoNameStep extends AbstractStep
{
    private String videoName = null;
    private String starName = null;
    
    public ParseVideoNameStep()
    {
    }
    
    @Override
    public void beforeRun()
    {
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        
        //CommonParser Task
        RestCallTask task = new CommonParserTask();
        String text = "?tags=movie_name_module,teleplay_name_module,star_name_module&text=" + sentence;
        String hostname = ConfigManager.INSTANCE.getPropertyString(Constants.TARGET_HOST);
        String port = ConfigManager.INSTANCE.getPropertyString(Constants.COMMON_PARSER_PORT);
        String endpoint = ConfigManager.INSTANCE.getPropertyString(Constants.COMMON_PARSER_ENDPOINT);
        String url = UrlUtils.getUrl(hostname, port, endpoint, text);
        HttpRequest request = new HttpRequest(url, null, HttpRequestType.GET);
        task.setRequest(request);
        this.addTask(task);
        
        //NLU Task
        task = new NLUTask();
        text = "?f=namedEntities,namedEntitiesMT&nerTypes=MOVIE&q=" + sentence;
        hostname = ConfigManager.INSTANCE.getPropertyString(Constants.TARGET_HOST);
        port = ConfigManager.INSTANCE.getPropertyString(Constants.NLU_PORT);
        endpoint = ConfigManager.INSTANCE.getPropertyString(Constants.NLU_ENDPOINT);
        url = UrlUtils.getUrl(hostname, port, endpoint, text);
        request = new HttpRequest(url, null, HttpRequestType.GET);
        task.setRequest(request);
        this.addTask(task);
    }

    /**
     * 这里的逻辑是先看commonParser给出的结果，找出人名和电影或者电视剧
     * 如果都找到，就直接返回，否则
     * 
     * 通过namedEntities找人名
     * 通过namedEntitiesMT找电影名称
     */
    @Override
    public void afterRun()
    {
        parserCommonParserResponse();
        if (!StringUtils.isEmpty(videoName) && !StringUtils.isEmpty(starName))
        {
            return;
        }
        parserNLUResponse();
        context.setValue(Constants.VIDEO_NAME_KEY, videoName);
        context.setValue(Constants.START_NAME_KEY, starName);
    }
    
    private void parserCommonParserResponse()
    {
        List<Response> responseList = this.outputMap.get(ResponseType.COMMON_PARSER);
        if (responseList == null || responseList.isEmpty())
        {
            return;
        }
        CommonParserResponse response = (CommonParserResponse) responseList.get(0);
        if (response == null)
        {
            return;
        }
        videoName = response.getMovie();
        if (!StringUtils.isEmpty(videoName))
        {
            return;
        }
        videoName = response.getTeleplay();
    }
    
    private void parserNLUResponse()
    {
        List<Response> responseList = this.outputMap.get(ResponseType.NLU);
        if (responseList == null || responseList.isEmpty())
        {
            return;
        }
        NLUResponse response = (NLUResponse) responseList.get(0);
        
        if (StringUtils.isEmpty(videoName))
        {
            videoName = response.getMovie();
        }
        if (StringUtils.isEmpty(starName))
        {
            starName = response.getStar();
        }
    }
}
