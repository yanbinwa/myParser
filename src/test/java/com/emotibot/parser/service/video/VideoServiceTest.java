package com.emotibot.parser.service.video;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.emotibot.middleware.request.HttpRequest;
import com.emotibot.middleware.request.HttpRequestType;
import com.emotibot.middleware.response.HttpResponse;
import com.emotibot.middleware.utils.HttpUtils;

import au.com.bytecode.opencsv.CSVReader;

public class VideoServiceTest
{

    public static final String correctionFile = "/Users/emotibot/Documents/workspace/other/myParser/file/correctionTest.csv";
    
    public static final String service_url = "http://localhost:9090/video/getVideoName";
    
    //没有第几集这边的表达
    //public static final String correctionFile2 = "/Users/emotibot/Documents/workspace/other/myParser/file/correctionTest.csv";
    
    public static final String sentenceTemplate = "/Users/emotibot/Documents/workspace/other/myParser/file/sentenceTemplate.csv";
    
    public static final int threadNum = 10;
    
    public static int totalCount = 0;
    public static int errorTotalCount = 0;
    
    @Test
    public void test() throws Exception
    {
        long startTime = System.currentTimeMillis();
        test2();
        long endTime = System.currentTimeMillis();
        System.out.println("用时：[" + (endTime - startTime) + "ms]");
        System.out.println("totalCount: " + totalCount + "; errorCount: " + errorTotalCount + "; errorRate: " + (errorTotalCount / (double)totalCount));
    }

    @SuppressWarnings("unused")
    private void test1() throws IOException
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
            String correctSentence = ss[0];
            String errorSentence = ss[1];
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
        System.out.println("total count: " + count + "; error count: " + errorCount + "; errorRate: " + (errorTotalCount / (double)totalCount));
    }
    
    @SuppressWarnings("unused")
    private void test2() throws IOException
    {
        File file = new File(correctionFile);  
        FileReader fReader = new FileReader(file);  
        CSVReader csvReader = new CSVReader(fReader); 
        List<String[]> correctList = csvReader.readAll();
        fReader.close();
        csvReader.close();
        
        file = new File(sentenceTemplate);  
        fReader = new FileReader(file); 
        csvReader = new CSVReader(fReader); 
        List<String[]> sentenceTemplateList = csvReader.readAll();
        fReader.close();
        csvReader.close();
        
        int count = 0;
        int errorCount = 0;
        for (String[] ss : correctList)
        {
            count ++;
            String correctSentence = ss[0];
            String errorSentence = ss[1];
            String sentenceTemplate = sentenceTemplateList.get(new Random().nextInt(sentenceTemplateList.size()))[0];
            String sentence = sentenceTemplate.replaceAll("XXX", errorSentence);
            HttpRequest request = new HttpRequest(service_url, sentence, HttpRequestType.POST);
            HttpResponse response = HttpUtils.post(request, 10000);
            String result = response.getResponse();
            if (!result.trim().equals(correctSentence))
            {
                System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; sentence: " + sentence + "; result: " + result);
                errorCount ++;
            }
        }
        System.out.println("total count: " + count + "; error count: " + errorCount);
    }
    
    @SuppressWarnings("unused")
    private void test3() throws IOException, InterruptedException
    {
        File file = new File(correctionFile);  
        FileReader fReader = new FileReader(file);  
        CSVReader csvReader = new CSVReader(fReader); 
        List<String[]> correctList = csvReader.readAll();
        fReader.close();
        csvReader.close();
        
        file = new File(sentenceTemplate);  
        fReader = new FileReader(file); 
        csvReader = new CSVReader(fReader); 
        List<String[]> sentenceTemplateList = csvReader.readAll();
        fReader.close();
        csvReader.close();
        
        int sentencePerTask = correctList.size() / threadNum;
        List<Thread> threadList = new ArrayList<Thread>();
        for (int i = 0; i < threadNum; i ++)
        {
            Thread thread = new Thread(new TestTask(correctList.subList(i * sentencePerTask, (i + 1) * sentencePerTask), sentenceTemplateList));
            thread.start();
            threadList.add(thread);
        }
        for (Thread thread : threadList)
        {
            thread.join();
        }
    }
    
    class TestTask implements Runnable
    {

        List<String[]> testSentenceList = null;
        List<String[]> sentenceTemplateList = null;
        
        public TestTask(List<String[]> testSentenceList, List<String[]> sentenceTemplateList)
        {
            this.testSentenceList = testSentenceList;
            this.sentenceTemplateList = sentenceTemplateList;
        }
        
        @Override
        public void run()
        {
            int count = 0;
            int errorCount = 0;
            for (String[] ss : testSentenceList)
            {
                count ++;
                String correctSentence = ss[0];
                String errorSentence = ss[1];
                String sentenceTemplate = sentenceTemplateList.get(new Random().nextInt(sentenceTemplateList.size()))[0];
                String sentence = sentenceTemplate.replaceAll("XXX", errorSentence);
                HttpRequest request = new HttpRequest(service_url, sentence, HttpRequestType.POST);
                HttpResponse response = HttpUtils.post(request, 10000);
                String result = response.getResponse();
                if (!result.trim().equals(correctSentence))
                {
                    System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; sentence: " + sentence + "; result: " + result);
                    errorCount ++;
                }
            }
            totalCount += count;
            errorTotalCount += errorCount;
            System.out.println("total count: " + count + "; error count: " + errorCount);
        }
    }
    
    class TestTask1 implements Runnable
    {

        List<String[]> testSentenceList = null;
        List<String[]> sentenceTemplateList = null;
        
        public TestTask1(List<String[]> testSentenceList, List<String[]> sentenceTemplateList)
        {
            this.testSentenceList = testSentenceList;
            this.sentenceTemplateList = sentenceTemplateList;
        }
        
        @Override
        public void run()
        {
            int count = 0;
            int errorCount = 0;
            for (String[] ss : testSentenceList)
            {
                count ++;
                String correctSentence = ss[0];
                String errorSentence = ss[1];
                for (String[] sentenceTemplates : sentenceTemplateList)
                {
                    String sentenceTemplate = sentenceTemplates[0];
                    String sentence = sentenceTemplate.replaceAll("XXX", errorSentence);
                    HttpRequest request = new HttpRequest(service_url, sentence, HttpRequestType.POST);
                    HttpResponse response = HttpUtils.post(request, 10000);
                    String result = response.getResponse();
                    if (!result.trim().equals(correctSentence))
                    {
                        System.out.println("correct: " + correctSentence + "; error: " + errorSentence + "; sentence: " + sentence + "; result: " + result);
                        errorCount ++;
                    }
                }                
            }
            totalCount += count;
            errorTotalCount += errorCount;
            System.out.println("total count: " + count + "; error count: " + errorCount);
        }
    }
}
