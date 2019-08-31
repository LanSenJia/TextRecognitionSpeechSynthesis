package com.gdsgj.textrecognitionspeechsynthesis.bean;

import java.util.List;

/*
 * @Title:
 * @Copyright:  GuangZhou F.R.O Electronic Technology Co.,Ltd. Copyright 2006-2016,  All rights reserved
 * @Descrplacetion:  ${TODO}<请描述此文件是做什么的>
 * @data: 2019/7/16
 * @version:  V1.0
 * @OfficialWebsite: http://www.frotech.com/
 */
public class SpeechBean {

    /**
     * results_recognition : ["申请"]
     * result_type : final_result
     * best_result : 申请
     * origin_result : {"corpus_no":6714266475373681535,"err_no":0,"result":{"word":["申请"]},"sn":"a1098530-e58d-4349-9615-b52702d17d1d","voice_energy":31407.453125}
     * error : 0
     */

    private String result_type;
    private String best_result;
    private OriginResultBean origin_result;
    private int error;
    private List<String> results_recognition;

    public String getResult_type() {
        return result_type;
    }

    public void setResult_type(String result_type) {
        this.result_type = result_type;
    }

    public String getBest_result() {
        return best_result;
    }

    public void setBest_result(String best_result) {
        this.best_result = best_result;
    }

    public OriginResultBean getOrigin_result() {
        return origin_result;
    }

    public void setOrigin_result(OriginResultBean origin_result) {
        this.origin_result = origin_result;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public List<String> getResults_recognition() {
        return results_recognition;
    }

    public void setResults_recognition(List<String> results_recognition) {
        this.results_recognition = results_recognition;
    }

    public static class OriginResultBean {
        /**
         * corpus_no : 6714266475373681535
         * err_no : 0
         * result : {"word":["申请"]}
         * sn : a1098530-e58d-4349-9615-b52702d17d1d
         * voice_energy : 31407.453125
         */

        private long corpus_no;
        private int err_no;
        private ResultBean result;
        private String sn;
        private double voice_energy;

        public long getCorpus_no() {
            return corpus_no;
        }

        public void setCorpus_no(long corpus_no) {
            this.corpus_no = corpus_no;
        }

        public int getErr_no() {
            return err_no;
        }

        public void setErr_no(int err_no) {
            this.err_no = err_no;
        }

        public ResultBean getResult() {
            return result;
        }

        public void setResult(ResultBean result) {
            this.result = result;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public double getVoice_energy() {
            return voice_energy;
        }

        public void setVoice_energy(double voice_energy) {
            this.voice_energy = voice_energy;
        }

        public static class ResultBean {
            private List<String> word;

            public List<String> getWord() {
                return word;
            }

            public void setWord(List<String> word) {
                this.word = word;
            }
        }
    }
}
