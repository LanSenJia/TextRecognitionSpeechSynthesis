package com.baidu.aip.asrwakeup3.uiasr;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author lansenboy
 * @date 2019/11/11.
 * GitHub：
 * email：
 * description：
 */
public class AuthService {

    /**
     * 获取权限token
     *
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    public static String getAuth() {
        // 官网获取的 API Key 更新为你注册的
        String clientId = "4gjVTa9Q6Mjkzya9hEc6OYEQ";
        // 官网获取的 Secret Key 更新为你注册的
        String clientSecret = "8o5VRVhosQOoRpTL5GKTCdhmxDWijIL7";
        return getAuth(clientId, clientSecret);
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     *
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public static String getAuth(String ak, String sk) {
        // 获取token地址
        StringBuffer authHost = new StringBuffer();
        authHost.append("https://aip.baidubce.com/oauth/2.0/token?")
                .append("grant_type=client_credentials").append("&client_id=").append(ak).append("&client_secret=").append(sk);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
// 指定访问的服务器地址是电脑本机
                            .url(authHost.toString())
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.i("result auth", "run: re"+responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return null;
    }


}
