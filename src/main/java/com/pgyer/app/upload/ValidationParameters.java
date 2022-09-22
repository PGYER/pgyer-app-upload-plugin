package com.pgyer.app.upload;

import hudson.util.FormValidation;

class ValidationParameters {
    public static FormValidation doCheckUKey(String value) {
        if (value.length() == 0) {
            return FormValidation.error("Please set a uKey");
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckApiKey(String value) {
        if (value.length() == 0) {
            return FormValidation.error("Please set a api_key");
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckScanDir(String value) {
        if (value.length() == 0) {
            return FormValidation.error("Please set upload ipa or apk file base dir name");
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckWildcard(String value) {
        if (value.length() == 0) {
            return FormValidation.error("Please set upload ipa or apk file wildcard");
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckInstallType(String value) {
        try {
            int valueInt = Integer.parseInt(value);
            if (valueInt < 1 || valueInt > 3) {
                return FormValidation.error("application installation, the value is (1,2,3).");
            }
            return FormValidation.ok();
        } catch (Exception e) {
            return FormValidation.error("application installation, must be number, the value is (1,2,3).");
        }
    }

    public static FormValidation doCheckPassword(String installType, String value) {
        if (!installType.equals(String.valueOf(2))) {
            if (value.length() == 0) {
                return FormValidation.ok();
            }
            return FormValidation.warning("The value of installType is not 2, so the password will be ignored");
        } else {
            if (value.length() == 0) {
                return FormValidation.error("Please set a password");
            }
            if (value.length() < 6) {
                return FormValidation.warning("Isn't the password too short?");
            }
            return FormValidation.ok();
        }
    }
}
