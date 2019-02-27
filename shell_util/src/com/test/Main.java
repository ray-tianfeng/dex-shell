package com.test;

import com.utils.ApkShellUtils;
import com.utils.FileUtils;

import java.io.*;
import java.util.Scanner;

public class Main {

    private static String primaryApkPath = "G:\\demo\\shell\\demo.apk";
    private static String outShellApkPath = "G:\\demo\\shell\\shell_demo.apk";
    private static String unShellApkPath = "G:\\demo\\shell\\unshell.apk";
    public static void main(String[] args) {
        try {
            if(!FileUtils.isExit(primaryApkPath, unShellApkPath)){
                System.out.println("file is null");
                return;
            }
            FileUtils.delete("G:\\demo\\shell\\demo");
            FileUtils.delete("G:\\demo\\shell\\unshell");
            FileUtils.delete(outShellApkPath);
            ApkShellUtils.apkShell(primaryApkPath,unShellApkPath,outShellApkPath);
//            FileUtils.unZip(primaryApkPath,"G:\\demo\\shell\\demo");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
