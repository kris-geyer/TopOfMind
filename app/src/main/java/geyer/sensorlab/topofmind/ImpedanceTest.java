package geyer.sensorlab.topofmind;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

class ImpedanceTest {

    private final HandlingBytes h;
    private final SigError sigError;
    private int channelOfFocus;
    private LinkedList<Double> impedanceScores;
    private ArrayList<Double> meanImpedanceScores;
    private boolean isPositive;
    private final Context context;

    ImpedanceTest(SigError sigError, Context context) {
        this.h = new HandlingBytes();
        this.sigError = sigError;
        this.context = context;
    }

    private void initializeAllImpedanceScore (){
        meanImpedanceScores = new ArrayList<>();
        isPositive = true;
    }

    private void initializeImpedanceScore (){
        impedanceScores = new LinkedList<>();
    }

    void determineChannel (int channel){
        sigError.reportUpdate("Channel determined: " + channel);
        channelOfFocus = channel;
        initializeAllImpedanceScore();
        initializeImpedanceScore();
    }

    void supplyData(byte[] byteArray) {
        LinkedList<Double> data = new LinkedList<>();

        int c = ((channelOfFocus-1)*3);

        for (int i = 0; i < byteArray.length; i ++){
            if(h.byteToHex(byteArray[i]).equals("a0")){

                if(i+32<byteArray.length){
                    if(h.byteToHex(byteArray[i+32]).equals("c3")||h.byteToHex(byteArray[i+32]).equals("c4")){
                        int count = i;
                        while(count < byteArray.length){

                            if(i + 33 < byteArray.length){
                                final byte[] toReview = Arrays.copyOfRange(byteArray, count, count + 34);
                                if(h.byteToHex(toReview[33]).equals("a0") &&  h.byteToHex(toReview[0]).equals("a0") && (h.byteToHex(toReview[32]).equals("c4") || h.byteToHex(toReview[32]).equals("c3"))) {
                                    data.add(h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[c+2],toReview[c+3],toReview[c+4]})));
                                }else{
                                    break;
                                }
                            }else{
                                final byte[] toReview = Arrays.copyOfRange(byteArray, count, count + 33);
                                if(h.byteToHex(toReview[0]).equals("a0") && (h.byteToHex(toReview[32]).equals("c4") || h.byteToHex(toReview[32]).equals("c3"))) {
                                    data.add(h.changeToMicroVolts(h.myInterpret24bitAsInt32(new byte[]{toReview[c+2],toReview[c+3],toReview[c+4]})));
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
        segmentData(data);
    }

    private void segmentData(LinkedList<Double> data) {
        for (Double d: data){
            if (d > 0 && !isPositive){
                meanImpedanceScores.add(generateMean(impedanceScores) );
                initializeImpedanceScore();
                isPositive = true;
            }
            if (d < 0 && isPositive){
                meanImpedanceScores.add(generateMean(impedanceScores));
                initializeImpedanceScore();
                isPositive = false;
            }
            impedanceScores.add(d);
        }
        assessIfDataSizeIsSufficient();
    }

    private Double generateMean(LinkedList<Double> impedanceScores) {
        double holding = 0.0;
        int num = 0;
        for (Double score: impedanceScores){
            holding += (score);
            if (score != 0.0){
                num++;
            }
        }
        if (num > 0){
            return (holding/num);
        }else{
            return 0.0;
        }
    }

    private void assessIfDataSizeIsSufficient() {
        if (impedanceScores.size() > 0){
            meanImpedanceScores.add(generateMean(impedanceScores));
            initializeImpedanceScore();
        }

        while (meanImpedanceScores.remove(0.0)){ meanImpedanceScores.remove(0.0); }

        if (meanImpedanceScores.size() > 3){
            double finalImpedanceScore;

            finalImpedanceScore = generateFinalScore();


            if (finalImpedanceScore != 0.0 && !Double.isNaN(finalImpedanceScore)){
                finishTest(finalImpedanceScore);
            }else{
                sigError.reportUpdate("Final score not significant");
            }
        }
    }

    private double establishingImpedance(double voltage){
        return voltage/ Constant.CYTON_CURRENT;
    }

    private double generateFinalScore() {
        double finalImpedanceScore = 0.0;
        int size = 0;
        for (Double score: meanImpedanceScores){
            finalImpedanceScore+= Math.sqrt(Math.pow(score,2));
            if (score != 0.0){
                size++;
            }
        }
        return establishingImpedance(finalImpedanceScore/size);
    }

    private void finishTest(double finalImpedanceScore) {
        sigError.reportUpdate("channel: " + channelOfFocus + " - vol: " + finalImpedanceScore);

        Intent intent = new Intent("toCytonService");
        intent.putExtra("purpose", "change channel");
        intent.putExtra("channel", channelOfFocus);
        intent.putExtra("connectivity", finalImpedanceScore);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }
}
