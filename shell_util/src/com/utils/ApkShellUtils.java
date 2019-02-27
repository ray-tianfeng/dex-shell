package com.utils;

import com.alibaba.fastjson.JSON;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.Adler32;

/**
 * Created by zoulong on 2019/2/25 0025.
 */
public class ApkShellUtils {

    /**
     * 给apk加壳
     * @param primaryApkPath 原apk
     * @param unShellApkPath 解壳apk
     * @param outApkPath 加壳后新APK
     * @throws Exception
     */
    public static void apkShell(String primaryApkPath,String unShellApkPath,String outApkPath) throws Exception{
        if(!FileUtils.isExit(primaryApkPath, unShellApkPath)){
            throw new RuntimeException("check params");
        }
        //解压原apk
        String unPrimaryApkDstPath = primaryApkPath.replace(".apk", "");
        ApkToolUtils.decompile(primaryApkPath, unPrimaryApkDstPath);
        String primaryManifestPath = unPrimaryApkDstPath + File.separator + "AndroidManifest.xml";

        //解压解壳apk
        String unShellApkDstPath = unShellApkPath.replace(".apk", "");
        ApkToolUtils.decompile(unShellApkPath, unShellApkDstPath);
        String unShellManifestPath = unShellApkDstPath + File.separator + "AndroidManifest.xml";
        String unShellDexPath = unShellApkDstPath + File.separator + "classes.dex";
        File unShellFile = new File(unShellDexPath);


        File unApkDir = new File(unPrimaryApkDstPath);
        ArrayList<File> dexArray = new ArrayList<File>();
        for(File file : unApkDir.listFiles()){//读取解壳后的dex
            if(file.getName().endsWith(".dex")){
                dexArray.add(file);
            }
        }
        String shellDexPath = unPrimaryApkDstPath + File.separator + "classes.dex";
        shellDex(dexArray, unShellFile, shellDexPath);//生产新的dex（加壳）

        String mateInfPath = unPrimaryApkDstPath + File.separator +"META-INF";//删除meta-inf，重新签名后会生成
        FileUtils.delete(mateInfPath);

        for(File file : dexArray){//清理多余dex文件
            if(file.getName().equals("classes.dex")){
                continue;
            }
            FileUtils.delete(file.getAbsolutePath());
        }

        String unShellApplicationName = AndroidXmlUtils.readApplicationName(unShellManifestPath);//解壳ApplicationName
        String primaryApplicationName = AndroidXmlUtils.readApplicationName(primaryManifestPath);//原applicationName
        AndroidXmlUtils.changeApplicationName(primaryManifestPath, unShellApplicationName);//改变原Applicationname为解壳ApplicationName
        if(primaryApplicationName != null){//将原ApplicationName写入mateData中，解壳application中会读取并替换应用Application
            AndroidXmlUtils.addMateData(primaryManifestPath, "APPLICATION_CLASS_NAME", primaryApplicationName);
        }
        //回编，回编系统最好是linux
        ApkToolUtils.compile(unPrimaryApkDstPath,outApkPath);
        //v1签名
        SignUtils.V1(outApkPath, SignUtils.getDefaultKeystore());
        //清理目录
        FileUtils.delete(unPrimaryApkDstPath);
        FileUtils.delete(unShellApkDstPath);
    }

    /**
     * 给dex加壳
     * @param primaryDexs 原dex集合
     * @param unShellDex 解壳dex
     * @param shellDexPath 生成新的dex路径
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     */
    private static void shellDex(ArrayList<File> primaryDexs, File unShellDex, String shellDexPath) throws IOException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {
        int primaryDexLen = 0;
        ArrayList<DexFile> dexFileInfos = new ArrayList<>(primaryDexs.size());
        for(File file : primaryDexs){//计算所有primary的长度
            DexFile dexFile = new DexFile(file.getName(),encryptionAES(readFileBytes(file)));
            dexFileInfos.add(dexFile);
            primaryDexLen += dexFile.getDexLength();
        }
        byte[] unShellDexFileByte = readFileBytes(unShellDex);
        int unShellDexLen = unShellDexFileByte.length;//解壳dex长度

        String dexFileComment = JSON.toJSONString(dexFileInfos);//原dexs描述
        int dexFileCommentLen = dexFileComment.getBytes().length;//dexs描述长度

        int totalLen = primaryDexLen+dexFileCommentLen + unShellDexLen+4;//新dex总长度
        byte[] shellDex = new byte[totalLen];

        System.arraycopy(unShellDexFileByte,0,shellDex,0,unShellDexLen);//先拷贝解壳dex

        int currentCopyIndex = unShellDexLen;
        for(DexFile dexFile : dexFileInfos){//拷贝原dexs
            System.arraycopy(dexFile.getData(),0,shellDex,currentCopyIndex,dexFile.getDexLength());
            currentCopyIndex += dexFile.getDexLength();
        }

        System.arraycopy(dexFileComment.getBytes(),0,shellDex,currentCopyIndex,dexFileCommentLen);//加入dexs描述

        System.arraycopy(intToByte(dexFileCommentLen),0,shellDex,totalLen-4,4);//描述长度
        fixFileSizeHeader(shellDex);//修改dex头 file_size值
        fixSHA1Header(shellDex);//修改dex头 sha1值
        fixCheckSumHeader(shellDex);//修改dex头，CheckSum 校验码

        // 把内容写到 newDexFile
        File file = new File(shellDexPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream localFileOutputStream = new FileOutputStream(shellDexPath);
        localFileOutputStream.write(shellDex);
        localFileOutputStream.flush();
        localFileOutputStream.close();
    }


    //在此做数据加密
    private static byte[] encryptionAES(byte[] data) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        return SecurityUtils.AESEncryption(Constant.AES_PRIVATE_KEY,Constant.AES_IV,Constant.AES_TYPE,data);
    }

    /**
     * 修改dex头，CheckSum 校验码
     *
     * @param dexBytes
     */
    private static void fixCheckSumHeader(byte[] dexBytes) {
        Adler32 adler = new Adler32();
        adler.update(dexBytes, 12, dexBytes.length - 12);// 从12到文件末尾计算校验码
        long value = adler.getValue();
        int va = (int) value;
        byte[] newcs = intToByte(va);
        // 高位在前，低位在前掉个个
        byte[] recs = new byte[4];
        for (int i = 0; i < 4; i++) {
            recs[i] = newcs[newcs.length - 1 - i];

        }
        System.arraycopy(recs, 0, dexBytes, 8, 4);// 效验码赋值（8-11）
    }

    /**
     * 修改dex头 sha1值
     *
     * @param dexBytes
     * @throws NoSuchAlgorithmException
     */
    private static void fixSHA1Header(byte[] dexBytes)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(dexBytes, 32, dexBytes.length - 32);// 从32为到结束计算sha--1
        byte[] newdt = md.digest();
        System.arraycopy(newdt, 0, dexBytes, 12, 20);// 修改sha-1值（12-31）
        // 输出sha-1值，可有可无
        String hexstr = "";
        for (int i = 0; i < newdt.length; i++) {
            hexstr += Integer.toString((newdt[i] & 0xff) + 0x100, 16)
                    .substring(1);
        }
        System.out.println("sha-1 值：" + hexstr);

    }


    /**
     * int 转byte[]
     *
     * @param number
     * @return
     */
    public static byte[] intToByte(int number) {
        byte[] b = new byte[4];
        for (int i = 3; i >= 0; i--) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }

    /**
     * 修改dex头 file_size值
     *
     * @param dexBytes
     */
    private static void fixFileSizeHeader(byte[] dexBytes) {
        // 新文件长度
        byte[] newfs = intToByte(dexBytes.length);

        byte[] refs = new byte[4];
        // 高位在前，低位在前掉个个
        for (int i = 0; i < 4; i++) {
            refs[i] = newfs[newfs.length - 1 - i];

        }
        System.arraycopy(refs, 0, dexBytes, 32, 4);// 修改（32-35）
        System.out.println();
    }

    /**
     * 以二进制读出文件内容
     *
     * @param file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("resource")
    private static byte[] readFileBytes(File file) throws IOException {
        byte[] arrayOfByte = new byte[1024];
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        while (true) {
            int i = fis.read(arrayOfByte);
            if (i != -1) {
                localByteArrayOutputStream.write(arrayOfByte, 0, i);
            } else {
                fis.close();
                return localByteArrayOutputStream.toByteArray();
            }
        }
    }
}
