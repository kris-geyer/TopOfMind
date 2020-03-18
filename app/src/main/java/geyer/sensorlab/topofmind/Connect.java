package geyer.sensorlab.topofmind;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import timber.log.Timber;

public class Connect extends Activity {

    SigError sigError;
    SharedPreferences sharedPreferences, cytonServicePreferences;
    TextView textView;


    private CytonService cytonService;
    private ServiceConnection cytonServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
        statelessInitialization();

        Intent intent = getIntent();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null){
            sigError.reportUpdate("Device == null");
            //displaySimpleMessage("Please reconnect the dongle", "No dongle found");
        }else{
            sigError.reportUpdate("Device != null");

            UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
            sharedPreferences.edit().putInt("stage2-state", 6).apply();
            startCytonService(usbManager, device);
        }
    }

    private void statelessInitialization() {
        initializeSharedPreferences();
        initializeUI();
        initializeBroadcastReceiver();
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences("errorLog", MODE_PRIVATE);
        cytonServicePreferences = getSharedPreferences("cyton", MODE_PRIVATE);
        sigError = new SigError(sharedPreferences);
    }

    private void initializeUI() {
        textView = findViewById(R.id.tvResult);

    }

    //BROADCAST RECEIVERS
    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null){
                sigError.reportUpdate("from connect - purpose called");
                String purpose = extras.getString("purpose");
                if (purpose != null) {
                    if(purpose.equals("service has been intialized")){
                        sigError.reportUpdate("Connect informed that the service has been initialized");
                        stopConnect();
                    }
                    if(purpose.equals("finish activity")){
                        sigError.reportUpdate("from connect - told to finish");
                        stopConnect();
                    }
                }
            }
        }
    };

    private void stopConnect() {
        finish();
    }

    private void initializeBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter("toConnect"));
    }

    private void startCytonService(UsbManager usbManager, UsbDevice usbDevice){
        sigError.reportUpdate("Capable of starting service");
        Intent startCytonService = new Intent(this, CytonService.class);
        final String serviceStatus = isMyServiceRunning();
        if (serviceStatus.equals("Could not be assessed")){
            sigError.reportUpdate("Could not assess if service was running");
            return;
        }
        if(serviceStatus.equals("not Running")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startCytonService);
            }else{
                startService(startCytonService);
            }
            cytonServiceConnection = manageBindToService(usbManager, usbDevice);
            bindService(startCytonService, cytonServiceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    private String isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (CytonService.class.getName().equals(service.service.getClassName())) {
                    return "Running";
                }
            }
        }else{
            return "Could not be assessed";
        }
        return "not Running";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);
        if(cytonServiceConnection != null){
            unbindService(cytonServiceConnection);
        }
    }

    private ServiceConnection manageBindToService(final UsbManager usbManager, final UsbDevice usbDevice) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                sigError.reportUpdate("On service connected called from connect");
                CytonService.LocalBinder binder = (CytonService.LocalBinder) iBinder;
                cytonService = binder.getService();
                cytonServicePreferences.edit().putInt("state", Constant.SERVICE_BOUND).apply();

                try {
                    cytonService.inheritUSB(usbDevice, usbManager);
                    sigError.reportUpdate("inheritUSB called");
                } catch (Exception e) {
                    sigError.reportError(e.getLocalizedMessage(),e.getStackTrace());
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Timber.i("On service disconnected called");
                sigError.reportUpdate("service disconnected");
            }
        };

    }


}
