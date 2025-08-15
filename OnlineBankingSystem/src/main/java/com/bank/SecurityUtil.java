package com.bank;

import org.mindrot.jbcrypt.BCrypt;

public class SecurityUtil {

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String plainPassword, String hashed) {
        return hashed != null && BCrypt.checkpw(plainPassword, hashed);
    }
}
