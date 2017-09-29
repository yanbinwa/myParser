package com.emotibot.parser.service.video.step;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.util.StringUtils;

import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.CommonResponseType;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.response.commonParser.CommonParserResponse;
import com.emotibot.middleware.response.nlu.NLUResponse;
import com.emotibot.middleware.step.AbstractStep;
import com.emotibot.middleware.task.RestCallTask;
import com.emotibot.middleware.task.restCallTask.CommonParserTask;
import com.emotibot.middleware.task.restCallTask.NLUTask;
import com.emotibot.middleware.utils.UrlUtils;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.service.video.utils.VideoUtils;

public class ParseVideoNameStep extends AbstractStep
{   
    public ParseVideoNameStep()
    {
    }
    
    public ParseVideoNameStep(ExecutorService executorService)
    {
        super(executorService);
    }
    
    @Override
    public void beforeRun(Context context)
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
        task.setUniqId(context.getUniqId());
        context.addTask(task);
        
        //NLU Task
        task = new NLUTask();
        text = "?f=namedEntities,namedEntitiesMT,keyword,srl&nerTypes=MOVIE&q=" + sentence;
        hostname = ConfigManager.INSTANCE.getPropertyString(Constants.TARGET_HOST);
        port = ConfigManager.INSTANCE.getPropertyString(Constants.NLU_PORT);
        endpoint = ConfigManager.INSTANCE.getPropertyString(Constants.NLU_ENDPOINT);
        url = UrlUtils.getUrl(hostname, port, endpoint, text);
        request = new HttpRequest(url, null, HttpRequestType.GET);
        task.setRequest(request);
        task.setUniqId(context.getUniqId());
        context.addTask(task);
    }

    /**
     * 这里的逻辑是先看commonParser给出的结果，找出人名和电影或者电视剧
     * 如果都找到，就直接返回，否则
     * 
     * 通过namedEntities找人名
     * 通过namedEntitiesMT找电影名称
     * 
     * 这里就要判断是否找到了合适的内容，如果找到了，就看其后面是不是接着数字
     */
    @Override
    public void afterRun(Context context)
    {
        context.clearTaskList();
        parserVideoBaseInfo(context);
        parserCommonParserResponse(context);
//        if (context.isContainsKey(Constants.VIDEO_NAME_KEY) || context.isContainsKey(Constants.START_NAME_KEY))
//        {
//            return;
//        }
//        parserNLUResponse(context);
    }
    
    private void parserVideoBaseInfo(Context context)
    {
        List<Response> responseList = getOutputMap(context).get(CommonResponseType.NLU);
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        if (responseList == null || responseList.isEmpty())
        {
            return;
        }
        //第一步，读取出第几集，第几部的信息
        NLUResponse response = (NLUResponse)responseList.get(0);
        context.setValue(Constants.NLU_CACHE_KEY, response);
        parserEpisodeOrSeason(context, sentence);
        
        //第二步，读取出keyword，包括名词和形容词，例如“播一部好看的电影”，将其在特定的目录中查找
        List<String> keywordN = response.getValidSegmentForN();
        List<String> keywordA = response.getValidSegmentForA();
        context.setValue(Constants.VIDEO_KEYWORD_N_KEY, keywordN);
        context.setValue(Constants.VIDEO_KEYWORD_A_KEY, keywordA);
    }
    
    private void parserEpisodeOrSeason(Context context, String nameEntity)
    {
        List<String> matchTokens = new ArrayList<String>();
        int index = VideoUtils.getVideoPlayIndex(nameEntity, matchTokens);
        if (index != Integer.MIN_VALUE)
        {
            boolean isEpisode = true;
            for (String token : matchTokens)
            {
                if (token.contains("季") || token.contains("部"))
                {
                    isEpisode = false;
                }
            }
            if (isEpisode)
            {
                context.setValue(Constants.VIDEO_EPISODE_KEY, index);
            }
            else
            {
                context.setValue(Constants.VIDEO_SEASON_KEY, index);
            }
            context.setValue(Constants.VIDEO_REWRITE_TOKENS_KEY, matchTokens);
        }
    }
    
    private void parserCommonParserResponse(Context context)
    {
        List<Response> responseList = getOutputMap(context).get(CommonResponseType.COMMON_PARSER);
        if (responseList == null || responseList.isEmpty())
        {
            return;
        }
        CommonParserResponse response = (CommonParserResponse) responseList.get(0);
        if (response == null)
        {
            return;
        }
//        if (!StringUtils.isEmpty(response.getMovie()))
//        {
//            context.setValue(Constants.VIDEO_NAME_KEY, response.getMovie());
//        }
//        else if (!StringUtils.isEmpty(response.getTeleplay()))
//        {
//            context.setValue(Constants.VIDEO_NAME_KEY, response.getTeleplay());
//        }
        if (!StringUtils.isEmpty(response.getStar()))
        {
            context.setValue(Constants.START_NAME_KEY, response.getStar());
        }
    }
    
    @SuppressWarnings("unused")
    private void parserNLUResponse(Context context)
    {
        List<Response> responseList = getOutputMap(context).get(CommonResponseType.NLU);
        if (responseList == null || responseList.isEmpty())
        {
            return;
        }
        NLUResponse response = (NLUResponse) responseList.get(0);
        
        if (!StringUtils.isEmpty(response.getMovie()) && !context.isContainsKey(Constants.VIDEO_NAME_KEY))
        {
            context.setValue(Constants.VIDEO_NAME_KEY, response.getMovie());
        }
        if (StringUtils.isEmpty(response.getStar()) && !context.isContainsKey(Constants.START_NAME_KEY))
        {
            context.setValue(Constants.START_NAME_KEY, response.getStar());
        }
    }
}
