package geyer.sensorlab.topofmind;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class ExternalDataStorage {

    private final String fileName;
    private final File file;
    private final FileOutputStream fileOutputStream;
    private final int numberChannels;


    ExternalDataStorage (String fileName, int numberChannels) throws FileNotFoundException {
        this.fileName = fileName;
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + File.separator + "brain_stuff");
        dir.mkdir();

        this.file = new File(dir, this.fileName);
        this.fileOutputStream = new FileOutputStream(this.file, true);
        this.numberChannels = numberChannels;
    }

    void writeToFile(byte[] toEnter) throws IOException {
        fileOutputStream.write(toEnter);
    }

    void writeToFile(String toEnter) throws IOException {
        fileOutputStream.write(toEnter.getBytes());
    }

    void addEEGData(EEGDataRow row) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(row.row).append(" : ");

        for (Double signal: row.signal){
            stringBuilder.append(signal).append(" : ");
        }
        stringBuilder.append(row.timestamp).append("\n");

        fileOutputStream.write(stringBuilder.toString().getBytes());
    }

    void closeFile () throws IOException {
        fileOutputStream.close();
    }
}
