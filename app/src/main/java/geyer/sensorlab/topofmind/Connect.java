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
import android.view.View;
import android.widget.TextView;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import timber.log.Timber;

public class Connect extends Activity implements View.OnClickListener {

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
        findViewById(R.id.btnReportError).setOnClickListener(this);
    }

    //BROADCAST RECEIVERS
    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null){
                String purpose = extras.getString("purpose");
                if (purpose != null) {
                    if(purpose.equals("service has been intialized")){
                        sigError.reportUpdate("Connect informed that the service has been initialized");
                        stopConnect();
                    }
                    if(purpose.equals("finish activity")){
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


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnReportError:
                reportErrorLogs();
                break;
        }
    }

    private void reportErrorLogs() {
        StringBuilder errors = new StringBuilder();
        errors.append("Errors").append("\n").append("\n");
        for (int i = 0; i < sharedPreferences.getInt("errorNum", 0); i++){
            errors.append(sharedPreferences.getString("error" + i, "not error")).append("\n");
        }

        errors.append("Crashes").append("\n").append("\n");
        for (int i = 0; i < sharedPreferences.getInt("crashNum", 0); i++){
            errors.append(sharedPreferences.getString("crash" + i, "not a crash report")).append("\n");
        }

        errors.append("Updates").append("\n").append("\n");
        for (int i = 0; i <sharedPreferences.getInt("updateNum",0); i++){
            errors.append(sharedPreferences.getString("update"+i, "not update")).append("\n");
        }

        Timber.i("Error logs %s", errors);
        textView.setText(errors);
    }

    private void startCytonService(UsbManager usbManager, UsbDevice usbDevice){
        sigError.reportUpdate("Capable of starting service");
        Intent startCytonService = new Intent(this, CytonService.class);
        final String serviceStatus = isMyServiceRunning();
        if (serviceStatus.equals("Could not be assessed")){
            sigError.reportUpdate("Could not assess if service was running");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startCytonService);
        }else{
            startService(startCytonService);
        }

        cytonServiceConnection = manageBindToService(usbManager, usbDevice);
        bindService(startCytonService, cytonServiceConnection, Context.BIND_AUTO_CREATE);

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
        unbindService(cytonServiceConnection);
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


    /*DEPOSITING ALL POSSIBLY NECESSARY CODE

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null){
            sigError.reportUpdate("Device == null");
        }else{
            sigError.reportUpdate("Device != null");
            if(alertDialog != null){
                alertDialog.dismiss();
            }
            UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
            sharedPreferences.edit().putInt("stage2-state", 6).apply();
            startCytonService(usbManager, device);
        }

    }


    private void startCytonService(UsbManager usbManager, UsbDevice usbDevice){
        sigError.reportUpdate("Capable of starting service");
        Intent startCytonService = new Intent(this, CytonService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startCytonService);
        }else{
            startService(startCytonService);
        }
        cytonServiceConnection = manageBindToService(usbManager, usbDevice);

        bindService(startCytonService, cytonServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection manageBindToService(final UsbManager usbManager, final UsbDevice usbDevice) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                sigError.reportUpdate("On service connected called");
                CytonService.LocalBinder binder = (CytonService.LocalBinder) iBinder;
                cytonService = binder.getService();
                cytonService.documentConnection();
                cytonServicePreferences.edit().putInt("state", Constant.SERVICE_BOUND).apply();
                cytonService.inheritUSB(usbDevice, usbManager);
                actOnState(establishState());
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Timber.i("On service disconnected called");
                sigError.reportUpdate("service disconnected");
            }
        };
    }

    private void reportConnectivity(ArrayList<String> connectivity) {
        if (connectivity.size() == 0){
            displaySimpleMessage("Good connectivity to electrodes.", "Connectivity", "OK");
        }else{
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("channel: ");
            for (String channel: connectivity){
                stringBuilder.append(channel).append(", ");
            }

            stringBuilder.setLength(stringBuilder.length()-2);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            TextView myMsg = new TextView(this);
            myMsg.setText("Poor connectivity to electrodes in the following areas:" + stringBuilder + ". Once all hair and other interfering obstacles removed from electrode please press reassess connectivity");
            myMsg.setTextSize(16);
            myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
            builder.setView(myMsg);

            builder
                    .setTitle("Connectivity")
                    .setCancelable(false)
                    .setPositiveButton("Reassess connectivity", (dialogInterface, i) -> {
                        cytonService.performImpedanceTest( 1);
                    })

                    .create().show();

        }
    }


 /*

//LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter("toConnect"));
    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null){
                if (Objects.equals(extras.getString("purpose"), "Assessing cyton")){
                    //actOnState(establishState());
                }
                if (Objects.equals(extras.getString("purpose"), "returnResultsOfImpedanceTest")){
                    ArrayList<String> connectivity = extras.getStringArrayList("eegResults");
                    if (connectivity != null) {
                        //reportConnectivity(connectivity);
                    }
                }
            }
        }
    };

    */



}
