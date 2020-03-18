package geyer.sensorlab.topofmind;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sharedPreferences;
    private SigError sigError;
    private TextView textView;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stateLessInitialization();
        Bundle extras = getIntent().getExtras();
        if(extras!= null){
            sigError.reportCrash(extras.getString("crashReport"), extras.getString("cause"));
        }
        actOnState(establishState());
    }

    private void stateLessInitialization() {
        sharedPreferences = getSharedPreferences("errorLog", MODE_PRIVATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, new IntentFilter("Main_Receiver"));
        initializeUI();
        initializeErrorLogging();
        initializeClasses();
    }

    private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null){
                String purpose = extras.getString("purpose");
                if (purpose != null) {
                    if(purpose.equals("inform change")){
                        String update = extras.getString("change");
                        textView.setText("Status: " + update);
                    }
                }
            }
        }
    };

    private void initializeUI() {
        findViewById(R.id.btnTest).setOnClickListener(this);
        findViewById(R.id.btnNextStep).setOnClickListener(this);
        btn =  findViewById(R.id.btnScanQR);
        btn.setOnClickListener(this);
        btn.setEnabled(establishState() > 2);

        textView = findViewById(R.id.textView);
    }

    private void initializeClasses() {
        sigError = new SigError(sharedPreferences);
    }

    private void initializeErrorLogging() {

        Timber.plant(new Timber.DebugTree(){
            @Override
            protected @org.jetbrains.annotations.Nullable String createStackElementTag(@NotNull StackTraceElement element) {
                return String.format("C:%s:%s",super.createStackElementTag(element), element.getLineNumber());
        }
                  });

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        sigError = new SigError(sharedPreferences);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnTest:

                sendEmail();
                break;
            case R.id.btnNextStep:
                //reportProgress();
                reportErrorLogs();
                break;
            case R.id.btnScanQR:
                scanQR();
                break;
        }
    }

    private void reportProgress() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Progress");
        builder.setMessage(establishProgress());
        builder.setPositiveButton("Next step", (dialogInterface, i) -> actOnState(establishState()));
        builder.create().show();
    }

    private StringBuilder establishProgress(){
        StringBuilder stringBuilder = new StringBuilder();
        int state =  establishState();
        stringBuilder.append("Agree to participate in a study - ");
        if (state > 2){
            stringBuilder.append("complete").append("\n");
        }else{
            stringBuilder.append("incomplete").append("\n");
        }

        stringBuilder.append("Provide permissions - ");
        if (state > 3){
            stringBuilder.append("complete").append("\n");
        }else{
            stringBuilder.append("incomplete").append("\n");
        }

        stringBuilder.append("Scan QR code - ");
        if (state > 4){
            stringBuilder.append("complete").append("\n");
        }else{
            stringBuilder.append("incomplete").append("\n");
            return stringBuilder;
        }

        stringBuilder.append("Insert Dongle - ");
        if (state > 6){
            stringBuilder.append("complete").append("\n");
        }else{
            stringBuilder.append("incomplete").append("\n");
        }

        return stringBuilder;
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


    /**
     *
     * @return
     * 1 - Errors have not been identified as correctly being detected
     * 2 - Need to inform the user about app
     * 3 - Camera permission is required
     * 4 - QR code needs to be scanned
     * 5 - Need to inform user about experiment
     * 6 - Request USB access
     * 7 - Need to attempt to connect to usb
     * 8 - Need to connect with the service
     * 9 - Ready to connect to serial device
     * 10 - Failure to connect with cyton
     * 11 - Connection made with cyton
     * 12 - No communication with cyton (for 2 seconds)
     */

    private int establishState() {
        if (sharedPreferences.getInt("stage1-state",0) == 0){
            return 1;
        }else{

            switch (sharedPreferences.getInt("stage2-state", 0)){
                case 0:
                    return 2;
                case 1:
                case 2:
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ){
                        return 3;
                    }else{
                        return 4;
                    }
                case 3:
                    return 5;
                case 4:
                    return 6;
                case 5:
                case 6:
                    return 7;
            }
        }
        try {
            throw new Exception("State not established");
        } catch (Exception e) {
            sigError.reportError(e.getLocalizedMessage(), e.getStackTrace());
        }
        return 0;
    }

    /**
     *
     * @param state
     * 1 - Fire test error
     * 2 - Inform the user about app
     * 3 - Request Camera permission
     * 4 - Direct to QR scan
     * 5 - Inform user about experiment
     * 6 - Request USB access
     * 7 - Need to attempt to connect to usb
     * 8 - Ready to connect to Cyton
     * 9 - Ready to perform impedance test
     */

    private void actOnState(int state) {
        sigError.reportUpdate("Main state: " + state);
        Timber.i("State detected as %d", state);
        switch (state){
            case 0:
                try {
                    throw new Exception("Could not identify action state");
                } catch (Exception e) {
                    sigError.reportError(e.getLocalizedMessage(), e.getStackTrace());
                }
            case 1:
                causeError();
                reportErrors();
                break;
            case 2:
                displaySimpleMessage("This app is intended for scientific study of neurological activity that involves using a openBCI cyton. Please indicate that you consent in participating in a scientific study.", 1, true, "I consent");
                break;
            case 3:
                getPermission();
                break;
            case 4:
                displaySimpleMessage("We require you to scan a QR code provided by the researcher in order to continue. Please, press scan QR code when you have the code in front of you.", 0, false, "OK");
                break;
            case 5:
                CalibrationInstructions calibrationInstructions = new CalibrationInstructions(getSharedPreferences("calibration instructions", MODE_PRIVATE));
                QRInstructions instructions = calibrationInstructions.readInstructions(this);
                displaySimpleMessage("This experiment will involve: " + "\n" + reportExperimentProcedure(instructions) + ". Please indicate that you consent in participating in a scientific study.", 4, true, "I consent");
                break;
            case 6:
                btn.setEnabled(false);
                requestConnectToTheUSB();
                break;
            case 7:

                break;
        }
    }

    private StringBuilder reportExperimentProcedure(QRInstructions instructions) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("- Having a routine data quality assessment every ").append(instructions.frequencyOfImpedance).append(" minutes").append("\n");

        int count = 0;

        if (!instructions.c0.equals("null")){
            count++;
        }
        if (!instructions.c1.equals("null")){
            count++;
        }
        if (!instructions.c2.equals("null")){
            count++;
        }
        if (!instructions.c3.equals("null")){
            count++;
        }
        if (!instructions.c4.equals("null")){
            count++;
        }
        if (!instructions.c5.equals("null")){
            count++;
        }
        if (!instructions.c6.equals("null")){
            count++;
        }
        if (!instructions.c7.equals("null")){
            count++;
        }

        stringBuilder.append("- There are ").append(count).append(" so many EEG channels connected").append("\n");

        if(instructions.notifications){
            stringBuilder.append("- You will be notified if a bad connection is detected, you prompted to fix the connection if a bad connection is shown").append("\n");
        }

        return stringBuilder;
    }

    void displaySimpleMessage(String message, int update, boolean establishState, String positiveResponse){
        AlertDialog.Builder alertDialogB = new AlertDialog.Builder(this);

        TextView myMsg = new TextView(this);
        myMsg.setText(message);
        myMsg.setTextSize(16);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        alertDialogB.setView(myMsg);

        alertDialogB.setCancelable(false)
                .setTitle("Experiment")
                .setPositiveButton(positiveResponse, (dialogInterface, i) -> {
                    if (update > 0){
                        sharedPreferences.edit().putInt("stage2-state", update).apply();
                    }
                    if (establishState) {
                        actOnState(establishState());
                    }
                })
                .create().show();
    }

    /**
     * Act on state - 1
     */

    private void causeError() {
        try{
            throw new Exception("a test error");
        }catch (Exception e){
            sigError.reportError(e.getLocalizedMessage(), e.getStackTrace());
        }
    }

    private void reportErrors() {
        Timber.i("reporting errors");
        for (int i = 0; i < sharedPreferences.getInt("errorNum", 0); i++){
            Timber.i(sharedPreferences.getString("error" + i, "not error"));
        }

        if (sharedPreferences.getString("error0", "not error").contains(" a test error") && sharedPreferences.getInt("stage1-setup",0) == 0){
            sharedPreferences.edit().putInt("stage1-state", 1).apply();
            Timber.i("Stage1-setup changed to stage 1");
        }else{
            try {
                throw new Exception("stage1-setup could not be complete");
            } catch (Exception e) {
                sigError.reportError(e.getLocalizedMessage(),e.getStackTrace());
                return;
            }
        }

        for (int i = 0; i < sharedPreferences.getInt("crashNum", 0); i++){
            Timber.i(sharedPreferences.getString("crash" + i, "not a crash report"));
        }

        Timber.i("reporting updates");
        for (int i = 0; i <sharedPreferences.getInt("updateNum",0); i++){
            Timber.i(sharedPreferences.getString("update"+i, "not update"));
        }

        actOnState(establishState());
    }

    /**
     * Act on state 3
     */

    private void getPermission() {
        Timber.i("Requesting camera permission");
        final boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        final boolean writeExternalPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermissionGranted && !writeExternalPermissionGranted){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constant.BOTH_PERMISSION_REQUEST);
        }else{
            if(!cameraPermissionGranted){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constant.CAMERA_PERMISSION_REQUEST);
            }else if(!writeExternalPermissionGranted) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constant.WRITE_EXTERNAL_PERMISSION_REQUEST);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.CAMERA_PERMISSION_REQUEST || requestCode == Constant.WRITE_EXTERNAL_PERMISSION_REQUEST || requestCode == Constant.BOTH_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sharedPreferences.edit().putInt("stage2-state", 2).apply();
                actOnState(establishState());
                btn.setEnabled(true);
            } else {
                getPermission();
            }
        }
    }

    /**
     * Act on state 4
     */

    private void scanQR() {
        Timber.i("starting to scan QR");
        startActivityForResult(new Intent(this, QRScanner.class), Constant.QR_ACTIVITY_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.QR_ACTIVITY_RESULT) {
            switch (resultCode) {
                case Constant.QR_CODE_APPROPRIATE:
                    String result;
                    if (data != null) {
                        result = data.getStringExtra("result");
                        Timber.i("QR code found to be somewhat appropriate, relaying : " + result + " for further testing");
                        assessQRData(result);
                    } else {
                        try {
                            throw new Exception("data from QR was found to be null");
                        } catch (Exception e) {
                            sigError.reportError(e.getLocalizedMessage(), e.getStackTrace());
                        }
                    }
                    break;
                case Constant.QR_CODE_INAPPROPRIATE:
                    try {
                        throw new Exception("QR reported was found to be inappropriate");
                    } catch (Exception e) {
                        sigError.reportError(e.getLocalizedMessage(), e.getStackTrace());
                    }
                    break;
            }
        }
    }

    private void assessQRData(String result) {
        SharedPreferences instructionsPreferences = getSharedPreferences("calibration instructions", MODE_PRIVATE);
        CalibrationInstructions calibrationInstructions = new CalibrationInstructions(instructionsPreferences);
        calibrationInstructions.writeInstructions(result);
        ReturnQRCode returnQRcode = new ReturnQRCode(instructionsPreferences);
        QRInstructions instructions = returnQRcode.returnQRInstructions();

        if (instructions != null){
            Timber.i("instructions: %s", instructions.storeAsTextFile);
            Timber.i("errors: %s", instructions.errorLogs);
            sharedPreferences.edit().putInt("stage2-state", 3).apply();
            actOnState(establishState());
            btn.setEnabled(false);
        }
    }

    /**
     * Act on state 6
     */

    private void requestConnectToTheUSB() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView myMsg = new TextView(this);
        myMsg.setText(R.string.insert_dongle);
        myMsg.setTextSize(16);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        builder
                .setTitle("Please connect the usb dongle")
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> { });
                builder.create().show();
    }


    /**
     * Other methods
     */


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");

        //getting directory for internal files
        String directory = (this.getFilesDir() + File.separator);

        storeErrors();

        //initializing files reference
        File
                eegFile = new File(directory + File.separator + Constant.EEG_FILE_NAME),

                impedanceFile = new File(directory + File.separator + Constant.IMPEDANCE_FILE_NAME),

                error = new File(directory + File.separator + Constant.ERROR_FILE_NAME);

        sigError.reportUpdate("File size: " + eegFile.length());

        //list of files to be uploaded
        ArrayList<Uri> files = new ArrayList<>();

        //if target files are identified to exist then they are packages into the attachments of the email
        try {
            if(eegFile.exists()){
                files.add(FileProvider.getUriForFile(this, "geyer.sensorlab.topofmind.fileprovider", eegFile));
            }

            if (impedanceFile.exists()){
                files.add(FileProvider.getUriForFile(this, "geyer.sensorlab.topofmind.fileprovider", impedanceFile));
            }

            if (error.exists()){
                files.add(FileProvider.getUriForFile(this, "geyer.sensorlab.topofmind.fileprovider", error));
            }

            if(files.size()>0){
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }else{
                Timber.e("no files to upload");
            }
        }
        catch (Exception e){
            Timber.e(e);
        }
    }

    private void storeErrors() {

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

        String sErrors = errors.toString();

        try {
            FileOutputStream fileOutputStream = openFileOutput(Constant.ERROR_FILE_NAME, MODE_APPEND);
            fileOutputStream.write(sErrors.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            sigError.reportError(e.getMessage(),e.getStackTrace());
        } catch (IOException e) {
            sigError.reportError(e.getMessage(),e.getStackTrace());
        }

    }


}
