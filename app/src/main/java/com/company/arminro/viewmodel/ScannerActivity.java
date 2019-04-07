package com.company.arminro.viewmodel;


import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.company.arminro.logic.BarcodeData;
import com.company.arminro.logic.BarcodeDetector;
import com.company.arminro.logic.BarcodeImage;
import com.google.firebase.FirebaseApp;

/*camera functionality code is courtesy of
* https://developer.android.com/guide/topics/media/camera.html#custom-camera
* */
public class ScannerActivity extends AppCompatActivity {

    private Camera mCamera;
    private Preview mPreview;
    private Bitmap tempBmp;
    private static int cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseApp.initializeApp(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCamera.takePicture(null, null, mPic);
                mCamera.startPreview();
            }
        });
        setTitle("QR Scanner");

        mCamera = getCameraInstance();
        // setting the camera
        mPreview = new Preview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
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


    }
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++){
                info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    c = Camera.open(i); // attempt to get the back camera instance
                    cameraId = i;
                    Log.println(1, "CAM", "CAMERA CAPTURED");
                    break;
                }
            }

        }
        catch (Exception e){
            Log.e("CAM", "Could not start the camera: " + e.getMessage());
        }
        return c; // returns null if camera is unavailable
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
    }





}
