package com.pgyer.app.upload.net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pgyer.app.upload.bean.ParamsBean;
import com.pgyer.app.upload.bean.PgyerBean;
import com.pgyer.app.upload.bean.PgyerTokenBean;
import com.pgyer.app.upload.impl.Message;
import com.pgyer.app.upload.utils.CommonUtil;
import hudson.EnvVars;
import okhttp3.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PgyerUpload {
    private static final String UPLOAD_URL = CommonUtil.PGYER_HOST + "/apiv2/app/getCOSToken";
    public static void main(String[] args) {

        Message listener = new Message() {
            @Override
            public void message(boolean needTag, String mesage) {
                System.out.println((needTag ? CommonUtil.LOG_PREFIX : "") + mesage);
            }
        };

        CommonUtil.printHeaderInfo(listener);
        ParamsBean paramsBean = parseArgs(args, listener);
        if (paramsBean == null) {
            return;
        }
        upload2Pgyer(null, false, paramsBean, listener);
    }

    /**
     * parse args
     *
     * @param args
     * @param listener
     * @return
     */
    private static ParamsBean parseArgs(String[] args, Message listener) {
        // check args length
        int length = args.length;

        if (length == 0 || length % 2 != 0) {
            CommonUtil.printMessage(listener, true, "args length is error!\n");
            return null;
        }

        // args to map
        Map<String, String> maps = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            maps.put(args[i], args[i + 1]);
        }

        // check apiKey
        if (!maps.containsKey("-apiKey")) {
            CommonUtil.printMessage(listener, true, "apiKey not found!\n");
            return null;
        }
        // check scanDir
        if (!maps.containsKey("-scanDir")) {
            CommonUtil.printMessage(listener, true, "scanDir not found!\n");
            return null;
        }
        // check wildcard
        if (!maps.containsKey("-wildcard")) {
            CommonUtil.printMessage(listener, true, "wildcard not found!\n");
            return null;
        }

        // params to uploadBean
        ParamsBean paramsBean = new ParamsBean();
        paramsBean.setApiKey(maps.get("-apiKey"));
        paramsBean.setScandir(maps.get("-scanDir"));
        paramsBean.setWildcard(maps.get("-wildcard"));
        paramsBean.setBuildName(maps.containsKey("-buildName") ? maps.get("-buildName") : "");
        paramsBean.setQrcodePath(maps.containsKey("-qrcodePath") ? maps.get("-qrcodePath") : null);
        paramsBean.setEnvVarsPath(maps.containsKey("-envVarsPath") ? maps.get("-envVarsPath") : null);
        paramsBean.setBuildPassword(maps.containsKey("-buildPassword") ? maps.get("-buildPassword") : "");
        paramsBean.setBuildInstallType(maps.containsKey("-buildInstallType") ? maps.get("-buildInstallType") : "1");
        paramsBean.setBuildUpdateDescription(maps.containsKey("-buildUpdateDescription") ? maps.get("-buildUpdateDescription") : "");
        paramsBean.setBuildChannelShortcut(maps.containsKey("-buildChannelShortcut") ? maps.get("-buildChannelShortcut") : "");
        return paramsBean;
    }

    /**
     * upload 2 pgyer
     *
     * @param envVars      envVars
     * @param printHeader  printHeader
     * @param paramsBean uploadBean
     * @param listener     listener
     * @return pgyer bean
     */
    public static PgyerBean upload2Pgyer(EnvVars envVars, boolean printHeader, ParamsBean paramsBean, Message listener) {
        // print header info
        if (printHeader) {
            CommonUtil.printHeaderInfo(listener);
        }

        // find upload file
        paramsBean.setUploadFile(CommonUtil.findFile(paramsBean.getScandir(), paramsBean.getWildcard(), listener));

        // check upload file
        if (paramsBean.getUploadFile() == null) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        File uploadFile = new File(paramsBean.getUploadFile());
        if (!uploadFile.exists() || !uploadFile.isFile()) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        String result = "";
        try {
            CommonUtil.printMessage(listener, true, "upload：getToken to " + UPLOAD_URL);

            // optimization upload description
            if (CommonUtil.isBlank(paramsBean.getBuildUpdateDescription())
                    || "${SCM_CHANGELOG}".equals(paramsBean.getBuildUpdateDescription())) {
                paramsBean.setBuildUpdateDescription("");
            }

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MediaType.parse("multipart/form-data"))
                    .addFormDataPart("_api_key", paramsBean.getApiKey())
                    .addFormDataPart("buildInstallType", paramsBean.getBuildInstallType())
                    .addFormDataPart("buildPassword", paramsBean.getBuildPassword())
                    .addFormDataPart("buildUpdateDescription", paramsBean.getBuildUpdateDescription())
                    .addFormDataPart("buildChannelShortcut", paramsBean.getBuildChannelShortcut())
                    .addFormDataPart("buildType", "android")
                    .build();
            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(new ProgressRequestBody(requestBody, null))
                    .build();
            int timeout = CommonUtil.getUploadTimeout(envVars);
            Response execute = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .build()
                    .newCall(request).execute();

            if (execute.body() == null) {
                CommonUtil.printMessage(listener, true, "upload failed with pgyer!");
                CommonUtil.printMessage(listener, true, "upload token result is null.");
                return null;
            }
            result = execute.body().string();
            if (result != null && result.contains("\"data\":[]")) {
                result = result.replace("\"data\":[]", "\"data\":{}");
            }

            PgyerTokenBean tokenBean = null;
            try {
                tokenBean = new Gson().fromJson(result, new TypeToken<PgyerTokenBean>() {
                }.getType());

            } catch (Exception e) {
                e.printStackTrace();
                CommonUtil.printMessage(listener, true, e.getMessage());
                return null;
            }
            if (tokenBean.getCode() != 0) {
                CommonUtil.printMessage(listener, true, "upload failed with pgyer!");
                CommonUtil.printMessage(listener, true, "ERROR code：" + tokenBean.getCode());
                CommonUtil.printMessage(listener, true, "ERROR message：" + tokenBean.getMessage() + "\n");
                return null;
            }
            return upload2PgyerFile(envVars, paramsBean,tokenBean,listener);
        } catch (IOException e) {
            listener.message(true, "pgyer result: " + result);
            listener.message(true, "ERROR: " + e.getMessage() + "\n");
            return null;
        }
    }

    /**
     * upload 2 pgyer
     * @param envVars      envVars
     * @param paramsBean uploadBean
     * @param listener     listener
     * @return pgyer bean
     */
    public static PgyerBean upload2PgyerFile(EnvVars envVars, ParamsBean paramsBean, PgyerTokenBean tokenBean, Message listener) {

        // find upload file
        paramsBean.setUploadFile(CommonUtil.findFile(paramsBean.getScandir(), paramsBean.getWildcard(), listener));

        // check upload file
        if (paramsBean.getUploadFile() == null) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        File uploadFile = new File(paramsBean.getUploadFile());
        if (!uploadFile.exists() || !uploadFile.isFile()) {
            CommonUtil.printMessage(listener, true, "The uploaded file was not found，plase check scandir or wildcard!\n");
            return null;
        }

        String result = "";
        try {
            CommonUtil.printMessage(listener, true, "upload：" + uploadFile.getName() + " to Pgyer");
            CommonUtil.printMessage(listener, true, "upload file size: " + CommonUtil.convertFileSize(uploadFile.length()));

            MediaType type = MediaType.parse("application/octet-stream");
            RequestBody fileBody = RequestBody.create(type, uploadFile);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MediaType.parse("multipart/form-data"))
                    .addFormDataPart("key", tokenBean.getData().getKey())
                    .addFormDataPart("signature", tokenBean.getData().getParams().getSignature())
                    .addFormDataPart("x-cos-security-token", tokenBean.getData().getParams().getX_cos_security_token())
                    .addFormDataPart("file", uploadFile.getName(), fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url(tokenBean.getData().getEndpoint())
                    .post(new ProgressRequestBody(requestBody, new CommonUtil.FileUploadProgressListener(listener)))
                    .build();
            int timeout = CommonUtil.getUploadTimeout(envVars);
            Response execute = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .build()
                    .newCall(request).execute();

            if (execute.body() == null) {
                CommonUtil.printMessage(listener, true, "upload file failed with oss");
                CommonUtil.printMessage(listener, true, "upload file result is null.");
                return null;
            }
            if(execute.code() == 204){
                String url = "https://www.pgyer.com/apiv2/app/buildInfo?_api_key="+ paramsBean.getApiKey()+"&buildKey="+tokenBean.getData().getKey();
                times = 0;
                return uploadResult(url, paramsBean,listener);
            } else {
                CommonUtil.printMessage(listener, true, "upload failed with pgyer");
                return null;
            }
        } catch (IOException e) {
            listener.message(true, "pgyer result: " + result);
            listener.message(true, "ERROR: " + e.getMessage() + "\n");
            return null;
        }
    }

    static boolean bGo = true;
    static Timer timers = null;
    static int delay = 5000;
    static int times = 0;
    /**
     * Obtain the result of PGYER synchronizing data upload（获取pgyer 同步上传数据结果）
     * @param url
     * @param paramsBean
     * @param listener
     * @return
     */
    public static PgyerBean uploadResult(String url, ParamsBean paramsBean, Message listener){
        String result = "";
        CommonUtil.printMessage(listener, true, "upload：Wait for the pgyer synchronization result");
        try {
            //同步数据需要3~5秒延迟4秒获取最终同步数据
            timers = null;
            bGo = true;
            timers = new Timer(delay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(timers != null){
                        bGo = false;
                        timers.stop();
                        timers = null;

                    }
                }
            });
            timers.start();
            int i=0;
            while (bGo){
                i++;
                if(i % 2000000000 == 0){
                    CommonUtil.printMessage(listener, true, "upload：pgyer is synchronizing data……");
                }
            }
            Request request = new Request.Builder().url(url).get().build();
            Response execute = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .connectTimeout(300, TimeUnit.SECONDS)
                    .build()
                    .newCall(request).execute();

            if (execute.body() == null) {
                CommonUtil.printMessage(listener, true, "upload file result failed with uploadResult");
                CommonUtil.printMessage(listener, true, "upload file result is null.");
                return null;
            }
            result = execute.body().string();
            if (result != null && result.contains("\"data\":[]")) {
                result = result.replace("\"data\":[]", "\"data\":{}");
            }

            PgyerBean pgyerBean = null;
            try {
                pgyerBean = new Gson().fromJson(result, new TypeToken<PgyerBean>() {
                }.getType());
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtil.printMessage(listener, true, e.getMessage());
                return null;
            }

            if (pgyerBean.getCode() != 0) {
                if(pgyerBean.getCode() == 1246 || pgyerBean.getCode() == 1247){
                    if(times < 5){
                        times ++ ;
                        CommonUtil.printMessage(listener, true, "upload：Pgyer has not synchronized the results");
                        bGo = true;
                        delay = 2000;
                        return uploadResult(url, paramsBean,listener);
                    } else {
                        CommonUtil.printMessage(listener, true, "upload failed with pgyer!");
                        CommonUtil.printMessage(listener, true, "ERROR code：" + pgyerBean.getCode());
                        CommonUtil.printMessage(listener, true, "ERROR message：" + pgyerBean.getMessage() + "\n");
                        return null;
                    }
                } else {
                    CommonUtil.printMessage(listener, true, "upload failed with pgyer!");
                    CommonUtil.printMessage(listener, true, "ERROR code：" + pgyerBean.getCode());
                    CommonUtil.printMessage(listener, true, "ERROR message：" + pgyerBean.getMessage() + "\n");
                    return null;
                }
            }

            pgyerBean.getData().setAppPgyerURL(CommonUtil.PGYER_HOST + "/" + pgyerBean.getData().getBuildShortcutUrl());
            pgyerBean.getData().setAppBuildURL(CommonUtil.PGYER_HOST + "/" + pgyerBean.getData().getBuildKey());
            pgyerBean.getData().setBuildIcon(CommonUtil.PGYER_HOST + "/image/view/app_icons/" + pgyerBean.getData().getBuildIcon());

            CommonUtil.printMessage(listener, true, "uploaded successfully!\n");
            printResultInfo(pgyerBean, listener);
            writeEnvVars(paramsBean, pgyerBean, listener);
            downloadQrcode(paramsBean, pgyerBean, listener);
            return pgyerBean;
        } catch (IOException e) {
            e.printStackTrace();
            listener.message(true, "pgyer result: " + result);
            listener.message(true, "ERROR: " + e.getMessage() + "\n");
            return null;
        }
    }

    /**
     * Download the qr code
     *
     * @param paramsBean paramsBeanV2
     * @param pgyerBean  pgyerBeanV2
     * @param listener     listener
     */
    private static void downloadQrcode(ParamsBean paramsBean, PgyerBean pgyerBean, Message listener) {
        if (paramsBean.getQrcodePath() == null) {
            return;
        }
        if (CommonUtil.replaceBlank(paramsBean.getQrcodePath()).length() == 0) {
            return;
        }
        CommonUtil.printMessage(listener, true, "Downloading the qr code……");
        File qrcode = new File(paramsBean.getQrcodePath());
        if (!qrcode.getParentFile().exists() && !qrcode.getParentFile().mkdirs()) {
            CommonUtil.printMessage(listener, true, "Oh, my god, download the qr code failed……" + "\n");
            return;
        }
        File file = CommonUtil.download(pgyerBean.getData().getBuildQRCodeURL(), qrcode.getParentFile().getAbsolutePath(), qrcode.getName());
        if (file != null) {
            CommonUtil.printMessage(listener, true, "Download the qr code successfully! " + file + "\n");
        } else {
            CommonUtil.printMessage(listener, true, "Oh, my god, download the qr code failed……" + "\n");
        }
    }

    /**
     * Writing the environment variable to the file.
     *
     * @param paramsBean paramsBeanV2
     * @param pgyerBean  pgyerBeanV2
     * @param listener     listener
     */
    private static void writeEnvVars(ParamsBean paramsBean, PgyerBean pgyerBean, Message listener) {
        if (paramsBean.getEnvVarsPath() == null) {
            return;
        }
        if (CommonUtil.replaceBlank(paramsBean.getEnvVarsPath()).length() == 0) {
            return;
        }
        CommonUtil.printMessage(listener, true, "Writing the environment variable to the file……");
        File envVars = new File(paramsBean.getEnvVarsPath());
        if (!envVars.getParentFile().exists() && !envVars.getParentFile().mkdirs()) {
            CommonUtil.printMessage(listener, true, "Oh my god, the environment variable writes failed……" + "\n");
            return;
        }
        File file = CommonUtil.write(envVars.getAbsolutePath(), getEnvVarsInfo(pgyerBean), "utf-8");
        if (file != null) {
            CommonUtil.printMessage(listener, true, "The environment variable is written successfully! " + file + "\n");
        } else {
            CommonUtil.printMessage(listener, true, "Oh my god, the environment variable writes failed……" + "\n");
        }
    }

    /**
     * Print return log
     *
     * @param pgyerBean pgyerBeanV2
     * @param listener    listener
     */
    private static void printResultInfo(PgyerBean pgyerBean, Message listener) {
        PgyerBean.DataBean data = pgyerBean.getData();
        CommonUtil.printMessage(listener, true, "应用名称：" + data.getBuildName());
        CommonUtil.printMessage(listener, true, "应用类型：" + data.getBuildType());
        CommonUtil.printMessage(listener, true, "版本号：" + data.getBuildVersion());
        CommonUtil.printMessage(listener, true, "build号：" + data.getBuildBuildVersion());
        CommonUtil.printMessage(listener, true, "Build Key：" + data.getBuildKey());
        CommonUtil.printMessage(listener, true, "版本编号：" + data.getBuildVersionNo());
        CommonUtil.printMessage(listener, true, "文件大小：" + data.getBuildFileSize());
        CommonUtil.printMessage(listener, true, "应用介绍：" + data.getBuildDescription());
        CommonUtil.printMessage(listener, true, "应用主页：" + data.getAppPgyerURL());
        CommonUtil.printMessage(listener, true, "应用短链接：" + data.getBuildShortcutUrl());
        CommonUtil.printMessage(listener, true, "应用上传时间：" + data.getBuildCreated());
        CommonUtil.printMessage(listener, true, "应用更新时间：" + data.getBuildUpdated());
        CommonUtil.printMessage(listener, true, "应用构建主页：" + data.getAppBuildURL());
        CommonUtil.printMessage(listener, true, "应用更新说明：" + data.getBuildUpdateDescription());
        CommonUtil.printMessage(listener, true, "是否是最新版：" + data.getBuildIsLastest());
        CommonUtil.printMessage(listener, true, "应用程序包名：" + data.getBuildIdentifier());
        CommonUtil.printMessage(listener, true, "应用截图的key：" + data.getBuildScreenshots());
        CommonUtil.printMessage(listener, true, "应用二维码地址：" + data.getBuildQRCodeURL());
        CommonUtil.printMessage(listener, true, "是否是第一个App：" + data.getBuildType());
        CommonUtil.printMessage(listener, true, "应用的Icon图标key：" + data.getBuildIcon());
        CommonUtil.printMessage(listener, false, "");
    }

    /**
     * Format the return information.
     *
     * @param pgyerBean pgyerBeanV2
     * @return Formatted log
     */
    private static String getEnvVarsInfo(PgyerBean pgyerBean) {
        StringBuffer sb = new StringBuffer();
        sb.append("buildKey").append("=").append(pgyerBean.getData().getBuildKey()).append("\n");
        sb.append("buildName").append("=").append(pgyerBean.getData().getBuildName()).append("\n");
        sb.append("buildIcon").append("=").append(pgyerBean.getData().getBuildIcon()).append("\n");
        sb.append("buildType").append("=").append(pgyerBean.getData().getBuildType()).append("\n");
        sb.append("appBuildURL").append("=").append(pgyerBean.getData().getAppBuildURL()).append("\n");
        sb.append("appPgyerURL").append("=").append(pgyerBean.getData().getAppPgyerURL()).append("\n");
        sb.append("buildCreated").append("=").append(pgyerBean.getData().getBuildCreated()).append("\n");
        sb.append("buildIsFirst").append("=").append(pgyerBean.getData().getBuildIsFirst()).append("\n");
        sb.append("buildUpdated").append("=").append(pgyerBean.getData().getBuildUpdated()).append("\n");
        sb.append("buildVersion").append("=").append(pgyerBean.getData().getBuildVersion()).append("\n");
        sb.append("buildFileName").append("=").append(pgyerBean.getData().getBuildFileName()).append("\n");
        sb.append("buildFileSize").append("=").append(pgyerBean.getData().getBuildFileSize()).append("\n");
        sb.append("buildIsLastest").append("=").append(pgyerBean.getData().getBuildIsLastest()).append("\n");
        sb.append("buildQRCodeURL").append("=").append(pgyerBean.getData().getBuildQRCodeURL()).append("\n");
        sb.append("buildVersionNo").append("=").append(pgyerBean.getData().getBuildVersionNo()).append("\n");
        sb.append("buildIdentifier").append("=").append(pgyerBean.getData().getBuildIdentifier()).append("\n");
        sb.append("buildDescription").append("=").append(pgyerBean.getData().getBuildDescription()).append("\n");
        sb.append("buildScreenshots").append("=").append(pgyerBean.getData().getBuildScreenshots()).append("\n");
        sb.append("buildShortcutUrl").append("=").append(pgyerBean.getData().getBuildShortcutUrl()).append("\n");
        sb.append("buildBuildVersion").append("=").append(pgyerBean.getData().getBuildBuildVersion()).append("\n");
        sb.append("buildUpdateDescription").append("=").append(pgyerBean.getData().getBuildUpdateDescription()).append("\n");
        return sb.toString();
    }
}
