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
import com.emotibot.middleware.response.nlu.NLUResponse;
import com.emotibot.middleware.step.AbstractStep;
import com.emotibot.middleware.task.restCallTask.NLUTask;
import com.emotibot.middleware.utils.UrlUtils;
import com.emotibot.parser.common.Constants;

public class ParserNameEntitiesStep extends AbstractStep
{    
    public ParserNameEntitiesStep()
    {
    }
    
    public ParserNameEntitiesStep(ExecutorService executorService)
    {
        super(executorService);
    }
    
    @Override
    public void beforeRun(Context context)
    {
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        
        Object obj = context.getValue(Constants.NLU_CACHE_KEY);
        if (obj != null)
        {
            List<Response> nluReponseList = new ArrayList<Response>();
            nluReponseList.add((Response)obj);
            context.getOutputMap().put(CommonResponseType.NLU, nluReponseList);
            return;
        }
        
        NLUTask task = new NLUTask();
        String text = "?f=srl&q=" + sentence;
        String hostname = ConfigManager.INSTANCE.getPropertyString(Constants.TARGET_HOST);
        String port = ConfigManager.INSTANCE.getPropertyString(Constants.NLU_PORT);
        String endpoint = ConfigManager.INSTANCE.getPropertyString(Constants.NLU_ENDPOINT);
        String url = UrlUtils.getUrl(hostname, port, endpoint, text);
        HttpRequest request = new HttpRequest(url, null, HttpRequestType.GET);
        task.setRequest(request);
        task.setUniqId(context.getUniqId());
        context.addTask(task);
    }

    @Override
    public void afterRun(Context context)
    {
        context.clearTaskList();
        List<Response> responseList = context.getOutputMap().get(CommonResponseType.NLU);
        if (responseList == null || responseList.isEmpty())
        {
            return;
        }
        NLUResponse response = (NLUResponse)responseList.get(0);
        //输入是2，取最后两个动词作用的对象
        List<String> parserNameEntityList = response.getNameEntityBySRL(2);
        parserNameEntityList = adjustNameEntities(context, parserNameEntityList);
        context.setValue(Constants.NAME_ENTITY_LIST_KEY, parserNameEntityList);
    }
    
    @SuppressWarnings("unchecked")
    private List<String> adjustNameEntities(Context context, List<String> nameEntities)
    {
        List<String> ret = new ArrayList<String>();
        if (nameEntities == null || nameEntities.isEmpty())
        {
            return ret;
        }
        Object obj = context.getValue(Constants.VIDEO_REWRITE_TOKENS_KEY);
        List<String> matchTokens = null;
        if (obj == null)
        {
            matchTokens = new ArrayList<String>();
        }
        else
        {
            matchTokens = (List<String>) obj;
        }
        
        String starName = (String) context.getValue(Constants.START_NAME_KEY);
        if (matchTokens.isEmpty() && starName == null)
        {
            return nameEntities;
        }
        for (String nameEntity : nameEntities)
        {
            String nameEntityTmp = nameEntity;
            for (String token : matchTokens)
            {
                nameEntityTmp = nameEntityTmp.replaceFirst(token, "");
            }
            if (!StringUtils.isEmpty(starName))
            {
                nameEntityTmp = nameEntityTmp.replaceFirst(starName, "");
            }
            ret.add(nameEntityTmp);
        }
        return ret;
    }
}
