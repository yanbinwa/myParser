package com.emotibot.parser.service.video.step;

import java.util.List;

import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.response.ResponseType;
import com.emotibot.middleware.response.nlu.NLU;
import com.emotibot.middleware.response.nlu.NLUResponse;
import com.emotibot.middleware.step.AbstractStep;
import com.emotibot.middleware.task.restCallTask.NLUTask;
import com.emotibot.middleware.utils.UrlUtils;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.common.SentenceType;

public class ParseSentenceTypeStep extends AbstractStep
{

    private SentenceType type = null;
    
    public ParseSentenceTypeStep()
    {
    }
    
    @Override
    public void beforeRun()
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
        this.addTask(task);
    }

    @Override
    public void afterRun()
    {
        List<Response> responseList = this.outputMap.get(ResponseType.NLU);
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
        if (sentenceType != null && !sentenceType.trim().isEmpty())
        {
            if(sentenceType.contains("问句"))
            {
                this.type = SentenceType.QUESTION;
                return;
            }
            else if(sentenceType.contains("否定"))
            {
                this.type = SentenceType.NEGETIVE;
                return;
            }
            else if(sentenceType.contains("肯定"))
            {
                this.type = SentenceType.POSTIVE;
                return;
            }
        }
        String polarity = nlu.getPolarity();
        if (polarity != null && !polarity.trim().isEmpty())
        {
            if (polarity.contains("Negative"))
            {
                this.type = SentenceType.NEGETIVE;
                return;
            }
            else if(polarity.contains("Positive"))
            {
                this.type = SentenceType.POSTIVE;
                return;
            }
        }
    }
    
    public SentenceType getSentenceType()
    {
        return this.type;
    }

}
