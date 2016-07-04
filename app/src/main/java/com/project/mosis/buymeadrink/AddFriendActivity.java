package com.project.mosis.buymeadrink;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.project.mosis.buymeadrink.DataLayer.UserHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class AddFriendActivity extends AppCompatActivity {

    private final static UUID uuid = UUID.fromString("ab3e7f9c-1b2f-11e5-b60b-1697f925ec7b");

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            adapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
        }
        }
    };

    private ConnectedThread connectedThread;

    private BluetoothAdapter bluetoothAdapter;
    //private ToggleButton toggleButton;
    private ListView listView;
    private ArrayAdapter adapter;
    private static final int ENABLE_BT_REQUEST_CODE = 1;
    private static final int DISCOVERABLE_BT_REQUEST_CODE = 2;
    private static final int DISCOVERABLE_DURATION = 300;

    private final String MESSAGE_READ = "READ_ID";

    //UserHandler
    UserHandler userHandler;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);


        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            userID = bundle.getString("userID");
            userHandler = new UserHandler(this);


            ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
            assert toggle != null;
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter == null) {
                //device does not support BT
                Toast.makeText(getApplicationContext(), "Oops! Your device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                toggle.setChecked(false);
            } else {
                toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            // The toggle is enabled
                            //to turn on bt
//                            if (!bluetoothAdapter.isEnabled()) {
//                                //permission dialog show
//                                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                                startActivityForResult(enableBluetoothIntent, ENABLE_BT_REQUEST_CODE);
//                            } else {
                                Toast.makeText(getApplicationContext(), "Your device has already been enabled." + "\n" + "Scanning for remote bluetooth devices...", Toast.LENGTH_SHORT).show();
                                //discover remote bt devices
                                discoverDevices();
                                // make local device discoverable by other devices
                                makeDiscoverable();
                            //}
                        } else {
                            // The toggle is disabled
                            //turn off bt
                            bluetoothAdapter.disable();
                            adapter.clear();
                            Toast.makeText(getApplicationContext(), "Your device is now disabled.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            listView = (ListView) findViewById(R.id.listView);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String itemValue = (String) listView.getItemAtPosition(position);

                    String MAC = itemValue.substring(itemValue.length() - 17);

                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);

                    ConnectThread t = new ConnectThread(bluetoothDevice);
                    t.start();
                }
            });

            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
            listView.setAdapter(adapter);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

//        if (requestCode == ENABLE_BT_REQUEST_CODE) {
//            //bt successfully enabled
//            if (resultCode == Activity.RESULT_OK) {
//                Toast.makeText(getApplicationContext(), "Ha! Bluetooth is now enabled." + "\n" + "Scanning for remote Bluetooth devices...", Toast.LENGTH_SHORT).show();
//
//                makeDiscoverable();
//
//                discoverDevices();
//
//                AcceptThread t = new AcceptThread();
//                t.start();
//
//            } else { //result canceled user refused it
//
//                Toast.makeText(getApplicationContext(), "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
//
////                toggleButton.setChecked(false);
//            }
//        } else
            if (requestCode == DISCOVERABLE_BT_REQUEST_CODE) {

                if (resultCode == DISCOVERABLE_DURATION) {
                    Toast.makeText(getApplicationContext(), "Your device is now discoverable by other devices for " + DISCOVERABLE_DURATION + " seconds", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "fail to enable discoverability on your device.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    protected void discoverDevices() {
        //to scan remote bt devices
        if (bluetoothAdapter.startDiscovery()) {
            Toast.makeText(getApplicationContext(), "Discovering other bluetooth devices...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Discovery failed to start.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void makeDiscoverable() {
        // Make local device discoverable
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, DISCOVERABLE_BT_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the BroadcastReceiver for ACTION_FOUND
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread() {
            BluetoothServerSocket temp = null;
            try {
                temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothServerSocket = temp;
        }

        public void run() {
            BluetoothSocket bluetoothSocket;

            while (true) {
                try {
                    bluetoothSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                if (bluetoothSocket != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "A connection has been accepted.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Code to manage the connection in a separate thread
                   /*
                       manageBluetoothConnection(bluetoothSocket);
                   */
                    connectedThread = new ConnectedThread(bluetoothSocket);
                    connectedThread.run();

                    try {
                        bluetoothServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel() {

            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket temp = null;
            bluetoothDevice = device;

            try {
                temp = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothSocket = temp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                bluetoothSocket.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
            // Code to manage the connection in a separate thread
            /*
               manageBluetoothConnection(bluetoothSocket);
            */
            connectedThread = new ConnectedThread(bluetoothSocket);
            //connectedThread.run();

            connectedThread.write(new String("TEST TEST").getBytes(Charset.forName("UTF-8")));
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                    Log.i("BLUTHOTOOF::::::::::",new String(buffer,"UTF-8"));
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
