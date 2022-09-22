package com.pgyer.app.upload.bean;

import com.google.gson.annotations.SerializedName;

public class PgyerTokenBean {

    private int code;
    private String message;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {

        private String key;
        private String endpoint;
        private Params params;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Params getParams() {
            return params;
        }

        public void setParams(Params params) {
            this.params = params;
        }
    }

    public static class Params {
        private String key;
        private String signature;
        @SerializedName("x-cos-security-token")
        private String x_cos_security_token;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getX_cos_security_token() {
            return x_cos_security_token;
        }

        public void setX_cos_security_token(String x_cos_security_token) {
            this.x_cos_security_token = x_cos_security_token;
        }
    }
}
