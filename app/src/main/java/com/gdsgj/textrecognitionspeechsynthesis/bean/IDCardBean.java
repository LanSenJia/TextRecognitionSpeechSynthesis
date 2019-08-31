package com.gdsgj.textrecognitionspeechsynthesis.bean;

/*
 * @date 2019/6/22.
 * description：
 * version: 1.0
 */
public class IDCardBean {


    /**
     * log_id : 7037721
     * direction : 0
     * words_result_num : 2
     * words_result : {"住址":{"location":{"left":227,"top":235,"width":229,"height":51},"words":"湖北省天门市渔薪镇杨咀村一组2号"}}
     */

    private int log_id;
    private int direction;
    private int words_result_num;
    private WordsResultBean words_result;

    public int getLog_id() {
        return log_id;
    }

    public void setLog_id(int log_id) {
        this.log_id = log_id;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getWords_result_num() {
        return words_result_num;
    }

    public void setWords_result_num(int words_result_num) {
        this.words_result_num = words_result_num;
    }

    public WordsResultBean getWords_result() {
        return words_result;
    }

    public void setWords_result(WordsResultBean words_result) {
        this.words_result = words_result;
    }

    public static class WordsResultBean {
        /**
         * 住址 : {"location":{"left":227,"top":235,"width":229,"height":51},"words":"湖北省天门市渔薪镇杨咀村一组2号"}
         */

        private 住址Bean 住址;

        public 住址Bean get住址() {
            return 住址;
        }

        public void set住址(住址Bean 住址) {
            this.住址 = 住址;
        }

        public static class 住址Bean {
            /**
             * location : {"left":227,"top":235,"width":229,"height":51}
             * words : 湖北省天门市渔薪镇杨咀村一组2号
             */

            private LocationBean location;
            private String words;

            public LocationBean getLocation() {
                return location;
            }

            public void setLocation(LocationBean location) {
                this.location = location;
            }

            public String getWords() {
                return words;
            }

            public void setWords(String words) {
                this.words = words;
            }

            public static class LocationBean {
                /**
                 * left : 227
                 * top : 235
                 * width : 229
                 * height : 51
                 */

                private int left;
                private int top;
                private int width;
                private int height;

                public int getLeft() {
                    return left;
                }

                public void setLeft(int left) {
                    this.left = left;
                }

                public int getTop() {
                    return top;
                }

                public void setTop(int top) {
                    this.top = top;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }
            }
        }
    }
}
