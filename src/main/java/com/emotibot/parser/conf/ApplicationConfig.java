package com.emotibot.parser.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.emotibot.middleware.conf.ConfigManager;
import com.emotibot.middleware.controller.controller.StepController;
import com.emotibot.middleware.controller.controller.StepControllerImpl;
import com.emotibot.parser.common.Constants;
import com.emotibot.parser.common.synonym.SynonymService;
import com.emotibot.parser.common.synonym.SynonymServiceImpl;

@Configuration
public class ApplicationConfig
{
    @Bean(name="stepController")
    public StepController stepController()
    {
        StepController controller = new StepControllerImpl();
        String controllerConfigFile = ConfigManager.INSTANCE.getPropertyString(Constants.CONTROLLER_CONFIG_FILE_KEY);
        controller.loadConfig(controllerConfigFile);
        return controller;
    }
    
    @Bean(name="synonymService")
    public SynonymService synonymService()
    {
        return new SynonymServiceImpl();
    }
}
