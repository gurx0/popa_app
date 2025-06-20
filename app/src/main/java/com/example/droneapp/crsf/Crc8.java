package com.example.droneapp.crsf;

public class Crc8 {
    private final byte[] lut;

    public Crc8(int poly) {
        lut = new byte[256];
        for (int i = 0; i < 256; i++) {
            int crc = i;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x80) != 0) {
                    crc = (crc << 1) ^ poly;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFF;
            }
            lut[i] = (byte) crc;
        }
    }

    public byte calc(byte[] data) {
        byte crc = 0;
        for (byte b : data) {
            crc = lut[(crc ^ b) & 0xFF];
        }
        return crc;
    }
}