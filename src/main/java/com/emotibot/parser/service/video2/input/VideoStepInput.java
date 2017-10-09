package com.emotibot.parser.service.video2.input;

import com.emotibot.middleware.context.Context;
import com.emotibot.middleware.controller.input.AbstractStepInput;
import com.emotibot.parser.common.Constants;

public class VideoStepInput extends AbstractStepInput
{

    @Override
    public Context execute(String input, Context context)
    {
        context.setValue(Constants.SENTENCE_KEY, input);
        return context;
    }

}
