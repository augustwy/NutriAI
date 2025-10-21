package com.nexon.nutriai.util;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class PasswordUtil {
    // 生成哈希密码
    public static String hashPassword(String password, String salt) {
        // 使用bcrypt生成带盐值的哈希
        return OpenBSDBCrypt.generate(password.getBytes(), salt.getBytes(), 10);
    }
    
    // 验证密码
    public static boolean verifyPassword(String password, String hashedPassword) {
        return OpenBSDBCrypt.checkPassword(hashedPassword, password.getBytes());
    }
}
