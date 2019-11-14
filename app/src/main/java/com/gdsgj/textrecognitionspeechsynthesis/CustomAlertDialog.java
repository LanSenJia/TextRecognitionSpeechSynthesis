package com.gdsgj.textrecognitionspeechsynthesis;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.solver.Metrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.tts.client.SpeechSynthesizer;

/*
 * @Title:
 * @Copyright:  GuangZhou F.R.O Electronic Technology Co.,Ltd. Copyright 2006-2016,  All rights reserved
 * @Descrplacetion:  <请描述此文件是做什么的>
 * @author:  lansenboy
 * @data: 2019/11/14
 * @version:  V1.0
 * @OfficialWebsite: http://www.frotech.com/
 */
public class CustomAlertDialog extends AlertDialog implements View.OnClickListener {
    private AlertDialogMessageListner messageListner;
    private String editBtnText = "编辑", copyBtnText = "复制", playAudioBtnText = "播放", exitBtnText = "退出";

    private String title;
    private String message;
    private ImageView dialogHeadIcon;
    private TextView dialogTitle;
    private TextView dialogResultTv;
    private Button dialogEditBtn;
    private Button dialogCopyBtn;
    private Button dialogPlayAudioBtn;
    private Button dialogExitBtn;
    private SpeechSynthesizer speechSynthesizer;

    public CustomAlertDialog(@NonNull Context context) {
        super(context);
    }

    public CustomAlertDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    // 传递Header参数
    public CustomAlertDialog(Context context, String title, String message, SpeechSynthesizer speechSynthesizer) {
        super(context);
        this.title = title;
        this.message = message;
        this.speechSynthesizer = speechSynthesizer;
    }

    public CustomAlertDialog(Context context, String editBtnText, String copyBtnText, String playAudioBtnText, String exitBtnText, String title, String message, SpeechSynthesizer speechSynthesizer) {
        super(context);
        this.editBtnText = editBtnText;
        this.copyBtnText = copyBtnText;
        this.playAudioBtnText = playAudioBtnText;
        this.exitBtnText = exitBtnText;
        this.title = title;
        this.message = message;
        this.speechSynthesizer = speechSynthesizer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAlertView();
        super.onCreate(savedInstanceState);

    }

    public interface AlertDialogMessageListner {

        void editBtnClick();

        void copyBtnClick();

        void playAudioBtnClick();

        void exitBtnClick();

    }

    @Override
    public void show() {
        // 在Show方法中直接调用
        setCancelable(false);
        create();
        super.show();

//        // 设置 AlertDialog的 宽高和位置
//        Window dialogWindow = getWindow();
//        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
//        dialogWindow.setGravity(Gravity.CENTER);
//        layoutParams.width = (int) (new Metrics(getContext()).screenWidth() * 0.75);
//        // 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
//        dialogWindow.setAttributes(layoutParams);


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_exit_btn:
                if (messageListner != null) {
                    getMessageListner().exitBtnClick();
                }
                dismiss();
                break;
            case R.id.dialog_play_audio_btn:
                if (messageListner != null) {
                    getMessageListner().playAudioBtnClick();
                }
                speak(message);
            case R.id.dialog_copy_btn:
                if (messageListner != null) {
                    getMessageListner().copyBtnClick();
                }
                break;

            case R.id.dialog_edit_btn:
                if (messageListner != null) {
                    getMessageListner().editBtnClick();
                }
            default:
                break;
        }
    }

    public AlertDialogMessageListner getMessageListner() {
        return messageListner;
    }

    public void setMessageListner(AlertDialogMessageListner messageListner) {
        this.messageListner = messageListner;
    }

    /**
     * 配置自定义 View
     */
    public void setAlertView() {
        // 布局
        View view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.activityallrecog_dialog_layout, null);


        dialogHeadIcon = (ImageView) findViewById(R.id.dialog_head_icon);
        dialogTitle = (TextView) findViewById(R.id.dialog_title);
        dialogTitle.setText(title);
        dialogResultTv = (TextView) findViewById(R.id.dialog_result_tv);
        dialogResultTv.setText(message);

        dialogEditBtn = (Button) findViewById(R.id.dialog_edit_btn);
        dialogEditBtn.setText(editBtnText);
        dialogEditBtn.setOnClickListener(this);

        dialogCopyBtn = (Button) findViewById(R.id.dialog_copy_btn);
        dialogCopyBtn.setText(copyBtnText);
        dialogCopyBtn.setOnClickListener(this);

        dialogPlayAudioBtn = (Button) findViewById(R.id.dialog_play_audio_btn);
        dialogPlayAudioBtn.setText(playAudioBtnText);
        dialogPlayAudioBtn.setOnClickListener(this);

        dialogExitBtn = (Button) findViewById(R.id.dialog_exit_btn);
        dialogExitBtn.setText(exitBtnText);
        dialogExitBtn.setOnClickListener(this);


//        // 透明背景
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        setView(view);

    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void speak(String text) {
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);

        if (speechSynthesizer != null) {
            int result = speechSynthesizer.speak(text);
        }
    }


}
