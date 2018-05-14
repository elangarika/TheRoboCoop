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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.support.design.widget.*;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "ROBOCOOP";
    private BluetoothDevice m_arduino_device ;
    private static UUID m_uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") ;
    private CoordinatorLayout coordinatorLayout = null ;
    private String m_arduino_device_name = "HC-05" ;
    private BluetoothSocket socket ;
    private Snackbar mySnackbar ;
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
                        m_arduino_device = null ;
                        BluetoothAdapter adapter =  BluetoothAdapter.getDefaultAdapter() ;
                        if (adapter.isEnabled()) {
                            String listdevices = "";
                            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                            for (BluetoothDevice device : pairedDevices) {
                                listdevices = listdevices + device.getName() + "/";
                                Log.i(TAG, device.getName());
                                if (device.getName().equalsIgnoreCase (m_arduino_device_name))
                                    m_arduino_device = device;
                            }
                            if (m_arduino_device == null) {
                                mySnackbar = Snackbar.make(coordinatorLayout,  R.string.error_text_arduino_not_found  + listdevices, Snackbar.LENGTH_INDEFINITE);
                                mySnackbar.show();
                                return ;
                            }
                            try {
                                socket = m_arduino_device.createRfcommSocketToServiceRecord(m_uuid);
                                socket.connect();
                                mySnackbar = Snackbar.make(coordinatorLayout, R.string.bluetooth_connection_success, Snackbar.LENGTH_INDEFINITE);
                                mySnackbar.show();

                                // connected. get sunset
                                String sunset = "19";

                            } catch (IOException e) {
                                 mySnackbar = Snackbar.make(coordinatorLayout, R.string.bluetooth_connection_failed, Snackbar.LENGTH_INDEFINITE);
                                mySnackbar.show();
                                e.printStackTrace();
                            }


                        }
                        else
                        {
                             mySnackbar = Snackbar.make(coordinatorLayout,
                                    R.string.bluetooth_not_enabled, Snackbar.LENGTH_LONG);
                            return ;
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
                                    Snackbar.make(coordinatorLayout, R.string.connect_to_bluetooth_first, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return;
                        }
                        try {

                            InputStream is = socket.getInputStream();
                            OutputStream os = socket.getOutputStream();
                            // get offset
                            String offset =  "offset " + ((EditText) findViewById(R.id.offset_id)).getText().toString() + "\n";
                            // get arduino time
                            String arduinotime =  "date "+((EditText) findViewById(R.id.arduino_time_id)).getText().toString() + "\n";

                            os.write(offset.getBytes());
                            // one of the results is missing, collect it
                            os.write(arduinotime.getBytes());
                            // Check if answer was OK
                            byte[] buffer = new byte[256];
                            int bytes = is.read(buffer);
                            String result = new String(buffer, 0, bytes);
                            socket.close();
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.changes_sent_to_robocoop +result,Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                        }
                        catch (Exception e)
                        {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_connecting_to_robocoop, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
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
                        try
                        {   if (m_arduino_device == null)
                        {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.connect_to_bluetooth_first, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return ;
                        }   else {

                            InputStream is = socket.getInputStream();
                            OutputStream os = socket.getOutputStream();
                            String open_command =  "door 1\n"  ;

                            os.write(open_command.getBytes());
                            // Check if answer was OK
                            byte[] buffer = new byte[256];
                            int bytes = is.read(buffer);
                            String result = new String(buffer, 0, bytes);
                            socket.close();
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.changes_sent_to_robocoop+result,Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                        }
                        }
                        catch (Exception e)
                        {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_connecting_to_robocoop, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                        }
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
                        try
                        {   if (m_arduino_device == null)
                        {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.connect_to_bluetooth_first, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                            return ;
                        }   else {


                            OutputStream os = socket.getOutputStream();
                            String open_command =  "door 0\n"  ;

                            os.write(open_command.getBytes());

                            // Check if answer was OK
                            InputStream is = socket.getInputStream();
                            byte[] buffer = new byte[256];
                            int bytes = is.read(buffer);
                            String result = new String(buffer, 0, bytes);
                            socket.close();

                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.changes_sent_to_robocoop + result,Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                        }
                        }
                        catch (Exception e)
                        {
                            Snackbar mySnackbar =
                                    Snackbar.make(coordinatorLayout, R.string.error_connecting_to_robocoop, Snackbar.LENGTH_INDEFINITE);
                            mySnackbar.show();
                        }
                        break;

                }
            }

        });


    }

}
