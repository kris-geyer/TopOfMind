package geyer.sensorlab.topofmind;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScanner extends Activity implements ZXingScannerView.ResultHandler  {
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);
        mScannerView.setAutoFocus(true);
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent returnIntent = new Intent();
        if(rawResult.getText() != null){
            returnIntent.putExtra("result", rawResult.getText());
            setResult(Constant.QR_CODE_APPROPRIATE, returnIntent);
        }else{
            setResult(Constant.QR_CODE_INAPPROPRIATE);
        }
        finish();
    }
}
