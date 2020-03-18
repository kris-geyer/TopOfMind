package geyer.sensorlab.topofmind;

import java.io.FileOutputStream;
import java.io.IOException;

class InternalDataStorage {

   private final FileOutputStream fileOutputStream;


   InternalDataStorage(FileOutputStream fileOutputStream){
       this.fileOutputStream = fileOutputStream;
   }



    void addRaw(String valueOf) throws IOException {
       fileOutputStream.write(valueOf.getBytes());
    }
}
