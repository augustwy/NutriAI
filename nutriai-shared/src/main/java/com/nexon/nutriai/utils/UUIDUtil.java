package com.nexon.nutriai.utils;

import java.util.UUID;

/**
 * UUID工具类，用于生成UUID字符串
 */
public class UUIDUtil {
    
    /**
     * 生成标准UUID字符串
     * @return 36位UUID字符串（包含连字符）
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 生成不带连字符的UUID字符串
     * @return 32位UUID字符串（不含连字符）
     */
    public static String generateUUIDWithoutHyphen() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成指定长度的UUID字符串（最多32位）
     * @param length UUID字符串长度
     * @return 指定长度的UUID字符串
     */
    public static String generateShortUUID(int length) {
        if (length <= 0 || length > 32) {
            throw new IllegalArgumentException("Length must be between 1 and 32");
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

    static void main() {
        System.out.println(generateUUID());
    }
}
