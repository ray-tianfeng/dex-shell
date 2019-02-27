package com.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author zl
 * @time 2019/2/26 0026.
 */
public class SecurityUtils {
    /*
     * 加密
     * 1.构造密钥生成器
     * 2.根据ecnodeRules规则初始化密钥生成器
     * 3.产生密钥
     * 4.创建和初始化密码器
     * 5.内容加密
     * 6.返回字符串
     */
    public static byte[] AESEncryption(String privateKey,String iv,String AES_TYPE,byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        IvParameterSpec zeroIv = new IvParameterSpec(iv.getBytes());
        //两个参数，第一个为私钥字节数组， 第二个为加密方式 AES或者DES
        SecretKeySpec key = new SecretKeySpec(privateKey.getBytes(), "AES");
        //实例化加密类，参数为加密方式，要写全
        Cipher cipher = Cipher.getInstance(AES_TYPE); //PKCS5Padding比PKCS7Padding效率高，PKCS7Padding可支持IOS加解密
        //初始化，此方法可以采用三种方式，按加密算法要求来添加。（1）无第三个参数（2）第三个参数为SecureRandom random = new SecureRandom();中random对象，随机数。(AES不可采用这种方法)（3）采用此代码中的IVParameterSpec
        //加密时使用:ENCRYPT_MODE;  解密时使用:DECRYPT_MODE;
        cipher.init(Cipher.ENCRYPT_MODE, key); //CBC类型的可以在第三个参数传递偏移量zeroIv,ECB没有偏移量
        //加密操作,返回加密后的字节数组，然后需要编码。主要编解码方式有Base64, HEX, UUE,7bit等等。此处看服务器需要什么编码方式
        byte[] encryptedData = cipher.doFinal(data);
        return encryptedData;
    }
    /*
     * 解密
     * 解密过程：
     * 1.同加密1-4步
     * 2.将加密后的字符串反纺成byte[]数组
     * 3.将加密内容解密
     */
    public static byte[] AESDecrypt(String privateKey,String iv,String AES_TYPE,byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec zeroIv = new IvParameterSpec(iv.getBytes());
        SecretKeySpec key = new SecretKeySpec(privateKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(AES_TYPE);
        //与加密时不同MODE:Cipher.DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, key);  //CBC类型的可以在第三个参数传递偏移量zeroIv,ECB没有偏移量
        byte[] decryptedData = cipher.doFinal(data);
        return decryptedData;
    }

}
