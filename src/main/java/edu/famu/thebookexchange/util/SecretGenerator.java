package edu.famu.thebookexchange.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretGenerator {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[64];
        random.nextBytes(key);
        String secret = Base64.getEncoder().encodeToString(key);
        System.out.println(secret);
    }

}
