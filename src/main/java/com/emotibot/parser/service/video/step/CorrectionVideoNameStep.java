package com.emotibot.parser.service.video.step;

import java.util.List;

import com.emotibot.correction.service.CorrectionService;
import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.Response;
import com.emotibot.middleware.response.ResponseType;
import com.emotibot.middleware.response.nlu.NLUResponse;
import com.emotibot.middleware.step.AbstractStep;
import com.emotibot.middleware.task.restCallTask.NLUTask;
import com.emotibot.middleware.utils.UrlUtils;
import com.emotibot.parser.common.Constants;

public class CorrectionVideoNameStep extends AbstractStep
{

    private String correctedVideoName = null;
    private CorrectionService correctionService = null;
    
    public CorrectionVideoNameStep(CorrectionService correctionService)
    {
        this.correctionService = correctionService;
    }
    
    @Override
    public void beforeRun()
    {
        String sentence = (String) context.getValue(Constants.SENTENCE_KEY);
        
        NLUTask task = new NLUTask();
        String text = "?f=srl&q=" + sentence;
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
        correctedVideoName = response.getNameEntityBySRL();
        List<String> resultList = correctionService.correct(correctedVideoName);
        if (resultList == null || resultList.isEmpty())
        {
            return;
        }
        System.out.println(resultList);
        context.setValue(Constants.CORRECTED_VIDEO_NAME_KEY, resultList.get(0));
    }
    
}
