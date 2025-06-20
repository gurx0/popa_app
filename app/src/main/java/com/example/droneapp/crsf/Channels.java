package com.example.droneapp.crsf;

public class Channels {
    public int[] values;

    public Channels() {
        values = new int[16];
        for (int i = 0; i < values.length; i++) {
            values[i] = 1000;
        }
    }
}