package com.gdsgj.textrecognitionspeechsynthesis;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.client.SpeechSynthesizer;

import static android.content.Context.CLIPBOARD_SERVICE;

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
    protected CustomAlertDialog(Context context) {
        super(context);
    }

    @Override
    public void onClick(View v) {

    }
//    private String editBtnText = "编辑", copyBtnText = "复制", playAudioBtnText = "播放", exitBtnText = "退出";
////
////    private String title;
////    private String message;
////    private ImageView dialogHeadIcon;
////    private TextView dialogTitle;
////    private EditText dialogResultEt;
////    private Button dialogEditBtn;
////    private Button dialogCopyBtn;
////    private Button dialogPlayAudioBtn;
////    private Button dialogExitBtn;
////    private SpeechSynthesizer speechSynthesizer;
////    private boolean isClick = false;
////    private ClipData clipData;
////    private Context context;
////
////    private String TAG = "CustomAlertDialog";
////
////    public CustomAlertDialog(@NonNull Context context) {
////        super(context);
////    }
////
////    public CustomAlertDialog(@NonNull Context context, int themeResId) {
////        super(context, themeResId);
////    }
////
////    public CustomAlertDialog(Context context, String title, String message, SpeechSynthesizer speechSynthesizer) {
////        super(context);
////        this.title = title;
////        this.message = message;
////        this.speechSynthesizer = speechSynthesizer;
////    }
////
////    public CustomAlertDialog(Context context, String editBtnText, String copyBtnText, String playAudioBtnText, String exitBtnText, String title, String message, SpeechSynthesizer speechSynthesizer) {
////        super(context);
////        this.editBtnText = editBtnText;
////        this.copyBtnText = copyBtnText;
////        this.playAudioBtnText = playAudioBtnText;
////        this.exitBtnText = exitBtnText;
////        this.title = title;
////        this.message = message;
////        this.speechSynthesizer = speechSynthesizer;
////    }
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        setAlertView();
////        super.onCreate(savedInstanceState);
////
////    }
////
////
////    @Override
////    public void show() {
////        // 在Show方法中直接调用
////        setCancelable(false);
////        create();
////        super.show();
////
////    }
////
////
////    @Override
////    public void onClick(View view) {
////        switch (view.getId()) {
////            //退出
////            case R.id.dialog_exit_btn:
////
////                if (speechSynthesizer != null) {
////                    speechSynthesizer.stop();
////                }
////
////                dismiss();
////                break;
////            //播放音频
////            case R.id.dialog_play_audio_btn:
////
////                speak(message);
////                //复制文字
////            case R.id.dialog_copy_btn:
////                ClipboardManager myClipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
////
////                ClipData clipData = ClipData.newPlainText("message", message);
////                myClipboard.setPrimaryClip(clipData);
////                Log.i(TAG, "onClick: 已复制");
////
////                break;
////            //编辑
////            case R.id.dialog_edit_btn:
////
////                if (!isClick) {
////                    setCanEdit();
////                } else {
////                    setCanNotEditNoClick();
////                }
////
////
////            default:
////                break;
////        }
////    }
////
////
////    //不可编辑
////    public void setCanNotEditNoClick() {
////        dialogResultEt.setFocusable(false);
////        dialogResultEt.setFocusableInTouchMode(false);
////        // 如果之前没设置过点击事件，该处可省略
////        dialogResultEt.setOnClickListener(null);
////        dialogEditBtn.setText("编辑");
////    }
////
////    //可编辑
////    public void setCanEdit() {
////        dialogResultEt.setFocusable(true);
////        dialogResultEt.setFocusableInTouchMode(true);
////        dialogEditBtn.setText("保存");
////    }
////
////
////    /**
////     * 配置自定义 View
////     */
////    public void setAlertView() {
////        // 布局
////        View view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.activityallrecog_dialog_layout, null);
////
////
////        dialogHeadIcon = (ImageView) view.findViewById(R.id.dialog_head_icon);
////        dialogTitle = (TextView) findViewById(R.id.dialog_title);
////        dialogTitle.setText(title);
////        dialogResultEt = (EditText) view.findViewById(R.id.dialog_result_et);
////        dialogResultEt.setText(message);
////
////        dialogEditBtn = (Button) view.findViewById(R.id.dialog_edit_btn);
////        dialogEditBtn.setText(editBtnText);
////        dialogEditBtn.setOnClickListener(this);
////
////        dialogCopyBtn = (Button) view.findViewById(R.id.dialog_copy_btn);
////        dialogCopyBtn.setText(copyBtnText);
////        dialogCopyBtn.setOnClickListener(this);
////
////        dialogPlayAudioBtn = (Button) view.findViewById(R.id.dialog_play_audio_btn);
////        dialogPlayAudioBtn.setText(playAudioBtnText);
////        dialogPlayAudioBtn.setOnClickListener(this);
////
////        dialogExitBtn = (Button) view.findViewById(R.id.dialog_exit_btn);
////        dialogExitBtn.setText(exitBtnText);
////        dialogExitBtn.setOnClickListener(this);
////
////        setView(view);
////
////    }
////
////    /**
////     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
////     * 需要合成的文本text的长度不能超过1024个GBK字节。
////     */
////    private void speak(String text) {
////        // 需要合成的文本text的长度不能超过1024个GBK字节。
////        // 合成前可以修改参数：
////        // Map<String, String> params = getParams();
////        // synthesizer.setParams(params);
////
////        if (speechSynthesizer != null) {
////            speechSynthesizer.speak(text);
////        }
////
////
////    }




}
