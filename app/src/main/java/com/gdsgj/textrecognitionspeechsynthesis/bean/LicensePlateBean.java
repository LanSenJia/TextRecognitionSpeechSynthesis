package com.gdsgj.textrecognitionspeechsynthesis.bean;

/*
 * @Title:
 * @Copyright:  GuangZhou F.R.O Electronic Technology Co.,Ltd. Copyright 2006-2016,  All rights reserved
 * @Descrplacetion:  ${TODO}<请描述此文件是做什么的>
 * @author:  lansenboy
 * @data: 2019/6/10
 * @version:  V1.0
 * @OfficialWebsite: http://www.frotech.com/
 */
public class LicensePlateBean {

    /**
     * words_result : {"color":"blue","number":"粤FQ0000"}
     * log_id : 2783673432
     */

    private WordsResultBean words_result;
    private long log_id;

    public WordsResultBean getWords_result() {
        return words_result;
    }

    public void setWords_result(WordsResultBean words_result) {
        this.words_result = words_result;
    }

    public long getLog_id() {
        return log_id;
    }

    public void setLog_id(long log_id) {
        this.log_id = log_id;
    }

    public static class WordsResultBean {
        /**
         * color : blue
         * number : 粤FQ0000
         */

        private String color;
        private String number;

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }
    }
}
