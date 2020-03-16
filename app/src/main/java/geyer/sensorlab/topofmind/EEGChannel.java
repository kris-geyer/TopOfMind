package geyer.sensorlab.topofmind;

class EEGChannel {
    final int channelNum;
    final boolean EEG;
    final String location;

    EEGChannel(int channelNum, boolean EEG, String location){
        this.channelNum = channelNum;
        this.EEG = EEG;
        this.location = location;
    }
}
