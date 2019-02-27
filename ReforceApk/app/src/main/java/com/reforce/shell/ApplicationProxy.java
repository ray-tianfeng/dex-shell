package com.reforce.shell;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSON;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import dalvik.system.DexClassLoader;

/**
 * @author zl
 * @time 2019/2/22 0022.
 */
public class ApplicationProxy extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            File odex = getDir("payload_odex",MODE_PRIVATE);//android art虚拟机会对dex做优化
            File dex = getDir("payload_dex",MODE_PRIVATE);
            if(dex.listFiles().length == 0){
                byte[] dexData = readDexFromApk();
                splitPrimaryDexFromShellDex(dexData,dex.getAbsolutePath());
            }
            Object currentActivityThread = RefInvoke.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", new Class[] {}, new Object[] {});
            String packageName = getPackageName();
            WeakReference wr;
            if(Build.VERSION.SDK_INT < 19){
                HashMap mPackages = (HashMap) RefInvoke.getFieldOjbect(
                        "android.app.ActivityThread", currentActivityThread,
                        "mPackages");
                wr = (WeakReference) mPackages.get(packageName);
            } else {
                ArrayMap mPackages = (ArrayMap) RefInvoke.getFieldOjbect(
                        "android.app.ActivityThread", currentActivityThread,
                        "mPackages");
                wr = (WeakReference) mPackages.get(packageName);
            }
            //找到dex并通过DexClassLoader去加载
            StringBuffer dexPaths = new StringBuffer();
            for(File file:dex.listFiles()){
                dexPaths.append(file.getAbsolutePath());
                dexPaths.append(File.pathSeparator);
            }
            dexPaths.delete(dexPaths.length()-1,dexPaths.length());
            LogUtils.d(dexPaths.toString());
            DexClassLoader classLoader = new DexClassLoader(dexPaths.toString(), odex.getAbsolutePath(),getApplicationInfo().nativeLibraryDir,(ClassLoader) RefInvoke.getFieldOjbect(
                    "android.app.LoadedApk", wr.get(), "mClassLoader"));
            RefInvoke.setFieldOjbect("android.app.LoadedApk", "mClassLoader",
                    wr.get(), classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 从apk中读取dex文件
     * @return
     * @throws IOException
     */
    public byte[] readDexFromApk() throws IOException {
        ByteArrayOutputStream dexByteArrayOutputSteam = new ByteArrayOutputStream();
        System.out.println(getApplicationInfo().sourceDir);
        ZipInputStream localZipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(getApplicationInfo().sourceDir)));
        while(true){
            ZipEntry localZipEntry = localZipInputStream.getNextEntry();
            if(localZipEntry == null){
                localZipInputStream.close();
                break;
            }
            if(localZipEntry.getName().equals("classes.dex")){
                byte[] arrayOfByte = new byte[1024];
                while (true){
                    int i = localZipInputStream.read(arrayOfByte);
                    if(i == -1) break;
                    dexByteArrayOutputSteam.write(arrayOfByte,0,i);
                }
            }
        }
        localZipInputStream.close();
        return dexByteArrayOutputSteam.toByteArray();
    }

    /**
     * 从壳的dex文件中分离出原来的dex文件
     * @param data
     * @param primaryDexDir
     * @throws IOException
     */
    public void splitPrimaryDexFromShellDex(byte[] data, String primaryDexDir) throws IOException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        int shellDexLen = data.length;
        byte[] dexFileCommentLenByte = new byte[4];//dex信息长度
        System.arraycopy(data, shellDexLen-4, dexFileCommentLenByte, 0, 4);
        ByteArrayInputStream bais = new ByteArrayInputStream(dexFileCommentLenByte);
        DataInputStream in = new DataInputStream(bais);
        int dexFileCommentLen = in.readInt();

        byte[] dexFileCommentByte = new byte[dexFileCommentLen];//dex信息正文
        System.arraycopy(data,shellDexLen-4-dexFileCommentLen,dexFileCommentByte,0,dexFileCommentLen);
        String dexFileComment = new String(dexFileCommentByte);
        LogUtils.d("dex comment:"+dexFileComment);
        ArrayList<DexFile> dexFileArrayList = (ArrayList<DexFile>) JSON.parseArray(dexFileComment,DexFile.class);
        int currentReadEndIndex = shellDexLen - 4 - dexFileCommentLen;//当前已经读取到的内容的下标
        for(int i = dexFileArrayList.size()-1; i>=0; i--){//取出所有的dex,并写入到payload_dex目录下
            DexFile dexFile = dexFileArrayList.get(i);
            byte[] primaryDexData = new byte[dexFile.getDexLength()];
            System.arraycopy(data,currentReadEndIndex-dexFile.getDexLength(),primaryDexData,0,dexFile.getDexLength());
            primaryDexData = decryAES(primaryDexData);//界面
            File primaryDexFile = new File(primaryDexDir,dexFile.getDexName());
            if(!primaryDexFile.exists()) primaryDexFile.createNewFile();
            FileOutputStream localFileOutputStream = new FileOutputStream(primaryDexFile);
            localFileOutputStream.write(primaryDexData);
            localFileOutputStream.close();

            currentReadEndIndex -= dexFile.getDexLength();
        }
    }

    public byte[] decryAES(byte[] data) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return SecurityUtils.AESDecrypt(Constant.AES_PRIVATE_KEY,Constant.AES_IV,Constant.AES_TYPE,data);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void onCreate() {
        String appClassName = null;
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if(bundle != null && bundle.containsKey("APPLICATION_CLASS_NAME")){
                appClassName = bundle.getString("APPLICATION_CLASS_NAME");
            }else{
                super.onCreate();
                return;
            }
            LogUtils.d("appClassName:"+appClassName);
            // 有值的话调用该Applicaiton
            Object currentActivityThread = RefInvoke.invokeStaticMethod(
                    "android.app.ActivityThread", "currentActivityThread",
                    new Class[] {}, new Object[] {});
            Object mBoundApplication = RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread", currentActivityThread,
                    "mBoundApplication");
            Object loadedApkInfo = RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread$AppBindData",
                    mBoundApplication, "info");
            // 把当前进程的mApplication 设置成了null
            RefInvoke.setFieldOjbect("android.app.LoadedApk", "mApplication",
                    loadedApkInfo, null);
            Object oldApplication = RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread", currentActivityThread,
                    "mInitialApplication");
            // http://www.codeceo.com/article/android-context.html
            ArrayList<Application> mAllApplications = (ArrayList<Application>) RefInvoke
                    .getFieldOjbect("android.app.ActivityThread",
                            currentActivityThread, "mAllApplications");
            mAllApplications.remove(oldApplication);// 删除oldApplication

            ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) RefInvoke
                    .getFieldOjbect("android.app.LoadedApk", loadedApkInfo,
                            "mApplicationInfo");
            ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) RefInvoke
                    .getFieldOjbect("android.app.ActivityThread$AppBindData",
                            mBoundApplication, "appInfo");
            appinfo_In_LoadedApk.className = appClassName;
            appinfo_In_AppBindData.className = appClassName;
            Application app = (Application) RefInvoke.invokeMethod(
                    "android.app.LoadedApk", "makeApplication", loadedApkInfo,
                    new Class[] { boolean.class, Instrumentation.class },
                    new Object[] { false, null });// 执行
            // makeApplication（false,null）
            RefInvoke.setFieldOjbect("android.app.ActivityThread",
                    "mInitialApplication", currentActivityThread, app);

            Iterator it;
            if(Build.VERSION.SDK_INT < 19){
                // 解决了类型强转错误的问题，原因：
                // 4.4以下系统 mProviderMap 的类型是 HashMap
                // 4.4以上系统 mProviderMap 的类型是 ArrayMap
                HashMap mProviderMap = (HashMap) RefInvoke.getFieldOjbect(
                        "android.app.ActivityThread", currentActivityThread,
                        "mProviderMap");
                it = mProviderMap.values().iterator();
            }else{
                ArrayMap mProviderMap = (ArrayMap) RefInvoke.getFieldOjbect(
                        "android.app.ActivityThread", currentActivityThread,
                        "mProviderMap");
                it = mProviderMap.values().iterator();
            }
            while (it.hasNext()) {
                Object providerClientRecord = it.next();
                Object localProvider = RefInvoke.getFieldOjbect(
                        "android.app.ActivityThread$ProviderClientRecord",
                        providerClientRecord, "mLocalProvider");
                RefInvoke.setFieldOjbect("android.content.ContentProvider",
                        "mContext", localProvider, app);
            }
            app.onCreate();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            super.onCreate();
        }
    }
}
