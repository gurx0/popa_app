package com.example.droneapp;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoystickMonitorActivity extends AppCompatActivity {
    private static final byte[] PACKAGE_HEADER_BUF = "fengyingdianzi:".getBytes(); // Заголовок для H12
    private static final int NUM_CHANNELS = 12; // 12 каналов для H12
    private static final String SERIAL_PORT = "/dev/ttyHS1"; // Порт для H12
    private static final int BAUD_RATE = 921600; // Скорость порта

    private List<SeekBar> mSeekBarList = new ArrayList<>();
    private List<TextView> mTVList = new ArrayList<>();
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private Thread looperThread;
    private Thread readerThread;
    private volatile boolean isRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick);

        // Инициализация UI
        initUI();

        // Инициализация соединения
        initSerialPort();
    }

    private void initUI() {
        LinearLayout container = findViewById(R.id.seekbar_container);
        for (int i = 0; i < NUM_CHANNELS; i++) {
            TextView textView = new TextView(this);
            textView.setText("CH" + (i + 1));
            textView.setTextSize(16);
            container.addView(textView);
            mTVList.add(textView);

            SeekBar seekBar = new SeekBar(this);
            seekBar.setMax(2000); // Диапазон для H12
            seekBar.setEnabled(false); // Только для отображения
            container.addView(seekBar);
            mSeekBarList.add(seekBar);
        }
    }

    private void initSerialPort() {
        try {
            inputStream = new FileInputStream(SERIAL_PORT);
            outputStream = new FileOutputStream(SERIAL_PORT);
            startThreads();
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                mTVList.get(0).setText("Ошибка: " + e.getMessage());
            });
        }
    }

    private void startThreads() {
        looperThread = new Thread(() -> {
            byte[] bArr = new byte[PACKAGE_HEADER_BUF.length + 5];
            System.arraycopy(PACKAGE_HEADER_BUF, 0, bArr, 0, PACKAGE_HEADER_BUF.length);
            bArr[PACKAGE_HEADER_BUF.length] = (byte) 0xB1; // Команда запроса
            bArr[PACKAGE_HEADER_BUF.length + 1] = 2; // Длина данных
            bArr[PACKAGE_HEADER_BUF.length + 2] = 'R'; // Команда 'R'
            bArr[PACKAGE_HEADER_BUF.length + 3] = 1; // Параметр
            bArr[PACKAGE_HEADER_BUF.length + 4] = calculateBCC(bArr); // Контрольная сумма

            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    if (outputStream != null) {
                        outputStream.write(bArr);
                        outputStream.flush();
                    }
                    Thread.sleep(100); // Частота опроса 10 Гц
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
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
                            byte[] data = new byte[len];
                            System.arraycopy(buffer, 0, data, 0, len);
                            received(data);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
        if (datas.length < PACKAGE_HEADER_BUF.length + 2 || datas[PACKAGE_HEADER_BUF.length] != (byte) 0xB1) {
            return;
        }

        byte length = datas[PACKAGE_HEADER_BUF.length + 1];
        int startIndex = PACKAGE_HEADER_BUF.length + 2;

        runOnUiThread(() -> {
            for (int i = 0; i < length && i < NUM_CHANNELS * 2; i += 2) {
                setSeekBar(i / 2, datas[startIndex + i], datas[startIndex + i + 1]);
            }
        });
    }

    private int setSeekBar(int index, byte h, byte l) {
        int value = Utils.bytes2int(new byte[]{0, 0, h, l});
        value = Math.max(1000, Math.min(value, 2000)); // Ограничение диапазона 1000–2000
        int seekBarValue = value - 500; // Смещение для SeekBar (0–1000)
        mSeekBarList.get(index).setProgress(seekBarValue);
        mTVList.get(index).setText("CH" + (index + 1) + ": " + value);
        return value;
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
            e.printStackTrace();
        }
    }
}