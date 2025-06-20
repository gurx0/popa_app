package com.example.droneapp.crsf;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.droneapp.R;
import com.example.droneapp.crsf.Crc8;
import com.example.droneapp.utils.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoystickMonitorActivity extends AppCompatActivity {
    private static final byte[] PACKAGE_HEADER_BUF = "fengyingdianzi:".getBytes();
    private static final int NUM_CHANNELS = 12;
    private static final String SERIAL_PORT = "/dev/ttyHS1";
    private static final int BAUD_RATE = 420000;
    private static final int SEEK_BAR_MIN = 1000;
    private static final int SEEK_BAR_MAX = 2000;
    private static final int CRSF_ADDRESS_FLIGHT_CONTROLLER = 0xC8;
    private static final int CRSF_FRAMETYPE_RC_CHANNELS_PACKED = 0x16;
    private static final int CRSF_CHANNEL_VALUE_1000 = 191;
    private static final int CRSF_CHANNEL_VALUE_2000 = 1792;
    private static final String[] CHANNEL_NAMES = {
            "Throttle   ",     // CH1
            "Yaw         ",     // CH2
            "Pitch       ",     // CH3
            "Roll         ",     // CH4
            "E switch ",     // CH5
            "F switch ",     // CH6
            "A             ",     // CH7
            "B             ",     // CH8
            "C             ",     // CH9
            "D             ",     // CH10
            "G Roller ",     // CH11
            "H Roller "      // CH12
    };


    private List<SeekBar> seekBarList = new ArrayList<>();
    private List<TextView> tvList = new ArrayList<>();
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private Thread looperThread;
    private Thread readerThread;
    private volatile boolean isRunning = true;
    private Crc8 crc8;
    private Channels channels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick);

        crc8 = new Crc8(0xD5);
        channels = new Channels();
        initUI();
        initSerialPort();
    }

    private void initUI() {
        LinearLayout container = findViewById(R.id.seekbar_container);
        for (int i = 0; i < NUM_CHANNELS; i++) {
            TextView textView = new TextView(this);
            textView.setText("CH" + (i + 1));
            textView.setTextSize(16);
            container.addView(textView);
            tvList.add(textView);

            SeekBar seekBar = new SeekBar(this);
            seekBar.setMax(SEEK_BAR_MAX - SEEK_BAR_MIN);
            seekBar.setEnabled(false);
            container.addView(seekBar);
            seekBarList.add(seekBar);
        }
    }

    private void initSerialPort() {
        try {
            inputStream = new FileInputStream(SERIAL_PORT);
            outputStream = new FileOutputStream(SERIAL_PORT);
            startThreads();
        } catch (IOException e) {
            Log.e("JoystickMonitor", "Serial port error: " + e.getMessage());
            runOnUiThread(() -> tvList.get(0).setText("Ошибка: " + e.getMessage()));
        }
    }

    private void startThreads() {
        looperThread = new Thread(() -> {
            byte[] bArr = new byte[PACKAGE_HEADER_BUF.length + 5];
            System.arraycopy(PACKAGE_HEADER_BUF, 0, bArr, 0, PACKAGE_HEADER_BUF.length);
            bArr[PACKAGE_HEADER_BUF.length] = (byte) 0xB1;
            bArr[PACKAGE_HEADER_BUF.length + 1] = 2;
            bArr[PACKAGE_HEADER_BUF.length + 2] = 'R';
            bArr[PACKAGE_HEADER_BUF.length + 3] = 1;
            bArr[PACKAGE_HEADER_BUF.length + 4] = calculateBCC(bArr);

            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    if (outputStream != null) {
                        outputStream.write(bArr);
                        outputStream.flush();
//                        Log.d("JoystickMonitor", "Sent request: " + bytesToHex(bArr));
                    }
                    sendCrsfPacket();
                    Thread.sleep(20);
                } catch (IOException | InterruptedException e) {
                    Log.e("JoystickMonitor", "LooperThread error: " + e.getMessage());
                }
            }
        });
        looperThread.start();

        readerThread = new Thread(() -> {
            byte[] buffer = new byte[256];
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    if (inputStream != null) {
                        int len = inputStream.read(buffer);
                        if (len > 0) {
//                            Log.d("JoystickMonitor", "Received " + len + " bytes: " + bytesToHex(buffer, len));
                            byte[] data = new byte[len];
                            System.arraycopy(buffer, 0, data, 0, len);
                            received(data);
                        } else {
                            Log.d("JoystickMonitor", "No data received from /dev/ttyHS1");
                        }
                    }
                } catch (IOException e) {
                    Log.e("JoystickMonitor", "IOException: " + e.getMessage());
                }
            }
        });
        readerThread.start();
    }

    private byte calculateBCC(byte[] data) {
        byte bcc = 0;
        for (byte b : data) {
            bcc ^= b;
        }
        return bcc;
    }

    private void received(byte[] datas) {
//        Log.d("JoystickMonitor", "Received packet: " + bytesToHex(datas));
        if (datas.length < PACKAGE_HEADER_BUF.length + 2 || datas[PACKAGE_HEADER_BUF.length] != (byte) 0xB1) {
//            Log.w("JoystickMonitor", "Invalid packet or header");
            return;
        }

        byte length = datas[PACKAGE_HEADER_BUF.length + 1];
        int startIndex = PACKAGE_HEADER_BUF.length + 2;

        for (int i = 0; i < length && i < NUM_CHANNELS * 2; i += 2) {
            int index = i / 2;
            int value = Utils.bytes2int(new byte[]{0, 0, datas[startIndex + i], datas[startIndex + i + 1]});
            channels.values[index] = value;
            setSeekBar(index, value);
        }
    }

    private void setSeekBar(int index, int value) {
        value = Math.max(SEEK_BAR_MIN, Math.min(value, SEEK_BAR_MAX));
        int seekBarValue = value - SEEK_BAR_MIN;
//        Log.d("JoystickMonitor", "SetSeekBar: index=" + index + ", value=" + value + ", seekBarValue=" + seekBarValue);
        int finalValue = value;
        runOnUiThread(() -> {
            seekBarList.get(index).setProgress(seekBarValue);
            tvList.get(index).setText(CHANNEL_NAMES[index] + ": " + finalValue);
        });
    }

    private void sendCrsfPacket() {
        int[] channels_us = new int[16];
        for (int i = 0; i < 16; i++) {
            channels_us[i] = i < NUM_CHANNELS ? channels.values[i] : 1000;
        }
        byte[] payload = packChannels(channels_us);
        byte[] packet = new byte[payload.length + 4];
        packet[0] = (byte) CRSF_ADDRESS_FLIGHT_CONTROLLER;
        packet[1] = (byte) (payload.length + 2);
        packet[2] = CRSF_FRAMETYPE_RC_CHANNELS_PACKED;
        System.arraycopy(payload, 0, packet, 3, payload.length);
        byte[] crcData = new byte[payload.length + 1];
        crcData[0] = (byte) CRSF_FRAMETYPE_RC_CHANNELS_PACKED;
        System.arraycopy(payload, 0, crcData, 1, payload.length);
        packet[packet.length - 1] = crc8.calc(crcData);

        try {
            if (outputStream != null) {
                outputStream.write(packet);
                outputStream.flush();
                Log.d("JoystickMonitor", "Sent CRSF packet: " + bytesToHex(packet));
            }
        } catch (IOException e) {
            Log.e("JoystickMonitor", "CRSF send error: " + e.getMessage());
        }
    }

    private byte[] packChannels(int[] channels_us) {
        long packed = 0;
        for (int i = 0; i < 16; i++) {
            int ch_us = channels_us[i];
            int packed_ch = (int) mapValue(ch_us, 1000, 2000, CRSF_CHANNEL_VALUE_1000, CRSF_CHANNEL_VALUE_2000);
            packed_ch = Math.max(0, Math.min(2047, packed_ch));
            packed |= ((long) (packed_ch & 0x7FF)) << (i * 11);
        }
        byte[] result = new byte[22];
        for (int i = 0; i < 22; i++) {
            result[i] = (byte) (packed >> (i * 8));
        }
        return result;
    }

    private double mapValue(double x, double inMin, double inMax, double outMin, double outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (looperThread != null) {
            looperThread.interrupt();
        }
        if (readerThread != null) {
            readerThread.interrupt();
        }
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            Log.e("JoystickMonitor", "Close error: " + e.getMessage());
        }
    }
}