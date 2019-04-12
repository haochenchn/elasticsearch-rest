package com.aaron.system.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class DESUtils {
        
        /** 加密算法,可用 DES,DESede,Blowfish */  
        private final static String ALGORITHM = "DES/ECB/PKCS5Padding";  
       //加密时不足8字节的内容会用PKCS5Padding填充补足为8字节，正好8字节的整数倍则会再填充补足一个8字节
       //ECB是分组加密的模式，最常用的是CBC和ECB;
       //PKCS5Padding是最后一个分组的填充方式,大部分情况明文并非恰好是64位的倍数，所以长度小于64位，需用数据填充至64位，填充方式则是PKCS5Padding
        
        
        public static void main(String[] args) throws Exception {  
            String data = "hxtest_123456780";  
            String str = HexConvert.parseByte2HexStr(DESUtils.encryptV3(data.getBytes(),HexConvert.parseHexStr2Byte("11111111")));
            System.out.println("普通加密后: " + str);  
            
            String str3=new String(DESUtils.decryptV3(HexConvert.parseHexStr2Byte(str), HexConvert.parseHexStr2Byte("11111111")));
            System.out.println("解密后: " + str3); 
        }  
          
          
        /** 
         * 用指定的key对数据进行DES加密. 
         * @param data 待加密的数据 
         * @param key DES加密的key 
         * @return 返回DES加密后的数据 
         * @throws Exception 
         * version 1.0
         */  
        public static byte[] encrypt(byte[] data, byte[] key) throws Exception {  
            // DES算法要求有一个可信任的随机数源  
            SecureRandom sr = new SecureRandom();  
            // 从原始密匙数据创建DESKeySpec对象  
            DESKeySpec dks = new DESKeySpec(key);  
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成  
            // 一个SecretKey对象  
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");  
            SecretKey securekey = keyFactory.generateSecret(dks);  
            // Cipher对象实际完成加密操作  
            Cipher cipher = Cipher.getInstance(ALGORITHM);  
            // 用密匙初始化Cipher对象  
            cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);  
            // 执行加密操作  
            return cipher.doFinal(data);  
        }  
        
        /** 
         * 用指定的key对数据进行DES加密. 
         * @param data 待加密的数据 
         * @param password DES加密的key
         * @return 返回DES加密后的数据 
         * @throws Exception 
         * version 1.2
         */  
        public static byte[] encryptV2(byte[] data, byte[] password) throws Exception {  
        	 SecretKeySpec key = new SecretKeySpec(password, "DES");  
             Cipher cipher = Cipher.getInstance(ALGORITHM);// 创建密码器  
             cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化  
             byte[] result = cipher.doFinal(data);  
            return result;  
        }
        /** 
         * 用指定的key对数据进行DES加密. 
         * @param data 待加密的数据 
         * @param password DES加密的key
         * @return 返回DES加密后的数据 
         * @throws Exception 
         * version 1.3
         */  
        public static byte[] encryptV3(byte[] data, byte[] password) throws Exception {  
       	 DESKeySpec dks=new DESKeySpec(password);
    	 SecretKeyFactory keyFactory=SecretKeyFactory.getInstance("DES");
    	 SecretKey secretKey=keyFactory.generateSecret(dks);
         Cipher cipher = Cipher.getInstance(ALGORITHM);// 创建密码器  
         cipher.init(Cipher.ENCRYPT_MODE, secretKey);// 初始化  
         byte[] result = cipher.doFinal(data);  
        return result;  
        }  
        /** 
         * 用指定的key对数据进行DES解密. 
         * @param data 待解密的数据 
         * @param key DES解密的key 
         * @return 返回DES解密后的数据 
         * @throws Exception 
         * version 1.0
         */  
        public static byte[] decrypt(byte[] data, byte[] key) throws Exception {  
            // DES算法要求有一个可信任的随机数源  
            SecureRandom sr = new SecureRandom();  
            // 从原始密匙数据创建一个DESKeySpec对象  
            DESKeySpec dks = new DESKeySpec(key);  
            // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成  
            // 一个SecretKey对象  
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");  
            SecretKey securekey = keyFactory.generateSecret(dks);  
            // Cipher对象实际完成解密操作  
            Cipher cipher = Cipher.getInstance(ALGORITHM);  
            // 用密匙初始化Cipher对象  
            cipher.init(Cipher.DECRYPT_MODE, securekey, sr);  
            // 现在，获取数据并解密  
            // 正式执行解密操作  
            return cipher.doFinal(data);  
        }
        /** 
         * 用指定的key对数据进行DES解密. 
         * @param data 待解密的数据 
         * @param password DES解密的key
         * @return 返回DES解密后的数据 
         * @throws Exception 
         * version 1.2
         */  
        public static byte[] decryptV2(byte[] data, byte[] password) throws Exception {  
        	 SecretKeySpec key = new SecretKeySpec(password, "DES");  
             Cipher cipher = Cipher.getInstance(ALGORITHM);// 创建密码器  
             cipher.init(Cipher.DECRYPT_MODE, key);// 初始化  
             byte[] result = cipher.doFinal(data);  
            return result;  
        }
        
        /** 
         * 用指定的key对数据进行DES解密. 
         * @param data 待解密的数据 
         * @param password DES解密的key
         * @return 返回DES解密后的数据 
         * @throws Exception
         * version 1.3
         *
         */  
        public static byte[] decryptV3(byte[] data, byte[] password) throws Exception {  
        	 DESKeySpec dks=new DESKeySpec(password);
        	 SecretKeyFactory keyFactory=SecretKeyFactory.getInstance("DES");
        	 SecretKey secretKey=keyFactory.generateSecret(dks);
             Cipher cipher = Cipher.getInstance(ALGORITHM);// 创建密码器  
             cipher.init(Cipher.DECRYPT_MODE, secretKey);// 初始化  
             byte[] result = cipher.doFinal(data);  
            return result;  
        }  
}
