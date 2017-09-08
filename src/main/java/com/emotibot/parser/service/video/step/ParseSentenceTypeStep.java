package com.emotibot.parser.service.video.step;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.CommonResponseType;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.response.nlu.NLU;
import com.emotibot.middleware.response.nlu.NLUResponse;
import com.emotibot.middleware.step.AbstractStep;
import com.emotibot.middleware.task.restCallTask.NLUTask;
import com.emotibot.middleware.utils.UrlUtils;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.common.SentenceType;

public class ParseSentenceTypeStep extends AbstractStep
{
    
    public ParseSentenceTypeStep()
    {
    }
    
    public ParseSentenceTypeStep(ExecutorService executorService)
    {
        super(executorService);
    }
    
    @Override
    public void beforeRun(Context context)
    {
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        
        NLUTask task = new NLUTask();
        String text = "?f=polarity,sentenceType&q=" + sentence;
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
        NLU nlu = response.getNLU();
        if (nlu == null)
        {
            return;
        }
        String sentenceType = nlu.getSentenceType();
        SentenceType type = null;
        if (sentenceType != null && !sentenceType.trim().isEmpty())
        {
            if(sentenceType.contains("问句"))
            {
                type = SentenceType.QUESTION;
            }
            else if(sentenceType.contains("否定"))
            {
                type = SentenceType.NEGETIVE;
            }
            else if(sentenceType.contains("肯定"))
            {
                type = SentenceType.POSTIVE;
            }
        }
        if (type != null)
        {
            context.setValue(Constants.SENTENCE_TYPE_KEY, type);
            return;
        }
        String polarity = nlu.getPolarity();
        if (polarity != null && !polarity.trim().isEmpty())
        {
            if (polarity.contains("Negative"))
            {
                type = SentenceType.NEGETIVE;
            }
            else if(polarity.contains("Positive"))
            {
                type = SentenceType.POSTIVE;
            }
            context.setValue(Constants.SENTENCE_TYPE_KEY, type);
        }
    }
}
