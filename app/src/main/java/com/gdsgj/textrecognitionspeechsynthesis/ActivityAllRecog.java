package com.gdsgj.textrecognitionspeechsynthesis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.asrwakeup3.uiasr.Base64Util;
import com.baidu.aip.asrwakeup3.uiasr.FaceBeanClass;
import com.baidu.aip.asrwakeup3.uiasr.FileUtil;
import com.baidu.aip.asrwakeup3.uiasr.PhotoUtils;
import com.baidu.aip.asrwakeup3.uiasr.activity.Beans;
import com.baidu.aip.imageclassify.AipImageClassify;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.gdsgj.textrecognitionspeechsynthesis.BaiduTTS.util.OfflineResource;
import com.gdsgj.textrecognitionspeechsynthesis.bean.GsonImpl;
import com.gdsgj.textrecognitionspeechsynthesis.bean.LicensePlateBean;
import com.gdsgj.textrecognitionspeechsynthesis.bean.UniversalTextRecognitionBean;
import com.gdsgj.textrecognitionspeechsynthesis.recog.ActivityAbstractRecog;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.gdsgj.textrecognitionspeechsynthesis.Const.appId;
import static com.gdsgj.textrecognitionspeechsynthesis.Const.secretKey;

public class ActivityAllRecog extends AppCompatActivity implements EventListener {
    private TextView txtResult;
    private TextView txtLog;
    private TextView resultTv;
    private ImageView recogIv;
    private Button btn;
    private final int PICK = 1;
    private final int IMAGE_RESULT_CODE = 2;
    private final int FACE = 3;
    private boolean isNeedSaveTTS = false;
    private BufferedOutputStream ttsFileBufferedOutputStream;

    private String TAG = "ActivityAllRecog";
    private EventManager asr;
    private String imageclassifyUrl = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general";//
    private String faceUrl = "https://aip.baidubce.com/rest/2.0/face/v3/detect";
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean logTime = true;
    private FileOutputStream ttsFileOutputStream;

    protected boolean enableOffline = false; // 测试离线命令词，需要改成true
    private Button stopBtn;
    private Uri imageUri;
    private String imgParam;

    private static final int REQUEST_CODE_GENERAL = 105;
    private static final int REQUEST_CODE_GENERAL_BASIC = 106;
    private static final int REQUEST_CODE_ACCURATE_BASIC = 107;
    private static final int REQUEST_CODE_ACCURATE = 108;
    private static final int REQUEST_CODE_GENERAL_ENHANCED = 109;
    private static final int REQUEST_CODE_GENERAL_WEBIMAGE = 110;
    private static final int REQUEST_CODE_BANKCARD = 111;
    private static final int REQUEST_CODE_VEHICLE_LICENSE = 120;
    private static final int REQUEST_CODE_DRIVING_LICENSE = 121;
    private static final int REQUEST_CODE_LICENSE_PLATE = 122;
    private static final int REQUEST_CODE_BUSINESS_LICENSE = 123;
    private static final int REQUEST_CODE_RECEIPT = 124;

    private static final int REQUEST_CODE_PASSPORT = 125;
    private static final int REQUEST_CODE_NUMBERS = 126;
    private static final int REQUEST_CODE_QRCODE = 127;
    private static final int REQUEST_CODE_BUSINESSCARD = 128;
    private static final int REQUEST_CODE_HANDWRITING = 129;
    private static final int REQUEST_CODE_LOTTERY = 130;
    private static final int REQUEST_CODE_VATINVOICE = 131;
    private static final int REQUEST_CODE_CUSTOM = 132;
    private static final int REQUEST_CODE_SMIILS = 133;
    private static final int REQUEST_CODE_CAR_MODEL = 134;
    private AlertDialog.Builder alertDialog;
    private SpeechSynthesizer mSpeechSynthesizer;
    private Application application;


    // ================== 初始化参数设置开始 ==========================


    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.ONLINE;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;


    // ================选择TtsMode.ONLINE  不需要设置以下参数; 选择TtsMode.MIX 需要设置下面2个离线资源文件的路径
    private static final String TEMP_DIR = "/sdcard/baiduTTS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";

    // 请确保该PATH下有这个文件 ，m15是离线男声
    private static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
    private TextView scan_value_tv;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_matin);
        initView();
        initRecog();
        initialTts();
        initPermission();

    }


    private void initView() {
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        resultTv = (TextView) findViewById(R.id.result_tv);
        recogIv = (ImageView) findViewById(R.id.recog_iv);
        btn = (Button) findViewById(R.id.btn);
        stopBtn = (Button) findViewById(R.id.btn_stop);
        scan_value_tv = (TextView) findViewById(R.id.scan_value_tv);
        alertDialog = new AlertDialog.Builder(this);
        application = new Application();
    }

    private void initRecog() {
        // 基于sdk集成1.1 初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");
        // 基于sdk集成1.3 注册自己的输出事件类
        asr.registerListener(this); //  EventListener 中 onEvent方法
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stop();
            }
        });
    }


    /**
     * 基于SDK集成2.2 发送开始事件
     * 点击开始按钮
     * 测试参数填在这里
     */
    private void start() {
        txtLog.setText("");
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params.put(SpeechConstant.NLU, "enable");
        // params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
        // params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
        // params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        // params.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号

        /* 语音自训练平台特有参数 */
        // params.put(SpeechConstant.PID, 8002);
        // 语音自训练平台特殊pid，8002：搜索模型类似开放平台 1537  具体是8001还是8002，看自训练平台页面上的显示
        // params.put(SpeechConstant.LMID,1068); // 语音自训练平台已上线的模型ID，https://ai.baidu.com/smartasr/model
        // 注意模型ID必须在你的appId所在的百度账号下
        /* 语音自训练平台特有参数 */

        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);
        // 复制此段可以自动检测错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        txtLog.append(message + "\n");
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, enableOffline)).checkAsr(params);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
        printLog("输入参数：" + json);
    }

    /**
     * 点击停止按钮
     * 基于SDK集成4.1 发送停止事件
     */
    private void stop() {
        printLog("停止识别：ASR_STOP");
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }


    private void printLog(String text) {
        if (logTime) {
            text += "  ;time=" + System.currentTimeMillis();
        }
        text += "\n";
        Log.i(getClass().getName(), text);
        txtLog.append(text + "\n");
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }


    @Override
    public void onEvent(String s, String s1, byte[] bytes, int i, int i1) {
        String logTxt = "name: " + s;


        if (s1 != null && !s1.isEmpty()) {
            logTxt += " ;params :" + s1;
        }
        if (s.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            if (s1 != null && s1.contains("\"nlu_result\"")) {
                if (i1 > 0 && bytes.length > 0) {
                    logTxt += ", 语义解析结果：" + new String(bytes, i, i1);
                }
            }
        } else if (bytes != null) {
            logTxt += " ;data length=" + bytes.length;
        }
        printLog(logTxt);

        if (logTxt.contains("颜值打分")) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, FACE);
        } else if (logTxt.contains("这是什么")) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, PICK);
        } else if (logTxt.contains("文字识别")) {
            Intent intent = new Intent(ActivityAllRecog.this, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                    com.gdsgj.textrecognitionspeechsynthesis.FileUtil.getSaveFile(getApplication()).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                    CameraActivity.CONTENT_TYPE_GENERAL);
            startActivityForResult(intent, REQUEST_CODE_GENERAL_BASIC);
        } else if (logTxt.contains("身份证识别")) {
            Intent intent = new Intent(ActivityAllRecog.this, IDCardActivity.class);
            startActivity(intent);
        } else if (logTxt.contains("银行卡识别")) {
            Intent intent = new Intent(ActivityAllRecog.this, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                    com.gdsgj.textrecognitionspeechsynthesis.FileUtil.getSaveFile(getApplication()).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                    CameraActivity.CONTENT_TYPE_BANK_CARD);
            startActivityForResult(intent, REQUEST_CODE_BANKCARD);
        } else if (logTxt.contains("车牌识别")) {
            Intent intent = new Intent(ActivityAllRecog.this, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                    com.gdsgj.textrecognitionspeechsynthesis.FileUtil.getSaveFile(getApplication()).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                    CameraActivity.CONTENT_TYPE_GENERAL);
            startActivityForResult(intent, REQUEST_CODE_LICENSE_PLATE);
        } else if (logTxt.contains("车型识别")) {
            Intent intent = new Intent(ActivityAllRecog.this, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, com.gdsgj.textrecognitionspeechsynthesis.FileUtil.getSaveFile(getApplication()).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
            startActivityForResult(intent, REQUEST_CODE_CAR_MODEL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 表示 调用照相机拍照
            case PICK:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");

                    recogIv.setImageBitmap(bitmap);

                    if (data.getData() != null) {
                        imageUri = data.getData();
                    } else {
                        imageUri = Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, null, null));
                    }
                    String imgString = bitmapToBase64(bitmap);
                    Log.i(TAG, "onActivityResult: image 本地路径" + imgString);
                    uploadImg(imgString);
                }
                break;


            case FACE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");

                    recogIv.setImageBitmap(bitmap);

//                    if (data.getData() != null) {
//                        imageUri = data.getData();
//                    } else {
//                        imageUri = Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, null, null));
//                    }
                    String imgString = bitmapToBase64(bitmap);
                    Log.i(TAG, "onActivityResult: face 本地路径" + imgString);

                    byte[] bitmapByte = getBitmapByte(bitmap);


                    String encode = Base64Util.encode(bitmapByte);
                    Log.i(TAG, "onActivityResult: face encode" + encode);
                    faceImage(imgString);
                }
                break;

            default:
                break;
        }


        // 识别成功回调，通用文字识别
        if (requestCode == REQUEST_CODE_GENERAL_BASIC && resultCode == Activity.RESULT_OK) {
            RecognizeService.recGeneralBasic(this, com.gdsgj.textrecognitionspeechsynthesis.FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            Log.i(TAG, "onResult:  == " + getUniverstalTextJsonBean(result));
                            infoPopText("识别结果,查看以下内容", getUniverstalTextJsonBean(result));
                            speak("文字识别结果是：" + getUniverstalTextJsonBean(result));
                        }
                    });
        }


        // 识别成功回调，银行卡识别
        if (requestCode == REQUEST_CODE_BANKCARD && resultCode == Activity.RESULT_OK) {
            RecognizeService.recBankCard(this, com.gdsgj.textrecognitionspeechsynthesis.FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            infoPopText("识别结果,查看以下内容", result);
                            Log.i(TAG, "onResult: 银行卡result" + result);
                            speak("银行卡识别结果是：" + result + "   ");
                        }
                    });
        }

        // 识别成功回调，车牌识别
        if (requestCode == REQUEST_CODE_LICENSE_PLATE && resultCode == Activity.RESULT_OK) {
            RecognizeService.recLicensePlate(this, com.gdsgj.textrecognitionspeechsynthesis.FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            infoPopText("车牌识别结果", getLicensePlateJsonBean(result));
                            speak("车牌识别结果是：" + getLicensePlateJsonBean(result));
                        }
                    });
        }

        //识别车型回调
        if (requestCode == REQUEST_CODE_CAR_MODEL && resultCode == Activity.RESULT_OK) {
            final String absolutePath = com.gdsgj.textrecognitionspeechsynthesis.FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
            try {
                FileInputStream fileInputStream = new FileInputStream(absolutePath);
                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                final byte[] bitmap2Bytes = bitmap2Bytes(bitmap);

                recogIv.setImageBitmap(bitmap);

                //提示扫描中
                scan_value_tv.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AipImageClassify classify = new AipImageClassify(appId, Const.appKey, secretKey);
                        classify.setConnectionTimeoutInMillis(2000);
                        classify.setSocketTimeoutInMillis(6000);
                        JSONObject vaule = classify.carDetect(bitmap2Bytes, new HashMap<String, String>());
                        Message message = Message.obtain();
                        message.what = REQUEST_CODE_CAR_MODEL;
                        message.obj = vaule;
                        handler.sendMessage(message);
                    }
                }).start();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case REQUEST_CODE_CAR_MODEL:
                    JSONObject carObject = (JSONObject) msg.obj;
                    JSONArray carJsonArray = null;
                    try {
                        carJsonArray = new JSONArray(carObject.optString("result"));
                        String name4 = carJsonArray.optJSONObject(0).optString("name");
                        String score4 = carJsonArray.optJSONObject(0).optString("score");

                        String[] mitems4 = {"该车型是：" + name4, "可能性：" + score4};
                        Log.i(TAG, "handleMessage: mitems4 car ==" + "名称：" + name4 + "可能性：" + score4);
                        scan_value_tv.setVisibility(View.GONE);
                        AlertDialog.Builder alertDialog4 = new AlertDialog.Builder(ActivityAllRecog.this);
                        alertDialog4.setTitle("识别报告").setItems(mitems4, null).create().show();

                        speak("识别报告：该车型是 " + name4);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

               /* case REQUEST_CODE_SMIILS:
                    JSONObject smailsObject = (JSONObject) msg.obj;
                    face_result=smailsObject.optString("result_num");
                    Log.i(TAG, "handleMessage: faceresult ==" + face_result);
                    int i = Integer.parseInt(face_result);
                    if(*//*Integer.parseInt(face_result.toString())*//*i>=1) {
                        try {
                            JSONArray js = new JSONArray(smailsObject.optString("result"));
                            face_age = js.optJSONObject(0).optString("age");
                            face_gender = js.optJSONObject(0).optString("gender");
                            if (face_gender.equals("female")) {
                                face_gender = "女";
                            } else {
                                face_gender = "男";
                            }
                            face_race = js.optJSONObject(0).optString("race");
                            if (face_race.equals("yellow")) {
                                face_race = "黄种人";
                            } else if (face_race.equals("white")) {
                                face_race = "白种人";
                            } else if (face_race.equals("black")) {
                                face_race = "黑种人";
                            }else if(face_race.equals("arabs")){
                                face_race = "阿拉伯人";
                            }
                            int express = Integer.parseInt(js.optJSONObject(0).optString("expression"));
                            if (express == 0) {
                                face_expression = "无";
                            } else if (express == 1) {
                                face_expression = "微笑";
                            } else {
                                face_expression = "大笑";
                            }
                            face_beauty = js.optJSONObject(0).optString("beauty");
                            double  beauty=Math.ceil(Double.parseDouble(face_beauty)+25);
                            if(beauty>=100){
                                beauty=99.0;
                            }
                            else if(beauty<70){
                                beauty+=10;
                            }
                            else if(beauty>80 && beauty<90){
                                beauty+=5;
                            }
                            else if(beauty>=90 && beauty<95){
                                beauty+=2;
                            }
                            face_beauty=String.valueOf(beauty);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        AlertDialog.Builder alertDialog5 = new AlertDialog.Builder(MainActivity.this);
                        String[] mItems5 = {"性别：" + face_gender, "年龄：" + face_age, "肤色：" + face_race, "颜值：" + face_beauty, "笑容：" + face_expression};
                        alertDialog5.setTitle("人脸识别报告").setItems(mItems5, null).create().show();
                        scan_value_tv.setVisibility(View.GONE);
                    }else{
                        AlertDialog.Builder alertDialog5 = new AlertDialog.Builder(MainActivity.this);
                        alertDialog5.setTitle("人脸识别报告").setMessage("图片不够清晰，请重新选择").create().show();
                    }
                    break;*/

                default:
                    break;
            }


        }
    };

    //颜值打分
    public void faceImage(String imgString) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        String filePath = imgString;
        byte[] imgData = new byte[0];
        try {
            imgData = FileUtil.readFileByBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String imgStr = Base64Util.encode(imgData);
        try {
            imgParam = URLEncoder.encode(imgStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
        String accessToken = "24.621c25b8187380829ddf0e7f4701dec6.2592000.1576169171.282335-17721709";

        FormBody body = new FormBody.Builder().add("access_token", accessToken).add("image", imgString).add("face_field", "age,beauty,expression").add("image_type", "BASE64").build();

        Request request = new Request.Builder().url(faceUrl).post(body).build();
//
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String data = response.body().string();
                Log.i(TAG, "onResponse: face result data==" + data);
                Gson gson = new Gson();
                FaceBeanClass faceBean = gson.fromJson(data, FaceBeanClass.class);
                FaceBeanClass.ResultBean result = faceBean.getResult();
                List<FaceBeanClass.ResultBean.FaceListBean> resultFace_list = result.getFace_list();
                for (int i = 0; i < resultFace_list.size(); i++) {
                    FaceBeanClass.ResultBean.FaceListBean faceListBean = resultFace_list.get(i);
                    int age = faceListBean.getAge();
                    double beauty = faceListBean.getBeauty();

                    Log.i(TAG, "onResponse: age ==" + age);
                    Log.i(TAG, "onResponse: beauty ==" + beauty);

                    if (beauty >= 90.0) {
                        resultTv.setText("该照片颜值是 :" + String.valueOf(beauty) + "\r\n" + " 简直就是沉鱼落雁，前无古人后无来者的颜值 \r\n（(❤ ω ❤)" + "\r\n" + "该照片的人年龄是 :" + age);
                    } else if (beauty >= 70.0) {
                        resultTv.setText("该照片颜值是 :" + String.valueOf(beauty) + "\r\n" + " 古代妃子皇子就是你把\r\nψ(._. )>" + "\r\n" + "该照片的人年龄是 :" + age);
                    } else if (beauty >= 60.0) {
                        resultTv.setText("该照片颜值是 :" + String.valueOf(beauty) + "\r\n" + " 真令人惊讶呢，还需要好好打扮自己哦\r\n(。・∀・)ノ" + "\r\n" + "该照片的人年龄是 :" + age);
                    } else if (beauty >= 50.0) {
                        resultTv.setText("该照片颜值是 :" + String.valueOf(beauty) + "\r\n" + " 你看，那猪在天上飞!\r\n( ఠൠఠ )ﾉ" + "\r\n" + "该照片的人年龄是 :" + age);
                    } else {
                        resultTv.setText("该照片颜值是 :" + String.valueOf(beauty) + "\r\n" + " 哇，长得丑不是你的错，出来吓人就是你不对了\r\n(　o=^•ェ•)o　┏━┓" + "\r\n" + "该照片的人年龄是 :" + age);
                    }
//                        Log.i(TAG, "onResponse: expressionType ==" + expressionType);

                }


            }

        });

    }


    //上传图片文件的操作
    public void uploadImg(String imgString) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        //上传图片参数需要与服务端沟通，我就不多做解释了，我添加的都是我们服务端需要的
        //你们根据情况自行更改
        //另外网络请求我就不多做解释了
        String filePath = imgString;
        byte[] imgData = new byte[0];
        try {
            imgData = FileUtil.readFileByBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String imgStr = Base64Util.encode(imgData);
        try {
            imgParam = URLEncoder.encode(imgStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
        String accessToken = "24.621c25b8187380829ddf0e7f4701dec6.2592000.1576169171.282335-17721709";

        FormBody body = new FormBody.Builder().add("access_token", accessToken).add("image", imgString).add("baike_num", "5").build();

        Request request = new Request.Builder().url(imageclassifyUrl).post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String data = response.body().string();
                Gson gson = new Gson();
                final Beans bean = gson.fromJson(data, Beans.class);
                List<Beans.ResultBean> result = bean.getResult();
                Log.d(TAG, "onResponse: " + data);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray resultl = jsonObject.getJSONArray("result");
                    JSONObject resultlJSONObject = resultl.getJSONObject(0);
                    String keyword = resultlJSONObject.getString("keyword");


                    JSONObject baike_info = resultlJSONObject.getJSONObject("baike_info");


                    Log.i(TAG, "onResponse: keyword ==" + keyword);
                    if (baike_info.equals("{}")) {
                        resultTv.setText("这个可能是 :" + keyword + "\r\n");
                    } else {
                        String description = baike_info.getString("description");
                        resultTv.setText("这个可能是 :" + keyword + "\n" + "该商品的百科是：" + description);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        Log.i("ActivityMiniRecog", "On pause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 基于SDK集成4.2 发送取消事件
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);


        // 基于SDK集成5.2 退出事件管理器
        // 必须与registerListener成对出现，否则可能造成内存泄露
        asr.unregisterListener(this);
    }

    //如上参需要64位编码可调用此方法，不需要可以忽略
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * 通用文字识别解析bean,返回结果list
     *
     * @param json 传入一个json
     * @return
     */
    private String getUniverstalTextJsonBean(String json) {
        StringBuilder words = new StringBuilder();
        UniversalTextRecognitionBean beans = GsonImpl.get().toObject(json, UniversalTextRecognitionBean.class);
        List<UniversalTextRecognitionBean.WordsResultBean> words_result = beans.getWords_result();
        for (int i = 0; i < words_result.size(); i++) {
            UniversalTextRecognitionBean.WordsResultBean wordsResultBean = words_result.get(i);
            words.append(wordsResultBean.getWords()).append(",");

        }
        return words.toString();
    }


    private void infoPopText(String title, final String result) {
        alertText(title, result);
    }

    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSpeechSynthesizer.stop();
                            }
                        })
                        .show();
            }
        });
    }

    //设置合成语音的listener
    SpeechSynthesizerListener speechSynthesizerListener = new SpeechSynthesizerListener() {

        private String destDir = application.getOutputDir().getPath();

        @Override
        public void onSynthesizeStart(String s) {
            //合成开始
            Log.i(TAG, "speechSynthesizerListener onSynthesizeStart");

            if (isNeedSaveTTS) {
                String filename = getTimeStampLocal() + ".pcm";
                // 保存的语音文件是 16K采样率 16bits编码 单声道 pcm文件。
                File ttsFile = new File(destDir, filename);
                try {
                    if (ttsFile.exists()) {
                        ttsFile.delete();
                    }
                    ttsFile.createNewFile();
                    FileOutputStream ttsFileOutputStream = new FileOutputStream(ttsFile);
                    ttsFileBufferedOutputStream = new BufferedOutputStream(ttsFileOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSynthesizeDataArrived(String s, byte[] data, int i) {
            // 合成过程中的数据回调接口
            Log.i(TAG, "speechSynthesizerListener onSynthesizeDataArrived s=" + s);

            if (isNeedSaveTTS) {
                try {
                    ttsFileBufferedOutputStream.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSynthesizeFinish(String s) {
            // 合成结束
            Log.i(TAG, "speechSynthesizerListener onSynthesizeFinish s=" + s);

            if (isNeedSaveTTS)
                close();
        }

        @Override
        public void onSpeechStart(String s) {
            // 播放开始
            Log.i(TAG, "speechSynthesizerListener onSpeechStart s=" + s);
        }

        @Override
        public void onSpeechProgressChanged(String s, int i) {
            // 播放过程中的回调
            Log.i(TAG, "onSpeechProgressChanged onSpeechProgressChanged s=" + s);
        }

        @Override
        public void onSpeechFinish(String s) {
            // 播放结束
            Log.i(TAG, "onSpeechProgressChanged onSpeechFinish s=" + s);
        }

        @Override
        public void onError(String s, SpeechError speechError) {
            // 合成和播放过程中出错时的回调
            Log.e(TAG, "onSpeechProgressChanged onError s=" + s + " error=" + speechError.toString());
            if (isNeedSaveTTS)
                close();
        }
    };

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {

        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);

        // 2. 设置listener
        mSpeechSynthesizer.setSpeechSynthesizerListener(speechSynthesizerListener);

        // 3. 设置appId，appKey.secretKey
        mSpeechSynthesizer.setAppId(Const.appId);
        mSpeechSynthesizer.setApiKey(Const.appKey, Const.secretKey);

        // 4. 支持离线的话，需要设置离线模型
        if (ttsMode.equals(TtsMode.MIX)) {
            // 检查离线授权文件是否下载成功，离线授权文件联网时SDK自动下载管理，有效期3年，3年后的最后一个月自动更新。
            if (!checkAuth()) {
                return;
            }
            // 文本模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, application.getTextModeFile());
            // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, application.getSpeechModeFile());
        }

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "4");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "4");
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // 设置播放器的音频流类型
        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_CURRENT);
        // 不使用压缩传输
        // mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE, SpeechSynthesizer.AUDIO_ENCODE_PCM);
        // mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_RATE, SpeechSynthesizer.AUDIO_BITRATE_PCM);

        // 6. 初始化
        mSpeechSynthesizer.initTts(ttsMode);
    }


    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return
     */
    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.e(TAG, "checkAuth: errorMSG" + errorMsg);
            return false;
        } else {
            return true;
        }
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

        int result = mSpeechSynthesizer.speak(text);
        checkResult(result, "speak");
    }

    public String getTimeStampLocal() {
        Date d = new Date();
        @SuppressLint("SimpleDateFormat")
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return format.format(d);
    }

    private void close() {
        if (ttsFileBufferedOutputStream != null) {
            try {
                ttsFileBufferedOutputStream.flush();
                ttsFileBufferedOutputStream.close();
                ttsFileBufferedOutputStream = null;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (ttsFileOutputStream != null) {
            try {
                ttsFileOutputStream.close();
                ttsFileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 解析车牌号码json的方法
     *
     * @param json
     * @return
     */
    private String getLicensePlateJsonBean(String json) {
        LicensePlateBean licensePlateBean = GsonImpl.get().toObject(json, LicensePlateBean.class);
        LicensePlateBean.WordsResultBean words_result = licensePlateBean.getWords_result();
        return words_result.getNumber();
    }

    //假设语音合成错误，返回的结果
    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.i("MainActivity", "error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    /**
     * 把Bitmap转Byte
     *
     * @Author HEH
     * @EditTime 2010-07-19 上午11:45:56
     */
    public byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

}
