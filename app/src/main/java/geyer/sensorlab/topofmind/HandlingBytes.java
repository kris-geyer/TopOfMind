package geyer.sensorlab.topofmind;

 class HandlingBytes {

    int myInterpret24bitAsInt32(byte[] byteArray) {
        int newInt = (((0xFF & byteArray[0]) << 16) |
                ((0xFF & byteArray[1]) << 8) |
                (0xFF & byteArray[2]));

        if ((newInt & 0x00800000) > 0) {
            newInt |= 0xFF000000;
        } else {
            newInt &= 0x00FFFFFF;
        }
        return newInt;
    }

    String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    double changeToMicroVolts(int input){
        return (input*0.02235);
    }

    int interpretHexAsInt32(String hex){
        final  String digits = "0123456789ABCDEF";

        int val = 0;
        hex = hex.toUpperCase();
        for (int i = 0; i < hex.length(); i++)
        {
            val = 16*val + digits.indexOf(hex.charAt(i));
        }
        return val;
    }

}
