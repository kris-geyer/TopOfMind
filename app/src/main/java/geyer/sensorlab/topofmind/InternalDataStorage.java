package geyer.sensorlab.topofmind;

import java.io.FileOutputStream;
import java.io.IOException;

class InternalDataStorage {

   private final FileOutputStream fileOutputStream;


   InternalDataStorage(FileOutputStream fileOutputStream){
       this.fileOutputStream = fileOutputStream;
   }


    void addEEGData(EEGDataRow row) throws IOException {
        String stringBuilder =
                row.row + " : " +
                row.chan1 + " : " +
                row.chan2 + " : " +
                row.chan3 + " : " +
                row.chan4 + " : " +
                row.chan5 + " : " +
                row.chan6 + " : " +
                row.chan7 + " : " +
                row.chan8 + " : " +
                row.timestamp + "\n";
        fileOutputStream.write(stringBuilder.getBytes());
    }


    void addRaw(String valueOf) throws IOException {
       fileOutputStream.write(valueOf.getBytes());
    }
}
