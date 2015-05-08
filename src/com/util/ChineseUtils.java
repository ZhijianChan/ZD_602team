package com.util;

import net.sourceforge.pinyin4j.*;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class ChineseUtils {
	private String[] pinyin;
	private HanyuPinyinOutputFormat format;
	
	private static boolean isNotEmojiChar(char c) {
		return (c == 0x0) ||
				(c == 0x9) ||
				(c == 0xA) ||
				(c == 0xD) ||
				((c >= 0x20) && (c <= 0xD7FF)) ||
				((c >= 0xE000) && (c <= 0xFFFD)) ||
				((c >= 0x10000) && (c <= 0x10FFFF));
	}
	public ChineseUtils() {
		format = new HanyuPinyinOutputFormat();
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	}
	
	private static final boolean isChinese(char c) {  
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
            return true;  
        }  
        return false;  
    }
	
	public static final boolean isChinese(String str) {  
        for (int i = 0; i < str.length(); i++) {  
            char c = str.charAt(i);  
            if (isChinese(c)) {  
                return true;  
            }  
        }  
        return false;  
    }
	
	public String getPinYinChar(char c) {
		try {
			pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
		}
		catch(BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		
		if (pinyin == null)
			return null;
		return pinyin[0];
	}
	
	public String getPinYinStr(String str) {
		StringBuilder sb = new StringBuilder();
		String tmp = null;
		for (int i = 0; i < str.length(); ++i) {
			if (!isNotEmojiChar(str.charAt(i))) {
				continue;
			}
			tmp = getPinYinChar(str.charAt(i));
			if (tmp == null) {
				sb.append(str.charAt(i));
			}
			else {
				sb.append(tmp);
			}
		}
		return sb.toString();
	}
	
	public String getShortPinYinStr(String str) {
		StringBuilder sb = new StringBuilder();
		String tmp = null;
		for (int i = 0; i < str.length(); ++i) {
			if (!isNotEmojiChar(str.charAt(i))) {
				continue;
			}
			tmp = getPinYinChar(str.charAt(i));
			if (tmp == null) {
				sb.append(str.charAt(i));
			}
			else {
				sb.append(tmp.charAt(0));
			}
		}
		return sb.toString();
	}
}
