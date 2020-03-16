package geyer.sensorlab.topofmind;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

class ExternalDataStorage {

    final String fileName;
    final File file;
    final FileOutputStream fileOutputStream;

    ExternalDataStorage (String fileName) throws FileNotFoundException {
        this.fileName = fileName;
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + File.separator + "brain_stuff");
        dir.mkdir();

        this.file = new File(dir, this.fileName);
        this.fileOutputStream = new FileOutputStream(this.file, true);
    }

    void writeToFile(byte[] toEnter) throws IOException {
        fileOutputStream.write(toEnter);
    }

    void writeToFile(String toEnter) throws IOException {
        fileOutputStream.write(toEnter.getBytes());
    }

    void addEEGData(EEGDataRow row) throws IOException {
        String stringBuilder =
                row.row + " : " +
                        row.chan1 + " : " +
                        row.chan2 + " : " +
                        row.chan3 + " : " +
                        row.chan4 + " : " +
                        row.chan5 + " : " +
                        row.chan6 + " : " +
                        row.chan7 + " : " +
                        row.chan8 + " : " +
                        row.timestamp + "\n";
        fileOutputStream.write(stringBuilder.getBytes());
    }

    void closeFile () throws IOException {
        fileOutputStream.close();
    }
}
