package com.pgyer.app.upload.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pgyer.app.upload.bean.ParamsBean;
import com.pgyer.app.upload.bean.PgyerBean;
import com.pgyer.app.upload.impl.Message;
import com.pgyer.app.upload.net.PgyerUpload;
import com.pgyer.app.upload.utils.CommonUtil;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

import java.io.IOException;
import java.util.Map;

public class PgyerHelper {
    /**
     * @param build        build
     * @param listener     listener
     * @param paramsBean uploadBean
     * @return success or failure
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    public static boolean upload(AbstractBuild<?, ?> build, final BuildListener listener, ParamsBean paramsBean) throws IOException, InterruptedException {
        Message message = new Message() {
            @Override
            public void message(boolean needTag, String mesage) {
                listener.getLogger().println((needTag ? CommonUtil.LOG_PREFIX : "") + mesage);
            }
        };

        if (CommonUtil.isBuildFailed(build, message)) {
            return true;
        }

        if (CommonUtil.isSkipUpload(build.getEnvironment(listener), message)) {
            return true;
        }

        // expand params
        paramsBean.setApiKey(build.getEnvironment(listener).expand(paramsBean.getApiKey()));
        paramsBean.setScandir(build.getEnvironment(listener).expand(paramsBean.getScandir()));
        paramsBean.setWildcard(build.getEnvironment(listener).expand(paramsBean.getWildcard()));
        paramsBean.setBuildPassword(build.getEnvironment(listener).expand(paramsBean.getBuildPassword()));
        paramsBean.setBuildInstallType(build.getEnvironment(listener).expand(paramsBean.getBuildInstallType()));
        paramsBean.setBuildUpdateDescription(build.getEnvironment(listener).expand(paramsBean.getBuildUpdateDescription()));
        paramsBean.setBuildName(build.getEnvironment(listener).expand(paramsBean.getBuildName()));
        paramsBean.setBuildChannelShortcut(build.getEnvironment(listener).expand(paramsBean.getBuildChannelShortcut()));

        // upload
        PgyerBean pgyerBean = PgyerUpload.upload2Pgyer(build.getEnvironment(listener), true, paramsBean, message);
        if (pgyerBean == null) {
            return false;
        }

        // http://jenkins-ci.361315.n4.nabble.com/Setting-an-env-var-from-a-build-step-td4657347.html
        message.message(true, "The Jenkins environment variable is being set.");
        String data = new Gson().toJson(pgyerBean.getData());
        Map<String, String> maps = new Gson().fromJson(data, new TypeToken<Map<String, String>>() {
        }.getType());
        for (Map.Entry<String, String> entry : maps.entrySet()) {
            String key = entry.getKey();
            build.addAction(new PublishEnvVarAction(key, entry.getValue()));
            message.message(true, "The ${" + key + "} set up successfully! You can use it anywhere now!");

            if (key.equals("buildQRCodeURL")) {
                build.addAction(new PublishEnvVarAction("appQRCodeURL", entry.getValue()));
                message.message(true, "The ${appQRCodeURL} set up successfully! You can use it anywhere now.!");
            }
        }
        message.message(true, "congratulations!\n");
        return true;
    }
}
