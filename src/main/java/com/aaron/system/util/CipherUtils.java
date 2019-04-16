package com.aaron.system.util;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
  
import javax.crypto.Cipher;   
import javax.crypto.KeyGenerator;   
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 使用AES对文件进行加密和解密
 *
 * @author Aaron
 * @since 20180826
 */
public class CipherUtils {
    /**
     * 使用AES对文件进行加密和解密
     */
    private static String type = "AES";

    private static File mkdirFiles(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        return file;
    }

    /**
     * 生成指定字符串的密钥(解决linux上加密成功解密失败问题)
     * @param secret 要生成密钥的字符串
     * @return
     * @throws GeneralSecurityException
     */
    private static Key getKey(String secret) throws GeneralSecurityException {
        KeyGenerator kgen = KeyGenerator.getInstance(type);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(secret.getBytes());
        kgen.init(128, secureRandom);
        SecretKey secretKey = kgen.generateKey();
        byte[] encodeFormat = secretKey.getEncoded();
        SecretKeySpec secretKey2 = new SecretKeySpec(encodeFormat, type);
        return secretKey2;
    }

    /**
     * 生成指定字符串的密钥
     * @param secret 要生成密钥的字符串
     * @return secretKey    生成后的密钥
     * @throws GeneralSecurityException
     */
    private static Key getKey2(String secret) throws GeneralSecurityException {
        KeyGenerator kgen = KeyGenerator.getInstance(type);
        kgen.init(128, new SecureRandom(secret.getBytes()));
        SecretKey secretKey = kgen.generateKey();
        return secretKey;
    }

    private static Key getKey3(String secret) throws GeneralSecurityException {
        KeyGenerator kgen = KeyGenerator.getInstance(type);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(secret.getBytes());
        kgen.init(128, secureRandom);
        SecretKey secretKey = kgen.generateKey();
        return secretKey;

    }

    /**
     * 创建密码器
     * @param type       加密类型，AES
     * @param privateKey 私钥
     * @param mode       Cipher.ENCRYPT_MODE、Cipher.DECRYPT_MODE
     */
    private static Cipher getCipher(String type, String privateKey, int mode) throws GeneralSecurityException {
        Key key = getKey(privateKey);
        Cipher cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding");
        cipher.init(mode, key);
        return cipher;
    }

    /**
     * 加密解密流
     * @param in     加密解密前的流
     * @param out    加密解密后的流
     * @param cipher 加密解密
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void crypt(InputStream in, OutputStream out, Cipher cipher) throws IOException, GeneralSecurityException {
        int blockSize = cipher.getBlockSize() * 1000;
        int outputSize = cipher.getOutputSize(blockSize);
        byte[] inBytes = new byte[blockSize];
        byte[] outBytes = new byte[outputSize];
        int inLength = 0;
        boolean more = true;
        while (more) {
            inLength = in.read(inBytes);
            if (inLength == blockSize) {
                int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
                out.write(outBytes, 0, outLength);
            } else {
                more = false;
            }
        }
        if (inLength > 0)
            outBytes = cipher.doFinal(inBytes, 0, inLength);
        else
            outBytes = cipher.doFinal();
        out.write(outBytes);
    }

    /**
     * 加解密
     * @param srcFile  加密文件路径
     * @param destFile 解密文件路径
     * @param cipher   加密器/解密器
     */
    public static void crypt(String srcFile, String destFile, Cipher cipher) throws IOException, GeneralSecurityException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(mkdirFiles(destFile));
            crypt(fis, fos, cipher);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Cipher cipher = getCipher(type, "amN5QDEyMzQ1Ng==", Cipher.DECRYPT_MODE);
        crypt("G:\\test\\idea_enc.txt", "G:\\test\\idea_dec.txt", cipher);
    }
}  
