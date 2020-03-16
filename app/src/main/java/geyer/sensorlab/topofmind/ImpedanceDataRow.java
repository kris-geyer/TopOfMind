package geyer.sensorlab.topofmind;

class ImpedanceDataRow {

    final String row, time;
    final double voltage;
    ImpedanceDataRow(String row, double voltage, String time){
        this.row = row;
        this.time = time;
        this.voltage = voltage;
    }

}
