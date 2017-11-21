package com.emotibot.parser.service.video;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.HttpResponse;
import com.emotibot.middleware.utils.HttpUtils;

import au.com.bytecode.opencsv.CSVReader;

public class Video2ServiceImplTest
{

    public static final String correctionFile = "/Users/emotibot/Documents/workspace/other/myParser/file/correctionTest2.csv";
    
    public static final String service_url = "http://localhost:9090/video/getVideo2Name";
        
    @Test
    public void test() throws IOException
    {
        test1();
    }
    
    private static void test1() throws IOException
    {
        File file = new File(correctionFile);  
        FileReader fReader = new FileReader(file);  
        CSVReader csvReader = new CSVReader(fReader); 
        List<String[]> list = csvReader.readAll();
        int count = 0;
        int errorCount = 0;
        for (String[] ss : list)
        {
            count ++;
            String correctSentence = ss[1];
            String errorSentence = ss[0];
            HttpRequest request = new HttpRequest(service_url, errorSentence, HttpRequestType.POST);
            HttpResponse response = HttpUtils.post(request, 10000);
            String result = response.getResponse();
            if (!result.trim().equals(correctSentence))
            {
                System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; result: " + result);
                errorCount ++;
            }
        }
        csvReader.close();
        fReader.close();
        System.out.println("total count: " + count + "; error count: " + errorCount);
    }

}
