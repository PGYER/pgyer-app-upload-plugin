package com.pgyer.app.upload.bean;

public class PgyerBean {

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

        private String buildKey;
        private String buildType;
        private String buildIsFirst;
        private String buildIsLastest;
        private String buildFileKey;
        private String buildFileName;
        private String buildFileSize;
        private String buildName;
        private String buildVersion;
        private String buildVersionNo;
        private String buildBuildVersion;
        private String buildIdentifier;
        private String buildIcon;
        private String buildDescription;
        private String buildUpdateDescription;
        private String buildScreenshots;
        private String buildShortcutUrl;
        private String buildCreated;
        private String buildUpdated;
        private String buildQRCodeURL;
        private String appPgyerURL;
        private String appBuildURL;

        public String getBuildKey() {
            return buildKey;
        }

        public void setBuildKey(String buildKey) {
            this.buildKey = buildKey;
        }

        public String getBuildType() {
            return buildType;
        }

        public void setBuildType(String buildType) {
            this.buildType = buildType;
        }

        public String getBuildIsFirst() {
            return buildIsFirst;
        }

        public void setBuildIsFirst(String buildIsFirst) {
            this.buildIsFirst = buildIsFirst;
        }

        public String getBuildIsLastest() {
            return buildIsLastest;
        }

        public void setBuildIsLastest(String buildIsLastest) {
            this.buildIsLastest = buildIsLastest;
        }

        public String getBuildFileKey() {
            return buildFileKey;
        }

        public void setBuildFileKey(String buildFileKey) {
            this.buildFileKey = buildFileKey;
        }

        public String getBuildFileName() {
            return buildFileName;
        }

        public void setBuildFileName(String buildFileName) {
            this.buildFileName = buildFileName;
        }

        public String getBuildFileSize() {
            return buildFileSize;
        }

        public void setBuildFileSize(String buildFileSize) {
            this.buildFileSize = buildFileSize;
        }

        public String getBuildName() {
            return buildName;
        }

        public void setBuildName(String buildName) {
            this.buildName = buildName;
        }

        public String getBuildVersion() {
            return buildVersion;
        }

        public void setBuildVersion(String buildVersion) {
            this.buildVersion = buildVersion;
        }

        public String getBuildVersionNo() {
            return buildVersionNo;
        }

        public void setBuildVersionNo(String buildVersionNo) {
            this.buildVersionNo = buildVersionNo;
        }

        public String getBuildBuildVersion() {
            return buildBuildVersion;
        }

        public void setBuildBuildVersion(String buildBuildVersion) {
            this.buildBuildVersion = buildBuildVersion;
        }

        public String getBuildIdentifier() {
            return buildIdentifier;
        }

        public void setBuildIdentifier(String buildIdentifier) {
            this.buildIdentifier = buildIdentifier;
        }

        public String getBuildIcon() {
            return buildIcon;
        }

        public void setBuildIcon(String buildIcon) {
            this.buildIcon = buildIcon;
        }

        public String getBuildDescription() {
            return buildDescription;
        }

        public void setBuildDescription(String buildDescription) {
            this.buildDescription = buildDescription;
        }

        public String getBuildUpdateDescription() {
            return buildUpdateDescription;
        }

        public void setBuildUpdateDescription(String buildUpdateDescription) {
            this.buildUpdateDescription = buildUpdateDescription;
        }

        public String getBuildScreenshots() {
            return buildScreenshots;
        }

        public void setBuildScreenshots(String buildScreenshots) {
            this.buildScreenshots = buildScreenshots;
        }

        public String getBuildShortcutUrl() {
            return buildShortcutUrl;
        }

        public void setBuildShortcutUrl(String buildShortcutUrl) {
            this.buildShortcutUrl = buildShortcutUrl;
        }

        public String getBuildCreated() {
            return buildCreated;
        }

        public void setBuildCreated(String buildCreated) {
            this.buildCreated = buildCreated;
        }

        public String getBuildUpdated() {
            return buildUpdated;
        }

        public void setBuildUpdated(String buildUpdated) {
            this.buildUpdated = buildUpdated;
        }

        public String getBuildQRCodeURL() {
            return buildQRCodeURL;
        }

        public void setBuildQRCodeURL(String buildQRCodeURL) {
            this.buildQRCodeURL = buildQRCodeURL;
        }

        public String getAppPgyerURL() {
            return appPgyerURL;
        }

        public void setAppPgyerURL(String appPgyerURL) {
            this.appPgyerURL = appPgyerURL;
        }

        public String getAppBuildURL() {
            return appBuildURL;
        }

        public void setAppBuildURL(String appBuildURL) {
            this.appBuildURL = appBuildURL;
        }
    }
}
