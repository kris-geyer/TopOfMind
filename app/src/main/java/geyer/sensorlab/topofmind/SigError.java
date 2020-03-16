package geyer.sensorlab.topofmind;

import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import timber.log.Timber;

class SigError extends Exception {

    private final SharedPreferences sharedPreferences;


    SigError(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;

    }

    void reportError (String message, StackTraceElement[] stackTraceElement){
        Timber.i("documenting error");
        String time = DateFormat.getDateTimeInstance().format(new Date());
        sharedPreferences.edit()
                .putString("error" + sharedPreferences.getInt("errorNum", 0), time + " - " + String.format("C:%s:%s",(stackTraceElement[0].getClassName()), stackTraceElement[0].getLineNumber()) + " - " + message )
                .putInt("errorNum", (1 + sharedPreferences.getInt("errorNum", 0) ))
                .apply();
    }

    void reportCrash(String crashReport, String cause) {
        Timber.i("documenting crash");
        String time = DateFormat.getDateTimeInstance().format(new Date());
        sharedPreferences.edit()
                .putString("crash" + sharedPreferences.getInt("crashNum", 0), time+ " - cause: " + cause + "\n" + " crash report:" + crashReport)
                .putInt("crashNum", (1 + sharedPreferences.getInt("crashNum", 0) ))
                .apply();
    }

    void reportUpdate(String update){
        Timber.i("documenting update");
        String time = DateFormat.getDateTimeInstance().format(new Date());
        sharedPreferences.edit()
                .putString("update" + sharedPreferences.getInt("updateNum", 0),  time + " - update:" + update)
                .putInt("updateNum", (1 + sharedPreferences.getInt("updateNum", 0) ))
                .apply();
    }

     void reportError(String problem) {
         Timber.i("documenting error");
         String time = DateFormat.getDateTimeInstance().format(new Date());
         sharedPreferences.edit()
                 .putString("error" + sharedPreferences.getInt("errorNum", 0), time + " - " + problem)
                 .putInt("errorNum", (1 + sharedPreferences.getInt("errorNum", 0) ))
                 .apply();
    }
}
