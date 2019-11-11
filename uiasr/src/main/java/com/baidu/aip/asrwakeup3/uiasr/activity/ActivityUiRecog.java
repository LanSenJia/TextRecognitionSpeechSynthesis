package com.baidu.aip.asrwakeup3.uiasr.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.aip.asrwakeup3.uiasr.AuthService;
import com.baidu.aip.asrwakeup3.uiasr.Base64Util;
import com.baidu.aip.asrwakeup3.uiasr.FileUtil;
import com.baidu.aip.asrwakeup3.uiasr.HttpUtil;
import com.baidu.aip.asrwakeup3.uiasr.PhotoUtils;
import com.baidu.aip.asrwakeup3.uiasr.R;
import com.baidu.aip.asrwakeup3.uiasr.params.AllRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.CommonRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.NluRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OfflineRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.params.OnlineRecogParams;
import com.baidu.aip.asrwakeup3.uiasr.setting.AllSetting;
import com.baidu.aip.asrwakeup3.uiasr.setting.NluSetting;
import com.baidu.aip.asrwakeup3.uiasr.setting.OfflineSetting;
import com.baidu.aip.asrwakeup3.uiasr.setting.OnlineSetting;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 识别的基类Activity。 ActivityCommon定义了通用的UI部分
 * 封装了识别的大部分逻辑，包括MyRecognizer的初始化，资源释放
 * <p>
 * <p>
 * 集成流程代码，只需要一句： myRecognizer.start(params);具体示例代码参见startRough()
 * =》.实例化 myRecognizer   new MyRecognizer(this, listener);
 * =》 实例化 listener  new MessageStatusRecogListener(null);
 * </p>
 * 集成文档： http://ai.baidu.com/docs#/ASR-Android-SDK/top 集成指南一节
 * demo目录下doc_integration_DOCUMENT
 * ASR-INTEGRATION-helloworld  ASR集成指南-集成到helloworld中 对应 ActivityMiniRecog
 * ASR-INTEGRATION-TTS-DEMO ASR集成指南-集成到合成DEMO中 对应 ActivityUiRecog
 * <p>
 * 大致流程为
 * 1. 实例化MyRecognizer ,调用release方法前不可以实例化第二个。参数中需要开发者自行填写语音识别事件的回调类，实现开发者自身的业务逻辑
 * 2. 如果使用离线命令词功能，需要调用loadOfflineEngine。在线功能不需要。
 * 3. 根据识别的参数文档，或者demo中测试出的参数，组成json格式的字符串。调用 start 方法
 * 4. 在合适的时候，调用release释放资源。
 * <p>
 */

public abstract class ActivityUiRecog extends ActivityCommon implements IStatus {

    /*
     * Api的参数类，仅仅用于生成调用START的json字符串，本身与SDK的调用无关
     */
    private final CommonRecogParams apiParams;

    private final Class settingActivityClass;

    private final int IMAGE_RESULT_CODE = 2;
    private final int PICK = 1;
    private String url = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general";//
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 控制UI按钮的状态
     */
    protected int status;

    /**
     * 日志使用
     */
    private static final String TAG = "ActivityUiRecog";
    private String imgString;
    private ImageView recogIv;
    private String imgParam;
    private Uri imageUri;
    private File outputImage;
    private Bitmap bitmap;
    private TextView resultTv;


    /**
     * 开始录音，点击“开始”按钮后调用。
     */
    protected abstract void start();


    /**
     * 开始录音后，手动停止录音。SDK会识别在此过程中的录音。点击“停止”按钮后调用。
     */
    protected abstract void stop();

    /**
     * 开始录音后，取消这次录音。SDK会取消本次识别，回到原始状态。点击“取消”按钮后调用。
     */
    protected abstract void cancel();

    protected boolean running = false;

    public ActivityUiRecog(int textId) {
        super(textId);
        String className = getClass().getSimpleName();
        if (className.equals("ActivityOnlineRecog") || className.equals("ActivityUiDialog")) {
            settingActivityClass = OnlineSetting.class;
            apiParams = new OnlineRecogParams();
        } else if (className.equals("ActivityOfflineRecog")) {
            settingActivityClass = OfflineSetting.class;
            apiParams = new OfflineRecogParams();
        } else if (className.equals("ActivityNlu")) {
            settingActivityClass = NluSetting.class;
            apiParams = new NluRecogParams();
        } else if (className.equals("ActivityAllRecog")) {
            settingActivityClass = AllSetting.class;
            apiParams = new AllRecogParams();
        } else {
            throw new RuntimeException("PLEASE DO NOT RENAME DEMO ACTIVITY, current name:" + className);
        }
    }

    protected Map<String, Object> fetchParams() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //  上面的获取是为了生成下面的Map， 自己集成时可以忽略
        Map<String, Object> params = apiParams.fetch(sp);
        //  集成时不需要上面的代码，只需要params参数。
        return params;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiParams.initSamplePath(this);
        recogIv = findViewById(R.id.recog_iv);
        resultTv = findViewById(R.id.result_tv);

        Message message = new Message();
        message.what = 1;
        mHandler.sendMessage(message);
    }

    @Override
    protected void initView() {
        super.initView();
        status = STATUS_NONE;
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (status) {
                    case STATUS_NONE: // 初始状态
                        start();
                        status = STATUS_WAITING_READY;
                        updateBtnTextByStatus();
                        txtLog.setText("");
                        txtResult.setText("");
                        break;
                    case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                    case STATUS_READY: // 引擎准备完毕。
                    case STATUS_SPEAKING: // 用户开始讲话
                    case STATUS_FINISHED: // 一句话识别语音结束
                    case STATUS_RECOGNITION: // 识别中
                        stop();
                        status = STATUS_STOPPED; // 引擎识别中
                        updateBtnTextByStatus();
                        break;
                    case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
                    case STATUS_STOPPED: // 引擎识别中
                        cancel();
                        status = STATUS_NONE; // 识别结束，回到初始状态
                        updateBtnTextByStatus();
                        break;
                    default:
                        break;
                }

            }
        });
        if (setting != null && settingActivityClass != null) {
            setting.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    running = true; // 是否该Activity依旧需要运行
                    Intent intent = new Intent(ActivityUiRecog.this, settingActivityClass);
                    startActivityForResult(intent, 1);
                }
            });

        }
    }

    protected void handleMsg(Message msg) {
        super.handleMsg(msg);

        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
                if (msg.arg2 == 1) {
//                    txtResult.setText(msg.obj.toString());
                    if (msg.obj.toString().length() != 0) {
                        txtLog.setVisibility(View.GONE);
                        String subStr = msg.obj.toString().substring(9);
                        subStr = subStr.substring(0, msg.obj.toString().indexOf("；"));
                        subStr = subStr.substring(0, subStr.length() - 10);
                        txtResult.setText(subStr);
                        Log.i(TAG, "handleMsg: msg识别结果" + subStr);
                        String whatis = "这是什么";
                        String whatis_keyword = "这个是什么";
                        if (whatis.contains(subStr) || whatis_keyword.contains(subStr)) {
                            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK);
                        }
                    }
                }
                status = msg.what;
                updateBtnTextByStatus();


                break;
            case STATUS_NONE:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                status = msg.what;
                updateBtnTextByStatus();
                break;
            default:
                break;

        }
    }

    private void updateBtnTextByStatus() {
        switch (status) {
            case STATUS_NONE:
                btn.setText("开始录音");
                btn.setEnabled(true);
                setting.setEnabled(true);
                break;
            case STATUS_WAITING_READY:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                btn.setText("停止录音");
                btn.setEnabled(true);
                setting.setEnabled(false);
                break;
            case STATUS_LONG_SPEECH_FINISHED:
            case STATUS_STOPPED:
                btn.setText("取消整个识别过程");
                btn.setEnabled(true);
                setting.setEnabled(false);
                break;
            default:
                break;
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
                    bitmap = (Bitmap) bundle.get("data");

                    recogIv.setImageBitmap(bitmap);

                    if (data.getData() != null) {
                        imageUri = data.getData();
                    } else {
                        imageUri = Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, null, null));
                    }

                    imgString = bitmapToBase64(bitmap);
                    Log.i(TAG, "onActivityResult: 本地路径" + outputImage);
                    uploadImg();
                }
                break;
            // 选择图片库的图片
            case IMAGE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Bitmap bitmap2 = PhotoUtils.getBitmapFromUri(uri, this);
                    imgString = bitmapToBase64(bitmap2);
                    uploadImg();
                }
                break;
        }

    }


    //上传图片文件的操作
    public void uploadImg() {
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
        String param = "image=" + imgString;

        // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
        String accessToken = "24.bf5c874e04876d3de725173a31c93359.2592000.1576019669.282335-17721709";

//        try {
//            String result = HttpUtil.post(url, accessToken, param);
//            Log.i(TAG, "uploadImg: result" + result);
//            txtLog.setText(result);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
        FormBody body = new FormBody.Builder().add("access_token", accessToken).add("image", imgString).add("baike_num", "5").build();

        Request request = new Request.Builder().url(url).post(body).build();

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
                    JSONObject resultlJSONObject= resultl.getJSONObject(0);
                    String keyword = resultlJSONObject.getString("keyword");


                    JSONObject baike_info = resultlJSONObject.getJSONObject("baike_info");


                    Log.i(TAG, "onResponse: keyword ==" + keyword);
                    if (baike_info.equals("{}")){
                        resultTv.setText("这个可能是 :" + keyword  + "\r" );
                    }else {
                        String description = baike_info.getString("description");
                        resultTv.setText("这个可能是 :" + keyword  + "\n" +"该商品的百科是：" + description);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
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


}


