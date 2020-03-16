package geyer.sensorlab.topofmind;

import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

class QRInstructions {


    boolean storeAsTextFile, errorLogs, crashLogs, notifications,
            connectToCyton, dataReturnstoCyton, assessMemoryCapacity;

    int frequencyOfImpedance, valueOfImpedance;
    String c0, c1,c2,c3,c4,c5,c6,c7;

    void updateInstructions( HashMap<String, String> calibrationInstructions) {
        this.storeAsTextFile = Objects.equals(calibrationInstructions.get("TF"), "T");
        Timber.i("TF:%s", calibrationInstructions.get("TF"));
        this.errorLogs = Objects.equals(calibrationInstructions.get("ER"), "T");
        Timber.i("ER:%s", calibrationInstructions.get("ER"));
        this.crashLogs = Objects.equals(calibrationInstructions.get("CR"), "T");
        Timber.i("CR:%s", calibrationInstructions.get("CR"));
        this.notifications = Objects.equals(calibrationInstructions.get("NO"), "T");
        Timber.i("NO:%s", calibrationInstructions.get("NO"));
        this.connectToCyton = Objects.equals(calibrationInstructions.get("CY"), "T");
        Timber.i("CY:%s", calibrationInstructions.get("CY"));
        this.dataReturnstoCyton = Objects.equals(calibrationInstructions.get("DCY"), "T");
        Timber.i("DCY:%s", calibrationInstructions.get("DCY"));
        this.assessMemoryCapacity = Objects.equals(calibrationInstructions.get("AS"), "T");
        Timber.i("AS:%s", calibrationInstructions.get("AS"));

        this.frequencyOfImpedance = retrieveNumber(Objects.requireNonNull(calibrationInstructions.get("IM_F")));
        this.valueOfImpedance = retrieveNumber(Objects.requireNonNull(calibrationInstructions.get("IM_V")));

        if(calibrationInstructions.containsKey("c0")){
            this.c0 = calibrationInstructions.get("c0");
            Timber.i("c0:%s", this.c0);
        }else{
            this.c0 = "null";
        }
        if(calibrationInstructions.containsKey("c1")){
            this.c1 = calibrationInstructions.get("c1");
            Timber.i("c1:%s", this.c1);
        }else{
            this.c1 = "null";
        }
        if(calibrationInstructions.containsKey("c2")){
            this.c2 = calibrationInstructions.get("c2");
            Timber.i("c2:%s", this.c2);
        }else{
            this.c2 = "null";
        }
        if(calibrationInstructions.containsKey("c3")){
            this.c3 = calibrationInstructions.get("c3");
            Timber.i("c3:%s", this.c3);
        }else{
            this.c3 = "null";
        }
        if(calibrationInstructions.containsKey("c4")){
            this.c4 = calibrationInstructions.get("c4");
            Timber.i("c4:%s", this.c4);
        }else{
            this.c4 = "null";
        }
        if(calibrationInstructions.containsKey("c5")){
            this.c5 = calibrationInstructions.get("c5");
            Timber.i("c5:%s", this.c5);
        }else{
            this.c5 = "null";
        }
        if(calibrationInstructions.containsKey("c6")){
            this.c6 = calibrationInstructions.get("c6");
            Timber.i("c6:%s", this.c6);
        }else{
            this.c6 = "null";
        }
        if(calibrationInstructions.containsKey("c7")){
            this.c7 = calibrationInstructions.get("c7");
            Timber.i("c7:%s", this.c7);
        }else{
            this.c7 = "null";
        }


        Timber.i("IM_F:%s", calibrationInstructions.get("IM_F"));
        Timber.i("IM_V:%s", calibrationInstructions.get("IM_V"));

    }


    private int retrieveNumber (String value){
        int toReturn = 0;
        for (char c: value.toCharArray()){
            if(Character.isDigit(c)){
                if (toReturn == 0){
                    toReturn+= Integer.parseInt(String.valueOf(c));
                }else{
                    toReturn *= 10;
                    toReturn+= Integer.parseInt(String.valueOf(c));
                }
            }
        }
        return toReturn;
    }
}
