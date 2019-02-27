package com.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by zoulong on 2019/2/25 0025.
 */
public class FileUtils {
    public static boolean isExit(String path){
        if(path == null && path.length() ==0) return false;
        File file = new File(path);
        return file.exists();
    }

    public static boolean isExit(String ... paths){
        for(String path:paths){
            if(!isExit(path)) return false;
        }
        return true;
    }

    public static boolean unZip(String zipPath,String dstPath){
        if(!isExit(zipPath)) return false;
        try {
            ZipFile zip = new ZipFile(zipPath, Charset.forName("GBK"));
            Enumeration<ZipEntry> em = (Enumeration<ZipEntry>) zip.entries();
            while (em != null && em.hasMoreElements()){
                ZipEntry ze = em.nextElement();
                String outPath = dstPath + File.separator + ze.getName();
                File dstFile = new File(outPath);
                if(!dstFile.exists()){
                    dstFile.getParentFile().mkdirs();
                }
                InputStream in = zip.getInputStream(ze);
                FileOutputStream out = new FileOutputStream(outPath);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0){
                    out.write(buf,0,len);
                }
                in.close();
                out.close();
            }
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 压缩成ZIP 方法1
     * @param srcDir 压缩文件夹路径
     * @param outPath    压缩文件输出流
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(String srcDir, String outPath, boolean KeepDirStructure)
            throws RuntimeException{
        ZipOutputStream zos = null ;
        try {
            zos = new ZipOutputStream(new FileOutputStream(outPath));
            File sourceFile = new File(srcDir);
            File[] dirFiles = new File(srcDir).listFiles();
            for(File file:dirFiles)
                compress(file,zos,file.getName(),KeepDirStructure);
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils",e);
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos        zip输出流
     * @param name       压缩后的名称
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name,boolean KeepDirStructure) throws Exception{
        byte[] buf = new byte[1024];
        if(sourceFile.isFile()){
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0){
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if(KeepDirStructure){
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            }else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(),KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(),KeepDirStructure);
                    }
                }
            }
        }
    }

    public static boolean delete(String path){
        if(!isExit(path)) return true;
        File file = new File(path);
        if(file.isFile()){
            return file.delete();
        }else{
            if(file.listFiles().length == 0) file.delete();
            else{
                for(File tempFile : file.listFiles()){
                    delete(tempFile.getAbsolutePath());
                }
                return file.delete();
            }
        }
        return true;
    }
}
