package com.gdsgj.textrecognitionspeechsynthesis.bean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/*
 * @Title:
 * @Copyright:  GuangZhou F.R.O Electronic Technology Co.,Ltd. Copyright 2006-2016,  All rights reserved
 * @Descrplacetion:  ${TODO}<请描述此文件是做什么的>
 * @author:  lansenboy
 * @data: 2019/6/9
 * @version:  V1.0
 * @OfficialWebsite: http://www.frotech.com/
 */
public class GsonImpl extends JsonUtil {
    private Gson gson = new Gson();
    @Override
    public String toJson(Object src) {
        return gson.toJson(src);
    }
    @Override
    public <T> T toObject(String json, Class<T> claxx) {
        return gson.fromJson(json, claxx);
    }
    @Override
    public <T> T toObject(byte[] bytes, Class<T> claxx) {
        return gson.fromJson(new String(bytes), claxx);
    }
    @Override
    public <T> List<T> toList(String json, Class<T> claxx) {
        Type type = new TypeToken<ArrayList<T>>() {}.getType();
        List<T> list = gson.fromJson(json, type);
        return list;
    }
}
