package com.baidu.aip.asrwakeup3.uiasr.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.aip.asrwakeup3.uiasr.Base64Util;
import com.baidu.aip.asrwakeup3.uiasr.FaceBeanClass;
import com.baidu.aip.asrwakeup3.uiasr.FileUtil;
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
import java.util.ArrayList;
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

    private final int PICK = 1;
    private final int IMAGE_RESULT_CODE = 2;
    private final int FACE = 3;
    private String imageclassifyUrl = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general";//
    private String faceUrl = "https://aip.baidubce.com/rest/2.0/face/v3/detect";
    private Handler mHandler = new Handler(Looper.getMainLooper());

    // 声明一个数组，用来存储所有需要动态申请的权限。这里写的是同时申请多条权限，如果你只申请一条那么你就在数组里写一条权限好了
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA};
    // 同时我们可以声明一个集合，用来存储用户拒绝授权的权限。
    List<String> mPermissionList = new ArrayList<>();

    /**
     * 控制UI按钮的状态
     */
    protected int status;

    /**
     * 日志使用
     */
    private static final String TAG = "ActivityUiRecog";
    private ImageView recogIv;
    private String imgParam;
    private Uri imageUri;
    private File outputImage;
    private TextView resultTv;
    private String filePath;


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
                        txtResult.setText("语音识别结果： "+subStr);
                        Log.i(TAG, "handleMsg: msg识别结果" + subStr);
                        String whatis = "这是什么";
                        String faceis = "颜值打分";
                        String faceis2 = "人脸打分";
                        String whatis_keyword = "这个是什么";
                        if (whatis.contains(subStr) || whatis_keyword.contains(subStr)) {
                            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK);

                        } else if (faceis.equals(subStr)|| faceis2.contains(subStr)) {
                            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, FACE);
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
            // 选择图片库的图片
            case IMAGE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Bitmap bitmap2 = PhotoUtils.getBitmapFromUri(uri, this);
                    String imgString = bitmapToBase64(bitmap2);
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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(ActivityUiRecog.this, permissions[i]);
                        if (showRequestPermission) {
                            //这里可以做相关操作，我这里是写的是重新申请权限
                            checkPermission();//重新申请权限
                            return;
                        } else {
                            //做相关操作。。。
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    //颜值打分
    public void faceImage(String imgString) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        filePath = imgString;
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

    //检查权限
    private void checkPermission() {
        mPermissionList.clear();
        /**
         *PackageManager.PERMISSION_GRANTED 表示有权限， PackageManager.PERMISSION_DENIED 表示无权限。
         * 判断哪些权限未授予
         * 以便必要的时候重新申请
         */
        for (String permission : permissions) {
            //判断所要申请的权限是否已经授权
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        /**
         * 判断存储委授予权限的集合是否为空
         */
        if (!mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(ActivityUiRecog.this, permissions, 1);//请求指定授权
        } else {//未授予的权限为空，表示都授予了
        }
    }


    /**
     * 读取文件里面的字符串
     *
     * @param fileName
     * @return
     */
    private String readFile(String fileName) {
        String result = null;
        try {
            InputStream inputStream = openFileInput(fileName);

            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            result = new String(bytes);

            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            Log.d(TAG, "toURLEncoded: " + paramString);
            return "";
        }

        try {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLEncoder.encode(str, "UTF-8");
            return str;
        } catch (Exception localException) {
            Log.e(TAG, "toURLEncoded: " + paramString, localException);
        }

        return "";
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

}




