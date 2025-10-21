package com.nexon.nutriai.util;

public class ThreadLocalUtil {
    public static final ThreadLocal<String> THREAD_LOCAL_PHONE = new ThreadLocal<>();
    public static final ThreadLocal<String> THREAD_LOCAL_CHAT_ID = new ThreadLocal<>();

    // 获取phone值
    public static String getPhone() {
        return THREAD_LOCAL_PHONE.get();
    }

    // 获取chatId值
    public static String getChatId() {
        return THREAD_LOCAL_CHAT_ID.get();
    }

    // 清理所有ThreadLocal变量
    public static void clearAll() {
        THREAD_LOCAL_PHONE.remove();
        THREAD_LOCAL_CHAT_ID.remove();
    }
}
