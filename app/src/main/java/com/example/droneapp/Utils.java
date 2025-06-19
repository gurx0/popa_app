package com.example.droneapp;

public class Utils {
    public static int bytes2int(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value |= (bytes[i] & 0xFF) << (24 - i * 8);
        }
        return value;
    }
}