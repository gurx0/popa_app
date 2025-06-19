package com.example.droneapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

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

