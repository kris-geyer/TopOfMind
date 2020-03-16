package geyer.sensorlab.topofmind;

import android.content.SharedPreferences;

import com.google.gson.Gson;

class ReturnQRCode {


    private SharedPreferences sharedPreferences;

    ReturnQRCode(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }

    QRInstructions returnQRInstructions (){
        String json = sharedPreferences.getString("json instructions", "not found");

        Gson gson = new Gson();

        return gson.fromJson(json, QRInstructions.class);
    }
}
