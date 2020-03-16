package geyer.sensorlab.topofmind;

class EEGDataRow {

    final String row;
    final double chan1,chan2,chan3,chan4,chan5,chan6,chan7,chan8;
    final String timestamp;

    EEGDataRow(String row, double chan1, double chan2, double chan3, double chan4, double chan5, double chan6, double chan7, double chan8, String timestamp ){
         this.row = row;
         this.chan1 = chan1;
         this.chan2 = chan2;
         this.chan3 = chan3;
         this.chan4 = chan4;
         this.chan5 = chan5;
         this.chan6 = chan6;
         this.chan7 = chan7;
         this.chan8 = chan8;
         this.timestamp = timestamp;
     }





}
