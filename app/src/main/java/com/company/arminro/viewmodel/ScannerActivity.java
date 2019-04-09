package com.company.arminro.viewmodel;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.google.zxing.Result;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/*camera functionality code is courtesy of
* https://developer.android.com/guide/topics/media/camera.html#custom-camera
* */
public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private Camera mCamera;
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
        setSupportActionBar(toolbar);

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



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            // for this simple app, we will send the value of the boolean flag and set it in the settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(getString(R.string.browse_automatically), openUrlAutomatically);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        openUrlAutomatically = getIntent().getBooleanExtra(getString(R.string.browse_automatically), false);
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






    @Override
    protected void onPause(){
        super.onPause();
        mScannerView.stopCamera();
    }


    @Override
    public void handleResult(Result rawResult) {

        if (rawResult.getText() != null && !rawResult.getText().isEmpty() && fab.isPressed() ) {

            final String text = rawResult.getText();
            urlTextView.setText(text);
            if(UrlValidator.Validate(text)){
                imgView.setImageResource(R.drawable.net);

                if(openUrlAutomatically){
                    // create an async call with a convenient delay
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
