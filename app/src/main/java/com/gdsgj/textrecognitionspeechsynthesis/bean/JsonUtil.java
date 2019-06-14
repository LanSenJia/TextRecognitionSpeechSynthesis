package com.gdsgj.textrecognitionspeechsynthesis.bean;

import java.util.List;

/*
 * @Title:
 * @Copyright:  GuangZhou F.R.O Electronic Technology Co.,Ltd. Copyright 2006-2016,  All rights reserved
 * @Descrplacetion:  ${TODO}<Json工具类>
 * @author:  lansenboy
 * @data: 2019/6/9
 * @version:  V1.0
 * @OfficialWebsite: http://www.frotech.com/
 */
public abstract class JsonUtil {


    private static JsonUtil jsonUtil;

    JsonUtil() {
    }

    public static JsonUtil get() {
        if (jsonUtil == null) {
            jsonUtil = new GsonImpl();
        }
        return jsonUtil;
    }

    public abstract String toJson(Object src);

    public abstract <T> T toObject(String json, Class<T> claxx);

    public abstract <T> T toObject(byte[] bytes, Class<T> claxx);

    public abstract <T> List<T> toList(String json, Class<T> claxx);

}
