package com.utils;

import java.io.IOException;

/**
 * Created by zoulong on 2019/2/26 0026.
 */
public class ApkToolUtils {
    private static final String APKTOOL_PATH = "tools/apktool/apktool.jar";
    public static boolean decompile(String apkPath,String outPath) throws IOException, InterruptedException {
        String cmd = "java -jar %s -s d %s -o %s";
        cmd = String.format(cmd, new String[]{APKTOOL_PATH, apkPath, outPath});
        CmdExecutor.executor(cmd);
        return true;
    }

    public static boolean compile(String apkDir, String outPath) throws IOException, InterruptedException {
        String cmd = "java -jar %s b %s -o %s";
        cmd = String.format(cmd, new String[]{APKTOOL_PATH, apkDir, outPath});
        CmdExecutor.executor(cmd);
        return true;
    }
}
