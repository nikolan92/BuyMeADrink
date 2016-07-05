package com.project.mosis.buymeadrink;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.project.mosis.buymeadrink.Application.MyApplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.UUID;

public class AddFriendActivity extends AppCompatActivity {

    private final static UUID uuid = UUID.fromString("ab3e7f9c-1b2f-11e5-b60b-1697f925ec7b");

    //Threads
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;
    private BluetoothAdapter bluetoothAdapter;
    //Layout
    private ListView listViewDevices;
    private ArrayAdapter<String> arrayAdapterDevices;
    private ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;
    ToggleButton toggle;
    //Intent request codes
    private static final int DISCOVERABLE_BT_REQUEST_CODE = 2;
    private static final int DISCOVERABLE_DURATION = 300;
    //Handler message code
    private static final int MESSAGE_READ = 0;
    private static final int SUCCESSFULLY_CONNECTED = 1;
    //Log Tag and request tag
    private final String LOG_TAG = "AddFriendActivity";
    private final String REQUEST_TAG = "AddFriendActivity";
    //UserHandler
    UserHandler userHandler;
    User user;
    String userID;
    String friendID = null;

    MyHandler mHandler;
    //BroadCastReceiver this receiver will be called when new bluetooth device was founded.
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayAdapterDevices.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_friend_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.add_friend_coordinator_layout);

        toggle = (ToggleButton) findViewById(R.id.add_friend_toggleButton);
        Bundle bundle = getIntent().getExtras();
        if(bundle==null) {
            toggle.setEnabled(false);
            return;
        }
        userID = bundle.getString("userID");
        userHandler = new UserHandler(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mHandler = new MyHandler(this);

        assert toggle != null;
        toggle.setChecked(false);

        if(bluetoothAdapter == null) {
            //device does not support bluetooth
            Toast.makeText(this, "Oops! Your device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            toggle.setEnabled(false);
        }else {
            toggle.setChecked(bluetoothAdapter.isEnabled());
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        //make discoverable will ask user to turn on bluetooth and after that will make device discoverable for 300s onActivityResult
                        makeDiscoverable();
                    } else {
                        // The toggle is disabled
                        //turn off bt
                        bluetoothAdapter.disable();
                        arrayAdapterDevices.clear();
                    }
                }
            });
        }
        setupListView();
    }
    private void setupListView(){
        listViewDevices = (ListView)findViewById(R.id.add_friend_list_view_unpaired_devices);
        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String itemValue = (String) listViewDevices.getItemAtPosition(position);

                String MAC = itemValue.substring(itemValue.length() - 17);
                Log.i(LOG_TAG,"ListView unpaired device item clicked MAC:"+MAC);
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);

                ConnectThread t = new ConnectThread(bluetoothDevice);
                t.start();
            }
        });
        arrayAdapterDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listViewDevices.setAdapter(arrayAdapterDevices);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DISCOVERABLE_BT_REQUEST_CODE) {
            if (resultCode == DISCOVERABLE_DURATION) {
                //when bluetooth is enabled then try to discover other devices
                //start to listening for connections
                acceptThread = new AcceptThread();
                acceptThread.start();
                discoverDevices();
//                Toast.makeText(getApplicationContext(), "Your device is now discoverable by other devices for " + DISCOVERABLE_DURATION + " seconds.", Toast.LENGTH_SHORT).show();
            } else {
                //user refuse to enable bluetooth device
                toggle.setChecked(false);
                Snackbar.make(coordinatorLayout,"Fail to enable discoverability on your device.",Snackbar.LENGTH_LONG).show();
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
        Log.i(LOG_TAG,"Register the BroadcastReceiver for ACTION_FOUND");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG,"Unregister the BroadcastReceiver for ACTION_FOUND");
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UserHandler.CancelAllRequestWithTagStatic(this,REQUEST_TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stop for connection listening if user leave activity
        if(acceptThread!=null)
            acceptThread.cancel();
    }
    /**
     * Work with received data from other device and sending data to the server.
     * */
    private void addFriend(String friendID){
        //close socket
        connectedThread.cancel();
        //disable bluetooth
        bluetoothAdapter.disable();
        //uncheck toogle button
        toggle.setChecked(false);

        user = ((MyApplication) AddFriendActivity.this.getApplication()).getUser();

        boolean alreadyFriends = false;
        if(user!=null) {
            for (int i = 0; i < user.getFriends().size(); i++) {
                if (user.getFriends().get(i).equals(friendID)) {
                    alreadyFriends = true;
                    break;
                }
            }
            if (alreadyFriends) {
                Snackbar.make(coordinatorLayout,"You are already friend with this user!",Snackbar.LENGTH_LONG).show();
            } else {
                user.addFriend(friendID);
                UserHandler userHandler = new UserHandler(this);
                userHandler.updateUserInfo(user,REQUEST_TAG,new UpdateUserListener(this));
                progressDialog = ProgressDialog.show(this,"Please wait","Sending data to the server...",false,false);
            }
        }
    }
    private void onUserUpdate(JSONObject result) {
        progressDialog.dismiss();
        try {
            if(result.getBoolean("Success")){
                SaveSharedPreference.SetUser(this,user);
                ((MyApplication) AddFriendActivity.this.getApplication()).setUser(user);
                Intent intentData = new Intent();
                intentData.putExtra("friendID",friendID);
                setResult(RESULT_OK,intentData);
                finish();
            }else{
                Snackbar.make(coordinatorLayout,"Something goes wrong, try again later.",Snackbar.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG,e.toString());
        }
    }
    /**
     * Bluetooth classes
     * */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread() {
            BluetoothServerSocket temp = null;
            try {
                //BluetoothServerSocket listening for connection after call accept, only connection with specific uuid will be accepted
                temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothServerSocket = temp;
        }

        public void run() {
            BluetoothSocket bluetoothSocket;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    //Start listening for connection requests by calling accept() after one of devices sent request for
                    //connection this will be unblocked and accept will return a connected BluetoothSocket, if everything
                    //goes fine, otherwise it will throw exception.
                    //BluetoothServerSocket should be closed after accepting connection, that will release all its resource
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
                    manageBluetoothConnection(bluetoothSocket);

                    //connectedThread = new ConnectedThread(bluetoothSocket);
                    //connectedThread.run();
                    try {
                        bluetoothServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        /**
         * Here we have bluetoothSocket an now we can read and write data through the socket
         * */
        private void manageBluetoothConnection(BluetoothSocket bluetoothSocket){
            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
            mHandler.obtainMessage(SUCCESSFULLY_CONNECTED).sendToTarget();

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
            //Note: You should always ensure that the device is not performing device discovery when you call connect().
            //If discovery is in progress, then the connection attempt will be significantly slowed and is more likely to fail.
            bluetoothAdapter.cancelDiscovery();

            try {
                //Upon this call, the system will perform an SDP lookup on the remote device in order to match the UUID.
                //If the lookup is successful and the remote device accepts the connection, it will share the RFCOMM channel
                //to use during the connection and connect() will return.
                //If, for any reason, the connection fails or the connect() method times out (after about 12 seconds), then it will throw an exception.
                //This method is a blocking call.
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
               manageBluetoothConnection(bluetoothSocket);
        }
        /**
         * Here we have bluetoothSocket an now we can read and write data through the socket
         * */
        private void manageBluetoothConnection(BluetoothSocket bluetoothSocket){
            //TODO:send messge to handler to update UI and enable button
            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
            mHandler.obtainMessage(SUCCESSFULLY_CONNECTED).sendToTarget();
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * <p>This class using user who act like a server or client there is no difference,both server or client side get instance of this class
     * (when bluetooth socket is ready) and exchange data with this class in separate thread.</p>
     * */
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
                    Log.i("ConnectedThread","Data received, data:" + new String(buffer,0,bytes,"UTF-8"));
                    //Send message to handler to processed data.
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Log.i("ConnectedThread","Data write:"+ new String(bytes,"UTF-8"));
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    /**
     * MyHandler class, using for update UI when message from bluetooth is received.
     * */
    private static class MyHandler extends Handler {
        private final WeakReference<AddFriendActivity> mActivity;
        //Handler message code
        public MyHandler(AddFriendActivity addFriendActivity){
            this.mActivity = new WeakReference<>(addFriendActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            AddFriendActivity addFriendActivity = this.mActivity.get();
            if(addFriendActivity!=null){
                switch (msg.what){
                    case AddFriendActivity.SUCCESSFULLY_CONNECTED:
                        //send message to other device
                        String userID = addFriendActivity.userID;
                        //send data to other device
                        addFriendActivity.connectedThread.write(userID.getBytes(Charset.forName("UTF-8")));
                        //Toast.makeText(addFriendActivity,"Sending user information to other device.\nWait...",Toast.LENGTH_SHORT).show();
                        break;
                    case AddFriendActivity.MESSAGE_READ:
                        int bytes = msg.arg1;
                        byte[] buffer = (byte[]) msg.obj;
                        String receivedString = null;
                        try {
                            receivedString = new String(buffer, 0, bytes, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            Log.e(addFriendActivity.LOG_TAG, e.toString());
                        }
                        if (receivedString != null) {
                            Log.i(addFriendActivity.LOG_TAG,"Data received, DATA:"+receivedString);

                            Toast.makeText(addFriendActivity, "Data from other device received.\n FriendID:" + receivedString, Toast.LENGTH_LONG).show();
                            addFriendActivity.addFriend(receivedString);
                        }
                        break;
                }
            }
        }
    }
    /**
     * UpdateUserListener - helper class for handling UserHandler callbacks (VolleyCallBack)
     * */
    private static class UpdateUserListener implements VolleyCallBack{
        private final WeakReference<AddFriendActivity> mActivity;
        UpdateUserListener(AddFriendActivity addFriendActivity){
            this.mActivity = new WeakReference<>(addFriendActivity);
        }

        @Override
        public void onSuccess(JSONObject result) {
            AddFriendActivity addFriendActivity = this.mActivity.get();
            if(addFriendActivity!=null){
                addFriendActivity.onUserUpdate(result);
            }
        }

        @Override
        public void onFailed(String error) {
            AddFriendActivity addFriendActivity = this.mActivity.get();
            if(addFriendActivity!=null){
                addFriendActivity.progressDialog.dismiss();
                Snackbar.make(addFriendActivity.coordinatorLayout,"Something goes wrong, try again later.",Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
