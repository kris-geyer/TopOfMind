package geyer.sensorlab.topofmind;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

class CalibrationInstructions  {

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    CalibrationInstructions(SharedPreferences sharedPreferences) {

        this.gson = new Gson();
        this.sharedPreferences = sharedPreferences;
    }

     void writeInstructions(String QRInput) {
        InterpretQR interpretQR = new InterpretQR(QRInput);

        String json = gson.toJson(interpretQR.generateCalibrationInstructions());
        sharedPreferences.edit().putString("json instructions",json).apply();
    }

    QRInstructions readInstructions (Context context){
        SigError sigError = new SigError(context.getSharedPreferences("errorLog", MODE_PRIVATE));
        String json =sharedPreferences.getString("json instructions", "not found");

        if (json.equals("not found")){
            sigError.reportUpdate("JSON instructions not found");
            return new QRInstructions();
        }else{
            sigError.reportUpdate("JSON instructions found");
            return gson.fromJson(json, QRInstructions.class);
        }
    }

}
