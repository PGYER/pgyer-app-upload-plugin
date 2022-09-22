package com.pgyer.app.upload;

import com.pgyer.app.upload.bean.ParamsBean;
import com.pgyer.app.upload.helper.PgyerHelper;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

public class PgyerAppUpload extends Recorder {
    private final Secret apiKey;
    private final String scanDir;
    private final String wildcard;
    private final String buildInstallType;
    private final Secret buildPassword;
    private final String buildUpdateDescription;
    private final String buildName;
    private final String buildChannelShortcut;

    private final String qrcodePath;
    private final String envVarsPath;

    @DataBoundConstructor
    public PgyerAppUpload(Secret apiKey, String scanDir, String wildcard, String buildInstallType, Secret buildPassword, String buildUpdateDescription, String buildName, String buildChannelShortcut, String qrcodePath, String envVarsPath) {
        this.apiKey = Secret.fromString(String.valueOf(apiKey));
        this.scanDir = scanDir;
        this.wildcard = wildcard;
        this.buildInstallType = buildInstallType;
        this.buildPassword = buildPassword;
        this.buildUpdateDescription = buildUpdateDescription;
        this.buildName = buildName;
        this.buildChannelShortcut = buildChannelShortcut;
        this.qrcodePath = qrcodePath;
        this.envVarsPath = envVarsPath;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public String getScanDir() {
        return scanDir;
    }

    public String getWildcard() {
        return wildcard;
    }

    public String getBuildInstallType() {
        return buildInstallType;
    }

    public Secret getBuildPassword() {
        return buildPassword;
    }

    public String getBuildUpdateDescription() {
        return buildUpdateDescription;
    }

    public String getBuildName() {
        return buildName;
    }

    public String getBuildChannelShortcut() {
        return buildChannelShortcut;
    }

    public String getQrcodePath() {
        return qrcodePath;
    }

    public String getEnvVarsPath() {
        return envVarsPath;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        ParamsBean paramsBean = new ParamsBean();
        paramsBean.setApiKey(apiKey.getPlainText());
        paramsBean.setScandir(scanDir);
        paramsBean.setWildcard(wildcard);
        paramsBean.setBuildPassword(buildPassword.getPlainText());
        paramsBean.setBuildInstallType(buildInstallType);
        paramsBean.setBuildName(buildName);
        paramsBean.setBuildUpdateDescription(buildUpdateDescription);
        paramsBean.setBuildChannelShortcut(buildChannelShortcut);
        paramsBean.setQrcodePath(qrcodePath);
        paramsBean.setEnvVarsPath(envVarsPath);
        return PgyerHelper.upload(build, listener, paramsBean);
//        return super.perform(build, launcher, listener);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Symbol("upload-pgyer-v2")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String installType = "1";

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckApiKey(@QueryParameter String value) {
            return ValidationParameters.doCheckApiKey(value);
        }

        public FormValidation doCheckScanDir(@QueryParameter String value) {
            return ValidationParameters.doCheckScanDir(value);
        }

        public FormValidation doCheckWildcard(@QueryParameter String value) {
            return ValidationParameters.doCheckWildcard(value);
        }

        public FormValidation doCheckBuildInstallType(@QueryParameter String value) {
            installType = value;
            return ValidationParameters.doCheckInstallType(value);
        }

        public FormValidation doCheckBuildPassword(@QueryParameter String value) {
            return ValidationParameters.doCheckPassword(installType, value);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Pgyer app upload";
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
