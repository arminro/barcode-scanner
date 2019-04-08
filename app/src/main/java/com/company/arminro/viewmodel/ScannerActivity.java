package com.company.arminro.viewmodel;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.company.arminro.logic.BarcodeData;
import com.company.arminro.logic.BarcodeDetector;
import com.company.arminro.logic.BarcodeImage;
import com.google.firebase.FirebaseApp;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/*camera functionality code is courtesy of
* https://developer.android.com/guide/topics/media/camera.html#custom-camera
* */
public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private Camera mCamera;
    private Preview mPreview;
    private Bitmap tempBmp;
    private static int cameraId;
    private ZXingScannerView mScannerView;
    private FloatingActionButton fab;
    private TextView urlTextView;
    private ImageView imgView;
    private boolean openUrlAutomatically;
    private BrowserControl browserControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        openUrlAutomatically = false;

        setContentView(R.layout.activity_scanner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        FirebaseApp.initializeApp(this);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        browserControl = new BrowserControl(this);



        // subscribing to the event of the browser control
        browserControl.setstartBrowserEventListener(new StartBrowserEventListener() {
            @Override
            public void startBrowser(String text) {
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(text));
                // we want to make sure that there is any app to handle the intent
                final ResolveInfo resolveInfo = getPackageManager().resolveActivity(browserIntent,PackageManager.MATCH_ALL);

                // if this is not null, we have an external app that can handle the intent
                if(resolveInfo != null){
                 startActivity(browserIntent);
                }
            }
        });
        setTitle("QR Scanner");
        cameraId = getCameraId();
        //mCamera = getCameraInstance();
        // setting the camera
        //mPreview = new Preview(this, mCamera);
        mScannerView = new ZXingScannerView(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        urlTextView = (TextView)findViewById(R.id.urlTextView);
        imgView = (ImageView) findViewById(R.id.contentImage);

        preview.addView(mScannerView);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanner, menu);
        return true;
    }

    // todo: this might not be needed
    private void releaseTempFiles(){
        tempBmp = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();


        mScannerView.startCamera(cameraId);
        mScannerView.setResultHandler(this);



    }
    public static int getCameraId(){
        Camera c = null;
        int i = 0;
        try {
            Camera.CameraInfo info = new Camera.CameraInfo();

            for (i = 0; i < Camera.getNumberOfCameras(); i++){
                info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    //c = Camera.open(i); // attempt to get the back camera instance
                    //cameraId = i;
                    Log.println(1, "CAM", "CAMERA CAPTURED");
                    break;
                }
            }

        }
        catch (Exception e){
            Log.e("CAM", "Could not start the camera: " + e.getMessage());
        }
        return i;
    }



    // capturing the image in a temp file for reuse
    private Camera.PictureCallback mPic = new Camera.PictureCallback(){

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                //tempBmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, info);

                BarcodeImage img = new BarcodeImage(data, info);
                Toast.makeText(ScannerActivity.this, "Image captured", Toast.LENGTH_SHORT).show();
                BarcodeDetector detector = new BarcodeDetector(ScannerActivity.this);
                BarcodeData barcodeData =  detector.Detect(img);
                if (barcodeData.getException() != null){
                    Toast.makeText(ScannerActivity.this, "Captured: " + barcodeData.getData(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ScannerActivity.this, "Captured: " + barcodeData.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onPause(){
        super.onPause();
        releaseTempFiles(); // we only need the temp files for the instance of capturing the picture
        mScannerView.stopCamera();
    }


    @Override
    public void handleResult(Result rawResult) {

        if (fab.isPressed()) {

            final String text = rawResult.getText();
            urlTextView.setText(text);
            if(UrlValidator.Validate(text)){
                imgView.setImageResource(R.drawable.net);

                if(openUrlAutomatically){
                    //  create an async call with a convenient delay
                    // https://stackoverflow.com/questions/10882611/how-to-make-a-delayed-non-blocking-function-call

                    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                    exec.schedule(new Runnable() {
                        public void run() {
                            browserControl.start(text);
                        }
                    }, 2, TimeUnit.SECONDS);

                }
                else {
                    urlTextView.setClickable(true);
                    urlTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            browserControl.start(text);
                        }
                    });
                    urlTextView.setTextColor(Color.BLUE);
                }
            }
            else{
                imgView.setImageResource(R.drawable.text);
            }
        }
        mScannerView.resumeCameraPreview(this);

    }




}
