//package com.example.droneapp;
//
//import android.content.Context;
//import android.view.InputDevice;
//import android.view.MotionEvent;
//import android.widget.TextView;
//
//public class JoystickReader {
//    private final Context context;
//    private final TextView textView;
//
//    public JoystickReader(Context context, TextView textView) {
//        this.context = context;
//        this.textView = textView;
//    }
//
//    public void handleJoystickInput(MotionEvent event) {
//        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
//                (event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
//            InputDevice device = InputDevice.getDevice(event.getDeviceId());
//            if (device == null) return;
//
//            StringBuilder info = new StringBuilder();
//            info.append("Device: ").append(device.getName()).append("\n");
//
//            // Аналоговые оси
//            info.append("Left Stick X: ").append(event.getAxisValue(MotionEvent.AXIS_X)).append("\n");
//            info.append("Left Stick Y: ").append(event.getAxisValue(MotionEvent.AXIS_Y)).append("\n");
//            info.append("Right Stick X: ").append(event.getAxisValue(MotionEvent.AXIS_Z)).append("\n");
//            info.append("Right Stick Y: ").append(event.getAxisValue(MotionEvent.AXIS_RZ)).append("\n");
//            info.append("Left Trigger: ").append(event.getAxisValue(MotionEvent.AXIS_LTRIGGER)).append("\n");
//            info.append("Right Trigger: ").append(event.getAxisValue(MotionEvent.AXIS_RTRIGGER)).append("\n");
//
//            // Кнопки (пример для основных кнопок)
//            info.append("Button A: ").append((event.getButtonState() & MotionEvent.BUTTON_A) != 0 ? "Pressed" : "Released").append("\n");
//            info.append("Button B: ").append((event.getButtonState() & MotionEvent.BUTTON_B) != 0 ? "Pressed" : "Released").append("\n");
//            info.append("Button X: ").append((event.getButtonState() & MotionEvent.BUTTON_X) != 0 ? "Pressed" : "Released").append("\n");
//            info.append("Button Y: ").append((event.getButtonState() & MotionEvent.BUTTON_Y) != 0 ? "Pressed" : "Released").append("\n");
//
//            textView.setText(info.toString());
//        }
//    }
//}