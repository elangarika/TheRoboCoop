package com.example.eceegdl.robocoop;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.support.design.widget.*;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "ROBOCOOP";
    private BluetoothDevice m_arduino_device;
    private static UUID m_uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private CoordinatorLayout coordinatorLayout = null;
    private String m_arduino_device_name = "HC-05";
    private BluetoothSocket socket;
    private Snackbar mySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.myCoordinatorLayout);

        final Button button_connect_command = findViewById(R.id.switch_id);
        final Button button_save_command = findViewById(R.id.button_save_id);
        final Button button_open_command = findViewById(R.id.button_open_id);
        final Button button_close_command = findViewById(R.id.button_close_id);

        // CONNECT TO ARDUINO
        button_connect_command.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                switch (v.getId()) {

                    case R.id.switch_id:
                        // Connect to Arduino
                        // Identify the connection to the Arduino
                        m_arduino_device = null;
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        if (adapter.isEnabled()) {
                            String listdevices = "";
                            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                            for (BluetoothDevice device : pairedDevices) {
                                listdevices = listdevices + device.getName() + "/";
                                Log.i(TAG, device.getName());
                                if (device.getName().equalsIgnoreCase(m_arduino_device_name))
                                    m_arduino_device = device;
                            }
                            if (m_arduino_device == null) {
                                mySnackbar = Snackbar.make(coordinatorLayout, R.string.error_text_arduino_not_found + listdevices, Snackbar.LENGTH_INDEFINITE);
                                mySnackbar.show();
                                return;
                            }
                            try {
                                socket = m_arduino_device.createRfcommSocketToServiceRecord(m_uuid);
                                socket.connect();
                                mySnackbar = Snackbar.make(coordinatorLayout, R.string.response_bluetooth_connection_success, Snackbar.LENGTH_INDEFINITE);
                                mySnackbar.show();

                                // connected. get sunset
                                String sunset = "19";

                            } catch (IOException e) {
                                mySnackbar = Snackbar.make(coordinatorLayout, R.string.error_bluetooth_connection_failed, Snackbar.LENGTH_INDEFINITE);
                                mySnackbar.show();
                                e.printStackTrace();
                            }


                        } else {
                            mySnackbar = Snackbar.make(coordinatorLayout,
                                    R.string.error_bluetooth_not_enabled, Snackbar.LENGTH_LONG);
                            return;
                        }

                        break;

                }
            }
        });

        // SAVE VALUES TO ARDUINO
        button_save_command.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                switch (v.getId()) {

                    case R.id.button_save_id:
                        // Send command to arduino
                        if (m_arduino_device == null) {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_connect_to_bluetooth_first, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return;
                        }
                        String offset_command = "offset " + ((EditText) findViewById(R.id.offset_id)).getText().toString() + "\n";
                        String response_save_offset = sendCommandToArduino(offset_command);
                        String arduinotime_command = "date " + ((EditText) findViewById(R.id.arduino_time_id)).getText().toString() + "\n";
                        String response_save_arduino_time = sendCommandToArduino(arduinotime_command);
                        if (!response_save_offset.contains("OK") || (!response_save_arduino_time.contains("OK"))) {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_command, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return;
                        }

                        break;
                }

            }
        });


        // OPEN THE DOOR
        button_open_command.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                switch (v.getId()) {

                    case R.id.button_open_id:
                        // Send command to arduino
                        if (m_arduino_device == null) {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_connect_to_bluetooth_first, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return;
                        }

                        String open_command = "door 1\n";
                        String answer_open = sendCommandToArduino(open_command);
                        if (!answer_open.contains("OK")) {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_command, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return;
                        }
                        Snackbar mySnackbar =
                                Snackbar.make(coordinatorLayout, R.string.response_changes_sent_to_robocoop + answer_open, Snackbar.LENGTH_INDEFINITE);
                        mySnackbar.show();

                        break;

                }
            }

        });
        // CLOSE THE DOOR
        button_close_command.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                switch (v.getId()) {

                    case R.id.button_close_id:
                        // Send command to arduino
                        if (m_arduino_device == null) {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_connect_to_bluetooth_first, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return;
                        }

                        String close_command = "door 0\n";
                        String answer_close = sendCommandToArduino(close_command);
                        if (!answer_close.contains("OK")) {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_command, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return;
                        }
                        Snackbar mySnackbar =
                                Snackbar.make(coordinatorLayout, R.string.response_changes_sent_to_robocoop + answer_close, Snackbar.LENGTH_INDEFINITE);
                        mySnackbar.show();


                        break;

                }
            }

        });

    }

    private String sendCommandToArduino(String command) {

        String line2 = "ERROR";
        BufferedReader in;

        try {
            // SEND COMMAND
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            os.write(command.getBytes());

            // COLLECT ANSWER
            // Skip 1st line (echoed)
            // I think there is a problem here, if no answer it waits forever..... find alternatives to read lines
            in = new BufferedReader(new InputStreamReader(is));
            in.readLine();
            line2 = in.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
      return (line2) ;
    }



}


