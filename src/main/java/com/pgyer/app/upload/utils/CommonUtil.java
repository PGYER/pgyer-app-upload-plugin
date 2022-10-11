package com.pgyer.app.upload.utils;

import com.pgyer.app.upload.impl.Message;
import com.pgyer.app.upload.net.ProgressRequestBody;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import org.apache.tools.ant.DirectoryScanner;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
    public static final String LOG_PREFIX = "[UPLOAD TO PGYER] - ";
    public static final String PGYER_HOST = "https://www.pgyer.com";

    /**
     * Header
     *
     * @param listener listener
     */
    public static void printHeaderInfo(Message listener) {
        printMessage(listener, false, "");
        printMessage(listener, false, "**************************************************************************************************");
        printMessage(listener, false, "**************************************************************************************************");
        printMessage(listener, false, "********************************        APP UPLOAD TO PGYER       ********************************");
        printMessage(listener, false, "********************************       https://www.pgyer.com      ********************************");
        printMessage(listener, false, "**************************************************************************************************");
        printMessage(listener, false, "**************************************************************************************************");
        printMessage(listener, false, "");
    }

    /**
     * print message
     *
     * @param listener listener
     * @param needTag  needTag
     * @param message  message
     */
    public static void printMessage(Message listener, boolean needTag, String message) {
        if (listener != null) {
            listener.message(needTag, message);
        }
    }

    /**
     * skip upload
     *
     * @param envVars envVars
     * @param message message
     * @return skip
     */
    public static boolean isSkipUpload(EnvVars envVars, Message message) {
        try {
            String uploadKey = "isUploadPgyer";
            String isUploadPgyerString = envVars.get(uploadKey, "true");
            boolean isUploadPgyer = Boolean.parseBoolean(isUploadPgyerString);
            if (!isUploadPgyer) {
                message.message(true, "The value of " + uploadKey + " is false, so it is not uploaded.");
            }
            return !isUploadPgyer;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * get upload timeout
     *
     * @param envVars envVars
     * @return timeout
     */
    public static int getUploadTimeout(EnvVars envVars) {
        int defalut = 300;
        if (envVars == null) {
            return defalut;
        }
        try {
            String timeoutKey = "uploadPgyerTimeout";
            if (envVars.containsKey(timeoutKey)) {
                String uploadPgyerTimeoutString = envVars.get(timeoutKey);
                return Integer.parseInt(uploadPgyerTimeoutString);
            } else {
                return defalut;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defalut;
        }
    }

    /**
     * check build result
     *
     * @param build   build
     * @param message message
     * @return failed
     */
    public static boolean isBuildFailed(AbstractBuild<?, ?> build, Message message) {
        // check build result
        Result result = build.getResult();
        boolean unStable = result != null && result.isWorseThan(Result.UNSTABLE);
        if (unStable) {
            message.message(true, "The build " + result.toString() + ", so the file was not uploaded.");
        }
        return unStable;
    }

    /**
     * find file
     *
     * @param scandir  scandir
     * @param wildcard wildcard
     * @param listener listener
     * @return file path
     */
    public static String findFile(String scandir, String wildcard, Message listener) {
        File dir = new File(scandir);
        if (!dir.exists() || !dir.isDirectory()) {
            CommonUtil.printMessage(listener, true, "scan dir:" + dir.getAbsolutePath());
            CommonUtil.printMessage(listener, true, "scan dir isn't exist or it's not a directory!");
            return null;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(scandir);
        scanner.setIncludes(new String[]{wildcard});
        scanner.setCaseSensitive(true);
        scanner.scan();
        String[] uploadFiles = scanner.getIncludedFiles();

        if (uploadFiles == null || uploadFiles.length == 0) {
            return null;
        }
        if (uploadFiles.length == 1) {
            return new File(dir, uploadFiles[0]).getAbsolutePath();
        }

        List<String> strings = Arrays.asList(uploadFiles);
        Collections.sort(strings, new FileComparator(dir));
        String uploadFiltPath = new File(dir, strings.get(0)).getAbsolutePath();
        CommonUtil.printMessage(listener, true, "Found " + uploadFiles.length + " files, the default choice of the latest modified file!");
        CommonUtil.printMessage(listener, true, "The latest modified file is " + uploadFiltPath + "\n");
        return uploadFiltPath;
    }

    /**
     * string is blank
     *
     * @param str string
     * @return isblank
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * size convert
     *
     * @param size file size
     * @return convert file size
     */
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    /**
     *
     */
    public static class FileUploadProgressListener implements ProgressRequestBody.Listener {
        private long last_time = -1L;
        private final Message listener;

        public FileUploadProgressListener(Message listener) {
            this.listener = listener;
        }

        @Override
        public void onRequestProgress(long bytesWritten, long contentLength) {
            final int progress = (int) (100F * bytesWritten / contentLength);
            if (progress == 100) {
                last_time = -1L;
                if(listener != null){
                    listener.message(true, "upload progress: " + progress + " %");
                }

                return;
            }

            if (last_time == -1) {
                last_time = System.currentTimeMillis();
                if(listener != null){
                    listener.message(true, "upload progress: " + progress + " %");
                }
                return;
            }

            if (System.currentTimeMillis() - last_time > 1000) {
                last_time = System.currentTimeMillis();
                listener.message(true, "upload progress: " + progress + " %");
            }
        }
    }

    /**
     *
     */
    public static class FileComparator implements Comparator<String>, Serializable {
        File dir;

        public FileComparator(File dir) {
            this.dir = dir;
        }

        @Override
        public int compare(String o1, String o2) {
            File file1 = new File(dir, o1);
            File file2 = new File(dir, o2);
            return Long.compare(file2.lastModified(), file1.lastModified());
        }
    }
}
