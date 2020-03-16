package geyer.sensorlab.topofmind;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

class DataCollection {


    //FINAL GLOBAL VARIABLES

    private final SigError sigError;
    private final HandlingBytes h;

    private ExternalDataStorage externalDataStorage;

    //GLOBAL VARIABLES

    private boolean ongoing;

    DataCollection(SigError sigError) throws IOException {
        externalDataStorage = new ExternalDataStorage("EEG.txt");

        h = new HandlingBytes();
        this.sigError = sigError;

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
                                    EEGDataRow eegDataRow = new EEGDataRow(
                                            String.valueOf(h.interpretHexAsInt32(h.byteToHex(toReview[1]))),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[2],toReview[3],toReview[4]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[5],toReview[6],toReview[7]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[8],toReview[9],toReview[10]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[11],toReview[12],toReview[13]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[14],toReview[15],toReview[16]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[17],toReview[18],toReview[19]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[20],toReview[21],toReview[22]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[23],toReview[24],toReview[25]})),
                                            String.valueOf(h.interpretHexAsInt32(h.byteToHex(toReview[29]) + h.byteToHex(toReview[30]) +h.byteToHex(toReview[31])))
                                    );
                                    eegDataRows.add(eegDataRow);
                                }else{
                                    break;
                                }
                            }else{
                                final byte[] toReview = Arrays.copyOfRange(byteArray, count, count + 33);
                                if(h.byteToHex(toReview[0]).equals("a0") && (h.byteToHex(toReview[32]).equals("c4") || h.byteToHex(toReview[32]).equals("c3"))) {
                                    EEGDataRow eegDataRow = new EEGDataRow(
                                            String.valueOf(h.interpretHexAsInt32(h.byteToHex(toReview[1]))),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[2],toReview[3],toReview[4]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[5],toReview[6],toReview[7]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[8],toReview[9],toReview[10]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[11],toReview[12],toReview[13]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[14],toReview[15],toReview[16]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[17],toReview[18],toReview[19]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[20],toReview[21],toReview[22]})),
                                            h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[23],toReview[24],toReview[25]})),
                                            String.valueOf(h.interpretHexAsInt32(h.byteToHex(toReview[29]) + h.byteToHex(toReview[30]) +h.byteToHex(toReview[31])))
                                    );
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
