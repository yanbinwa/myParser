package com.emotibot.parser.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.emotibot.correction.service.CorrectionService;
import com.emotibot.correction.service.CorrectionServiceImpl;

@Configuration
public class ApplicationConfig
{
    @Bean(name="correctionService")
    public CorrectionService correctionService()
    {
        return new CorrectionServiceImpl();
    }
}
