package com.example.droneapp.common;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Build;
import android.util.Log;
import android.view.InputDevice;

public class InputDeviceScanner {
    private final Context context;

    public InputDeviceScanner(Context context) {
        this.context = context;
    }

    public String getAllInputDevicesInfo() {
        InputManager inputManager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
        StringBuilder info = new StringBuilder();

        int[] deviceIds = inputManager.getInputDeviceIds();
        for (int id : deviceIds) {
            InputDevice device = inputManager.getInputDevice(id);
            if (device != null) {
                info.append("Device ID: ").append(device.getId()).append("\n");
                Log.d("", String.valueOf(device.getId()));

                info.append("Name: ").append(device.getName()).append("\n");
                Log.d("", String.valueOf(device.getName()));

                info.append("Descriptor: ").append(device.getDescriptor()).append("\n");
                Log.d("", String.valueOf(device.getDescriptor()));

                info.append("Sources: ").append(getSourceString(device.getSources())).append("\n");
                Log.d("", String.valueOf(device.getSources()));

                info.append("Is Virtual: ").append(device.isVirtual()).append("\n");
                Log.d("", String.valueOf(device.isVirtual()));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    info.append("Is enabled: ").append(device.isEnabled()).append("\n");
                    Log.d("", String.valueOf(device.isEnabled()));
                }
                info.append("-------------------\n");
            }
        }
        return info.length() > 0 ? info.toString() : "No input devices found.";
    }

    private String getSourceString(int sources) {
        StringBuilder sourceStr = new StringBuilder();
        if ((sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
            sourceStr.append("Keyboard ");
        }
        if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            sourceStr.append("Gamepad ");
        }
        if ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
            sourceStr.append("Joystick ");
        }
        if ((sources & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
            sourceStr.append("Mouse ");
        }
        if ((sources & InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN) {
            sourceStr.append("Touchscreen ");
        }
        return sourceStr.length() > 0 ? sourceStr.toString() : "Unknown";
    }
}