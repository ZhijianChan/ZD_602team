package com.util;

import it.sauronsoftware.base64.Base64;

/**
 * <p>
 * BASE64������빤�߰�
 * </p>
 * <p>
 * ����javabase64-1.3.1.jar
 * </p>
 * 
 */
public class Base64Utils {

	/**
	 * �ļ���ȡ��������С
	 */

	/**
	 * <p>
	 * BASE64�ַ�������Ϊ����������
	 * </p>
	 * 
	 * @param base64
	 * @return
	 * @throws Exception
	 */
	public static byte[] decode(String base64) throws Exception {
		return Base64.decode(base64.getBytes());
	}

	/**
	 * <p>
	 * ���������ݱ���ΪBASE64�ַ���
	 * </p>
	 * 
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static String encode(byte[] bytes) throws Exception {
		return new String(Base64.encode(bytes));
	}

}
