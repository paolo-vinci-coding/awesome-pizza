package com.awesomepizza.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OrderCodeGenerator {

    private static final String PREFIX = "AP-";
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(PREFIX);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
