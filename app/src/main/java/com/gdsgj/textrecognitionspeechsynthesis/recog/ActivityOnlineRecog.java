package com.gdsgj.textrecognitionspeechsynthesis.recog;


import com.gdsgj.textrecognitionspeechsynthesis.R;

/**
 * 在线识别，用于展示在线情况下的识别参数和效果。
 * <p>
 * 本类可以忽略 看下ActivityAbstractRecog
 */
public class ActivityOnlineRecog extends ActivityAbstractRecog {

    public ActivityOnlineRecog() {
        super(R.raw.online_recog, false);
        // uiasr\src\main\res\raw\online_recog.txt 本Activity使用的说明文件
        // false 表示activity不支持离线
    }


}