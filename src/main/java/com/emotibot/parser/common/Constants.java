package com.emotibot.parser.common;

public class Constants
{
    public static final String TARGET_HOST = "TARGET_HOST";
    
    //NLU
    public static final String NLU_PORT = "NLU_PORT";
    public static final String NLU_ENDPOINT = "NLU_ENDPOINT";
    
    //COMMON_PARSER
    public static final String COMMON_PARSER_PORT = "COMMON_PARSER_PORT";
    public static final String COMMON_PARSER_ENDPOINT = "COMMON_PARSER_ENDPOINT";
    public static final String COMMON_PARSER_SUCCESS_TAG = "SUCCESS";
    
    //VideoService
    public static final String SENTENCE_KEY = "sentence";
    public static final String VIDEO_NAME_KEY = "videoName";
    public static final String START_NAME_KEY = "starName";
    public static final String NAME_ENTITY_LIST_KEY = "nameEntityList";
    public static final String CORRECTED_VIDEO_NAME_KEY = "correctedVideoName";
    public static final String SENTENCE_TYPE_KEY = "sentenceType";
    
    //ParserVideoBaseStep
    public static final String VIDEO_EPISODE_KEY = "episode";
    public static final String VIDEO_SEASON_KEY = "season";
    public static final String VIDEO_REWRITE_SENTENCE_KEY = "rewriteSentence";
    public static final String VIDEO_REWRITE_TOKENS_KEY = "rewriteTokens";
    public static final String NLU_CACHE_KEY = "nlu_cache";
    
    public static final String VIDEO_KEYWORD_N_KEY = "keywordN";
    public static final String VIDEO_KEYWORD_A_KEY = "keywordA";
    
    //MyEmbeddedServletContainerFactory
    public static final String TOMCAT_PORT_KEY = "TOMCAT_PORT";
    public static final String TOMCAT_MAX_CONNECTION_KEY = "TOMCAT_MAX_CONNECTION";
    public static final String TOMCAT_MAX_THREAD_KEY = "TOMCAT_MAX_THREAD";
    public static final String TOMCAT_CONNECTION_TIMEOUT_KEY = "TOMCAT_CONNECTION_TIMEOUT";
    
    //Controller
    public static final String CONTROLLER_CONFIG_FILE_KEY = "CONTROLLER_CONFIG_FILE";
    
    /* 训练参数 */
    //Param CorrectNameEntities
    public static final double EDIT_DISTANCE_1_RATE = 0.5;
    public static final double EDIT_DISTANCE_2_RATE = 5;
    public static final double EDIT_DISTANCE_3_RATE = 0.5;
    
    public static final double CALCULATE_DIFF_RATE = 0.05;
    public static final int CORRECT_SELECT_NUM = 10;
    public static final double CORRECT_CHOOSE_RATE = 0.4;
    
    //Param CorrectionTask
    public static final double DIFF_LEN_RATE_LEVEL_1 = 0.2;
    public static final double DIFF_LEN_RATE_LEVEL_2 = 0.4;
    public static final double DIFF_LEN_RATE_LEVEL_3 = 0.6;
    public static final double DIFF_LEN_RATE_LEVEL_4 = 0.8;
    
    public static final double DIFF_LEN_ADJUST_RATE_1 = 0.7;
    public static final double DIFF_LEN_ADJUST_RATE_2 = 0.5;
    public static final double DIFF_LEN_ADJUST_RATE_3 = 0.3;
    public static final double DIFF_LEN_ADJUST_RATE_4 = 0.1;
    
    public static final double DIFF_LEN_THRESHOLD = 2;
    public static final double DIFF_LEN_THRESHOLD_RATE = 0.2;
    
    //final select
    public static final double MAX_DIFF_LEN = 2;
    public static final double MAX_DIFF_RATE = 2;
}
