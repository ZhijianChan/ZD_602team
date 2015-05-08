package com.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>
 * AES加密解密工具包
 * </p>
 * 
 * @date 2012-5-18
 * @version 1.0
 */
public class AESUtils {

	private static final String ALGORITHM = "AES";
	private static final int KEY_SIZE = 128;
	private static String key = "";
	private static String mi = "";
	private static String outputStr = "";
	private static String original = "";

	static {
		try {
			key = AESUtils.getSecretKey("21da");// 传入密钥种子，生成密钥
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getCiphertext() {
		return mi;
	}
	
	public static String getOriginal() {
		return original;
	}
	
	public static void enAES(String original) throws Exception {

		byte[] inputData = original.getBytes();
		byte[] encodedData = encrypt(inputData, key);
		mi = Base64Utils.encode(encodedData);
		//System.err.println("加密后:\t" + mi);
	}// 对明文进行加密

	public static void deAES(String ciphertext) throws Exception {

		byte[] encodedData = Base64Utils.decode(ciphertext);

		byte[] outputData = AESUtils.decrypt(encodedData, key);
		
		original = new String(outputData);
		
		Log.i("AESUtils", original);

	}// 解密生成明文

	/**
	 * <p>
	 * 生成随机密钥
	 * </p>
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getSecretKey() throws Exception {
		return getSecretKey(null);
	}

	/**
	 * <p>
	 * 生成密钥
	 * </p>
	 * 
	 * @param seed
	 *            密钥种子
	 * @return
	 * @throws Exception
	 */
	@SuppressLint("TrulyRandom")
	public static String getSecretKey(String seed) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		SecureRandom secureRandom;
		if (seed != null && !"".equals(seed)) {
			secureRandom = new SecureRandom(seed.getBytes());
		} else {
			secureRandom = new SecureRandom();
		}
		keyGenerator.init(KEY_SIZE, secureRandom);
		SecretKey secretKey = keyGenerator.generateKey();
		return Base64Utils.encode(secretKey.getEncoded());
	}

	/**
	 * <p>
	 * 加密
	 * </p>
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, String key) throws Exception {
		Key k = toKey(Base64Utils.decode(key));
		byte[] raw = k.getEncoded();
		SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		return cipher.doFinal(data);
	}

	/**
	 * <p>
	 * 解密
	 * </p>
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, String key) throws Exception {
		Key k = toKey(Base64Utils.decode(key));
		byte[] raw = k.getEncoded();
		SecretKeySpec secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		return cipher.doFinal(data);
	}

	/**
	 * <p>
	 * 转换密钥
	 * </p>
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private static Key toKey(byte[] key) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
		return secretKey;
	}
	
	//  =============================================================================================
	
	/**
	 * another encrypt
	 * @param seed
	 * @param cleartext
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String seed, String cleartext) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}

	public static String decrypt(String seed, String encrypted) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}


	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
		sr.setSeed(seed);
		   kgen.init(128, sr); // 192 and 256 bits may not be available
		   SecretKey skey = kgen.generateKey();
		   byte[] raw = skey.getEncoded();
		   return raw;
	}



	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		   SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		   cipher.init(Cipher.ENCRYPT_MODE, skeySpec,new IvParameterSpec(new byte[cipher.getBlockSize()]));
		   byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}


	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		   SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		   cipher.init(Cipher.DECRYPT_MODE, skeySpec,new IvParameterSpec(new byte[cipher.getBlockSize()]));
		   byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}


	private static String toHex(String txt) {
		return toHex(txt.getBytes());
	}
	
	private static String fromHex(String hex) {
		return new String(toByte(hex));
	}

	private static byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
		result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}


	private static String toHex(byte[] buf) {
		if (buf == null)
		return "";
		StringBuffer result = new StringBuffer(2*buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}
	
	private final static String HEX = "0123456789ABCDEF";
	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
}