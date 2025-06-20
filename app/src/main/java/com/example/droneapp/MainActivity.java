package com.example.droneapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.droneapp.crsf.JoystickMonitorActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, JoystickMonitorActivity.class);
        startActivity(intent);

        //        TextView textView = findViewById(R.id.text_view_devices);
//        if (textView != null) {
//            InputDeviceScanner scanner = new InputDeviceScanner(this);
//            textView.setText(scanner.getAllInputDevicesInfo());
//        } else {
//            Log.e("MainActivity", "TextView not found");
//        }
    }
}

