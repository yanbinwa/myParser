package com.emotibot.parser.component;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class InitComponent implements ApplicationListener<ApplicationReadyEvent>
{
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        //CorrectionUtils.test();
    }

}
