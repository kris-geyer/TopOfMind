package geyer.sensorlab.topofmind;

import java.util.LinkedList;

class EEGDataRow {

    final String row;
    final LinkedList<Double> signal;
    final String timestamp;

    EEGDataRow(String row, LinkedList<Double> signal, String timestamp ){
         this.row = row;
         this.signal = signal;
         this.timestamp = timestamp;
     }





}
