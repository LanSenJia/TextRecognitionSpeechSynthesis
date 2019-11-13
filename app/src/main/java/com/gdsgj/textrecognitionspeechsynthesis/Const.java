package com.gdsgj.textrecognitionspeechsynthesis;


public class Const {

    // file
    public static final String MY_BASE_DIR = "MXTts"; //恢复成原来的文件夹名
    public static final String OFFLINE_VOICE_DIR = "offline";
    public static final String OUTPUT_DIR = "output";
    public static String TOKEN ="";


    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected static String appId = "17721709";

    protected static String appKey = "4gjVTa9Q6Mjkzya9hEc6OYEQ";

    protected static String secretKey = "8o5VRVhosQOoRpTL5GKTCdhmxDWijIL7 ";

}
