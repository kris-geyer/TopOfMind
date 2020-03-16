package geyer.sensorlab.topofmind;

class Constant {

    static final int
            BOTH_PERMISSION_REQUEST = 0,
            CAMERA_PERMISSION_REQUEST = 1,
            WRITE_EXTERNAL_PERMISSION_REQUEST = 2,

            QR_ACTIVITY_RESULT = 2,
            QR_CODE_APPROPRIATE = 3,
            QR_CODE_INAPPROPRIATE= 4,

            SERVICE_BOUND = 1,

            INITIALIZING = 1,
            NOT_INITIALIZING = 2,
            IMPEDANCE = 3,
            DATA_COLLECTING = 4,



            CYTON_CURRENT = 6,


            CYTON_SERVICE_ID = 101;
    static final String
            USB_PERMISSION_REQUEST_FILTER = "tom.USB_PERMISSION",
            EEG_FILE_NAME = "eeg.txt",
            IMPEDANCE_FILE_NAME = "impedance.txt",
            ERROR_FILE_NAME = "error.txt";



}
