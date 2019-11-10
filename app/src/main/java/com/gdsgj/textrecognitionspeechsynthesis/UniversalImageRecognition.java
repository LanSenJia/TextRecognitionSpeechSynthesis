package com.gdsgj.textrecognitionspeechsynthesis;

import com.baidu.aip.imageclassify.AipImageClassify;

import org.json.JSONObject;

import java.util.HashMap;

public class UniversalImageRecognition {

    public static void main(String[] args) {
        // 初始化一个AipImageClassify
        AipImageClassify client = new AipImageClassify(Const.appId, Const.appKey, Const.secretKey);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

        // 调用接口
        String path = "test.jpg";
        JSONObject res = client.objectDetect(path, new HashMap<String, String>());
//        System.out.println(res.toString(2));

    }
}
