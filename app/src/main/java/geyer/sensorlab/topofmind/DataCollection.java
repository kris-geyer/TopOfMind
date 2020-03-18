package geyer.sensorlab.topofmind;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

class DataCollection {


    //FINAL GLOBAL VARIABLES

    private final SigError sigError;
    private final HandlingBytes h;
    private final int channels;

    private ExternalDataStorage externalDataStorage;

    //GLOBAL VARIABLES

    private boolean ongoing;

    DataCollection(SigError sigError, int channels) throws IOException {
        externalDataStorage = new ExternalDataStorage("EEG.txt", channels);

        h = new HandlingBytes();
        this.sigError = sigError;
        this.channels = channels;
        ongoing = true;
    }

    void addTimeStamp () throws IOException {
        externalDataStorage.writeToFile("Data collection starting at: " + System.currentTimeMillis()+"\n");
    }


    void storeRawData(byte[] byteArray) {
        LinkedList<EEGDataRow> eegDataRows = new LinkedList<>();

        for (int i = 0; i < byteArray.length; i ++){
            if(h.byteToHex(byteArray[i]).equals("a0")){
                if(i+32<byteArray.length){
                    if(h.byteToHex(byteArray[i+32]).equals("c3")||h.byteToHex(byteArray[i+32]).equals("c4")){
                        int count = i;
                        while(count < byteArray.length){

                            if(i + 33 < byteArray.length){
                                final byte[] toReview = Arrays.copyOfRange(byteArray, count, count + 34);
                                if(h.byteToHex(toReview[33]).equals("a0") &&  h.byteToHex(toReview[0]).equals("a0") && (h.byteToHex(toReview[32]).equals("c4") || h.byteToHex(toReview[32]).equals("c3"))) {
                                    LinkedList<Double> signals = new LinkedList();
                                    for (int j = 0; j < channels; j++){
                                        signals.add(h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[(j*3)+2],toReview[(j*3)+3], toReview[(j*3)+4]})));
                                    }

                                    EEGDataRow eegDataRow = new EEGDataRow(String.valueOf(h.interpretHexAsInt32(h.byteToHex(toReview[1]))), signals, String.valueOf(h.interpretHexAsInt32(h.byteToHex(toReview[29]) + h.byteToHex(toReview[30]) +h.byteToHex(toReview[31]))));

                                    eegDataRows.add(eegDataRow);
                                }else{
                                    break;
                                }
                            }else{
                                final byte[] toReview = Arrays.copyOfRange(byteArray, count, count + 33);
                                if(h.byteToHex(toReview[0]).equals("a0") && (h.byteToHex(toReview[32]).equals("c4") || h.byteToHex(toReview[32]).equals("c3"))) {
                                    LinkedList<Double> signals = new LinkedList();
                                    for (int j = 0; j < channels; j++){
                                        signals.add(h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[(j*3)+2],toReview[(j*3)+3], toReview[(j*3)+4]})));
                                    }
                                    EEGDataRow eegDataRow = new EEGDataRow(String.valueOf(h.interpretHexAsInt32(h.byteToHex(toReview[1]))), signals, String.valueOf(h.interpretHexAsInt32(h.byteToHex(toReview[29]) + h.byteToHex(toReview[30]) +h.byteToHex(toReview[31]))));

                                    eegDataRows.add(eegDataRow);
                                }else{
                                    break;
                                }
                            }
                            count+=32;
                        }
                        i = count;
                    }
                }
            }
        }

        ongoing = true;

        if(eegDataRows.size()>0){
            storageInternally(eegDataRows);
        }
    }

    /**
     * ***********************DATA STORAGE**************************
     */

    private void storageInternally(LinkedList<EEGDataRow> eegDataRows){
        for(EEGDataRow row: eegDataRows){
            try {
                externalDataStorage.addEEGData(row);

            } catch (IOException e) {
                sigError.reportError(e.getLocalizedMessage(),e.getStackTrace());
            }
        }
    }


    boolean DataCollectionOnGoing() {
        boolean currentResult = ongoing;
        ongoing = false;
        if(!currentResult){
            try{
            externalDataStorage.closeFile();
            } catch (IOException e) {
                sigError.reportError(e.getMessage(),e.getStackTrace());
            }
        }
        return currentResult;
    }
}
