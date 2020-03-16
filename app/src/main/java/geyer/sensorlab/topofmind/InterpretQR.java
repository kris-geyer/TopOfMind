package geyer.sensorlab.topofmind;

import com.google.gson.Gson;

import java.util.HashMap;

import timber.log.Timber;

class InterpretQR {

    private final String[] QRInputSplit;

    InterpretQR(String QRInput){
        this.QRInputSplit = QRInput.split("\n");
    }

    QRInstructions generateCalibrationInstructions() {
        Gson gson = new Gson();
        HashMap<String, String> calibrationInstructions = new HashMap<>();
        for(String input: QRInputSplit){
            String inputKey = input.substring(1, input.indexOf(":"));
            String inputValue = input.substring(input.indexOf(":") +1, input.length()-1);

            if (inputKey.equals("C")){
                char channel = input.charAt(3);
                int channelNum = 9;
                switch (channel){
                    case '0':
                        channelNum = 0;
                        break;
                    case '1':
                        channelNum = 1;
                        break;
                    case '2':
                        channelNum = 2;
                        break;
                    case '3':
                        channelNum = 3;
                        break;
                    case '4':
                        channelNum = 4;
                        break;
                    case '5':
                        channelNum = 5;
                        break;
                    case '6':
                        channelNum = 6;
                        break;
                    case '7':
                        channelNum = 7;
                        break;
                    case '8':
                        channelNum = 8;
                        break;
                    default:
                        Timber.i("Channel num : %s", channel);
                }

                String[] channelInstruction = new String[3];

                int count = 0;
                for (char c: inputValue.toCharArray()){
                    if (c == ':'){
                        count++;
                    }
                }

                if (count == 2){
                    channelInstruction = inputValue.split(":");
                    channelInstruction[0] = channelInstruction[0].split(",")[0];
                    channelInstruction[1] = channelInstruction[1].split(",")[0];

                }else{

                    channelInstruction[0] = "error";
                    channelInstruction[1] = "error";
                    channelInstruction[2] = "error";
                }

                calibrationInstructions.put("c" + channelNum, gson.toJson(new EEGChannel(channelNum, channelInstruction[1].equals("EEG") , channelInstruction[2])));
            }else{
                calibrationInstructions.put(inputKey, inputValue);
            }
        }

        QRInstructions qrInstructions = new QRInstructions();

        qrInstructions.updateInstructions(calibrationInstructions);

        return qrInstructions;
    }
}
