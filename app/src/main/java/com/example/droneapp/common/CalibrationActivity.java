package com.example.droneapp.common;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.droneapp.R;

public class CalibrationActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Получение данных с акселерометра
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                // Здесь можно добавить логику для перекалибровки
                // Например, сохранить смещение для коррекции
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Обработка изменения точности
            }
        };
    }

    // Метод для "перекалибровки" (регистрация сенсора)
    private void recalibrateSensor() {
        if (accelerometer != null) {
            // Сброс и повторная регистрация слушателя
            sensorManager.unregisterListener(sensorListener);
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        recalibrateSensor(); // Вызов перекалибровки при возобновлении
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener); // Отключение сенсора
    }
}