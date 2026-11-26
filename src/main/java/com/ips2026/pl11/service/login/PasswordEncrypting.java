package com.ips2026.pl11.service.login;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncrypting {
    
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
    
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

}
