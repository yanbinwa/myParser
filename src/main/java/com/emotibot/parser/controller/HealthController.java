package com.emotibot.parser.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController
{
    @RequestMapping(value="/checkHealth", method = RequestMethod.GET)
    public String checkHealth()
    {
        return "";
    }
}
