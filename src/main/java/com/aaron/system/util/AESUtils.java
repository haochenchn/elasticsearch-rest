package com.aaron.system.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



/**
 *@ClassName:AESUtils
 *@Description:aes加密
 *@author 江大润
 *@date 2015年8月27日
 *
 */
public class AESUtils {
	static final String algorithmStr="AES/ECB/PKCS5Padding"; //加密算法/模式和填充算法
	//加密时不足16字节的内容会用PKCS5Padding填充补足为16字节，正好16字节的整数倍则会再填充补足一个16字节
	static final int codeLen=128; //加密密钥长度
	
	
	public static void main(String[] args) throws Exception {
	    String content = "wosh龙的传";  
	    String password = "61626365313233343536373839303132";//AES密钥长度只能是128或192或256位，否则出错  
	    //加密  
	    System.out.println("加密前：" + content);  
	    byte[] encryptResult = encryptV2(content, password);  
	    String encryptResultStr =HexConvert.parseByte2HexStr(encryptResult);  
	    System.out.println("加密后：" +encryptResultStr);  
	    //解密  
	    byte[] decryptFrom = HexConvert.parseHexStr2Byte(encryptResultStr);  
	    byte[] decryptResult = decryptV2(decryptFrom,password);  
	    System.out.println("解密后：" + new String(decryptResult));  
		
		
	/*	String data="b5f20e3c75aa55f40f0f7c52ac98e66becd2a63c15a30b8322d88c1282a7ebdc0b6ad2c4e5432eeea618ebe45387df63b5452d86530f54cd3ef816280c7b017491ab9b33a102903219a97a267f7fd4676ee07f33bdd5959fd8f0270a3138fcb4dfd3de3e39898f35f027135b60f7205415ef5313f038e1d12580d3895cd1bb2c35160302b7f6762c5665602009e72545532b7ffbebfc1494ffd33fc37b1d492248f33dc792b6558ccd7a20bcc40db205";
	    String password="A397A25553BEF1FCF9796B521413E9E2";
	    byte[] srcdata=HexConvert.parseHexStr2Byte(data);
	    String mingwen=new String(decryptV2(srcdata,password));
	    System.err.println("明文:"+mingwen);*/
	    
	}
    /** 
     * 加密 
     *  密钥需要随即源
     * @param content 需要加密的内容 
     * @param password  加密密码 
     * @return 
     */  
    public static byte[] encrypt(String content, String password) {  
            try {             
                    KeyGenerator kgen = KeyGenerator.getInstance("AES");  
                    kgen.init(codeLen, new SecureRandom(HexConvert.parseHexStr2Byte(password)));  
                    //kgen.init(codeLen, new SecureRandom(password.getBytes()));  
                    SecretKey secretKey = kgen.generateKey();  
                    byte[] enCodeFormat = secretKey.getEncoded();  
                    SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");  
                    Cipher cipher = Cipher.getInstance(algorithmStr);// 创建密码器  
                    byte[] byteContent = content.getBytes("utf-8"); 
                    System.out.println("加密前内容:"+HexConvert.parseByte2HexStr(byteContent));
                    cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化 
                    byte[] result = cipher.doFinal(byteContent);  
                    return result; // 加密  
            } catch (NoSuchAlgorithmException e) {  
                    e.printStackTrace();  
            } catch (NoSuchPaddingException e) {  
                    e.printStackTrace();  
            } catch (InvalidKeyException e) {  
                    e.printStackTrace();  
            } catch (UnsupportedEncodingException e) {  
                    e.printStackTrace();  
            } catch (IllegalBlockSizeException e) {  
                    e.printStackTrace();  
            } catch (BadPaddingException e) {  
                    e.printStackTrace();  
            }  
            return null;  
    }  
    
    /** 
     * 加密 
     *  密钥无需随即源
     * @param content 需要加密的内容 
     * @param password  加密密码 
     * @return 
     */  
    public static byte[] encryptV2(String content, String password) {  
            try {             
                    SecretKeySpec key = new SecretKeySpec(HexConvert.parseHexStr2Byte(password), "AES");  
                    Cipher cipher = Cipher.getInstance(algorithmStr);// 创建密码器  
                    byte[] byteContent = content.getBytes("utf-8"); 
                    System.out.println("加密前内容:"+new String(byteContent));
                    cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化  
                    byte[] result = cipher.doFinal(byteContent);  
                    return result; // 加密  
            } catch (NoSuchAlgorithmException e) {  
                    e.printStackTrace();  
            } catch (NoSuchPaddingException e) {  
                    e.printStackTrace();  
            } catch (InvalidKeyException e) {  
                    e.printStackTrace();  
            } catch (UnsupportedEncodingException e) {  
                    e.printStackTrace();  
            } catch (IllegalBlockSizeException e) {  
                    e.printStackTrace();  
            } catch (BadPaddingException e) {  
                    e.printStackTrace();  
            }  
            return null;  
    }  
    
    /**解密 
     * 密钥需要随即源
     * @param content  待解密内容 
     * @param password 解密密钥 
     * @return 
     */  
    public static byte[] decrypt(byte[] content, String password) {  
            try {  
                     KeyGenerator kgen = KeyGenerator.getInstance("AES");  
                     kgen.init(codeLen,new SecureRandom(HexConvert.parseHexStr2Byte(password)));  
                     SecretKey secretKey = kgen.generateKey();  
                     byte[] enCodeFormat = secretKey.getEncoded();  
                     SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");              
                     Cipher cipher = Cipher.getInstance(algorithmStr);// 创建密码器  
                    cipher.init(Cipher.DECRYPT_MODE, key);// 初始化  
                    byte[] result = cipher.doFinal(content);  
                    return result; // 加密  
            } catch (NoSuchAlgorithmException e) {  
                    e.printStackTrace();  
            } catch (NoSuchPaddingException e) {  
                    e.printStackTrace();  
            } catch (InvalidKeyException e) {  
                    e.printStackTrace();  
            } catch (IllegalBlockSizeException e) {  
                    e.printStackTrace();  
            } catch (BadPaddingException e) {  
                    e.printStackTrace();  
            }  
            return null;  
    }  
    
    /**解密
     * 密钥无需随即源 
     * @param content  待解密内容 
     * @param password 解密密钥 
     * @return 
     */  
    public static byte[] decryptV2(byte[] content, String password) {  
            try {  
                     SecretKeySpec key = new SecretKeySpec(HexConvert.parseHexStr2Byte(password), "AES");              
                     Cipher cipher = Cipher.getInstance(algorithmStr);// 创建密码器  
                    cipher.init(Cipher.DECRYPT_MODE, key);// 初始化  
                    byte[] result = cipher.doFinal(content);  
                    return result; // 加密  
            } catch (NoSuchAlgorithmException e) {  
                    e.printStackTrace();  
            } catch (NoSuchPaddingException e) {  
                    e.printStackTrace();  
            } catch (InvalidKeyException e) {  
                    e.printStackTrace();  
            } catch (IllegalBlockSizeException e) {  
                    e.printStackTrace();  
            } catch (BadPaddingException e) {  
                    e.printStackTrace();  
            }  
            return null;  
    }  
}
