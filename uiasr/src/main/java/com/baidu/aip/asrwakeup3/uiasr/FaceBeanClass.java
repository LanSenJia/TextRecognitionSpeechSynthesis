package com.baidu.aip.asrwakeup3.uiasr;

import java.util.List;

/**
 * @author lansenboy
 * @date 2019/11/12.
 * GitHub：
 * email：
 * description：
 */
public class FaceBeanClass {


    /**
     * error_code : 0
     * error_msg : SUCCESS
     * log_id : 18420145999
     * timestamp : 1573653232
     * cached : 0
     * result : {"face_num":1,"face_list":[{"face_token":"2834368d120ac0c98c21947e7cc7d601","location":{"left":21.34,"top":60.78,"width":66,"height":65,"rotation":5},"face_probability":1,"angle":{"yaw":1.84,"pitch":6.21,"roll":2.83},"age":23,"beauty":52.46,"expression":{"type":"none","probability":1}}]}
     */

    private int error_code;
    private String error_msg;
    private long log_id;
    private int timestamp;
    private int cached;
    private ResultBean result;

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public long getLog_id() {
        return log_id;
    }

    public void setLog_id(long log_id) {
        this.log_id = log_id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getCached() {
        return cached;
    }

    public void setCached(int cached) {
        this.cached = cached;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * face_num : 1
         * face_list : [{"face_token":"2834368d120ac0c98c21947e7cc7d601","location":{"left":21.34,"top":60.78,"width":66,"height":65,"rotation":5},"face_probability":1,"angle":{"yaw":1.84,"pitch":6.21,"roll":2.83},"age":23,"beauty":52.46,"expression":{"type":"none","probability":1}}]
         */

        private int face_num;
        private List<FaceListBean> face_list;

        public int getFace_num() {
            return face_num;
        }

        public void setFace_num(int face_num) {
            this.face_num = face_num;
        }

        public List<FaceListBean> getFace_list() {
            return face_list;
        }

        public void setFace_list(List<FaceListBean> face_list) {
            this.face_list = face_list;
        }

        public static class FaceListBean {
            /**
             * face_token : 2834368d120ac0c98c21947e7cc7d601
             * location : {"left":21.34,"top":60.78,"width":66,"height":65,"rotation":5}
             * face_probability : 1
             * angle : {"yaw":1.84,"pitch":6.21,"roll":2.83}
             * age : 23
             * beauty : 52.46
             * expression : {"type":"none","probability":1}
             */

            private String face_token;
            private LocationBean location;
            private int face_probability;
            private AngleBean angle;
            private int age;
            private double beauty;
            private ExpressionBean expression;

            public String getFace_token() {
                return face_token;
            }

            public void setFace_token(String face_token) {
                this.face_token = face_token;
            }

            public LocationBean getLocation() {
                return location;
            }

            public void setLocation(LocationBean location) {
                this.location = location;
            }

            public int getFace_probability() {
                return face_probability;
            }

            public void setFace_probability(int face_probability) {
                this.face_probability = face_probability;
            }

            public AngleBean getAngle() {
                return angle;
            }

            public void setAngle(AngleBean angle) {
                this.angle = angle;
            }

            public int getAge() {
                return age;
            }

            public void setAge(int age) {
                this.age = age;
            }

            public double getBeauty() {
                return beauty;
            }

            public void setBeauty(double beauty) {
                this.beauty = beauty;
            }

            public ExpressionBean getExpression() {
                return expression;
            }

            public void setExpression(ExpressionBean expression) {
                this.expression = expression;
            }

            public static class LocationBean {
                /**
                 * left : 21.34
                 * top : 60.78
                 * width : 66
                 * height : 65
                 * rotation : 5
                 */

                private double left;
                private double top;
                private int width;
                private int height;
                private int rotation;

                public double getLeft() {
                    return left;
                }

                public void setLeft(double left) {
                    this.left = left;
                }

                public double getTop() {
                    return top;
                }

                public void setTop(double top) {
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

                public int getRotation() {
                    return rotation;
                }

                public void setRotation(int rotation) {
                    this.rotation = rotation;
                }
            }

            public static class AngleBean {
                /**
                 * yaw : 1.84
                 * pitch : 6.21
                 * roll : 2.83
                 */

                private double yaw;
                private double pitch;
                private double roll;

                public double getYaw() {
                    return yaw;
                }

                public void setYaw(double yaw) {
                    this.yaw = yaw;
                }

                public double getPitch() {
                    return pitch;
                }

                public void setPitch(double pitch) {
                    this.pitch = pitch;
                }

                public double getRoll() {
                    return roll;
                }

                public void setRoll(double roll) {
                    this.roll = roll;
                }
            }

            public static class ExpressionBean {
                /**
                 * type : none
                 * probability : 1
                 */

                private String type;
                private int probability;

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }

                public int getProbability() {
                    return probability;
                }

                public void setProbability(int probability) {
                    this.probability = probability;
                }
            }
        }
    }
}
