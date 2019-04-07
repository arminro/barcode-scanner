package com.company.arminro.logic;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.company.arminro.viewmodel.ScannerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.ArrayList;
import java.util.List;

public class BarcodeDetector {

    private FirebaseVisionBarcodeDetector detector;
    private BarcodeData temp;

    public BarcodeDetector(Activity current) {
        FirebaseApp.initializeApp(current);
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE)
                        .build();
        // improving performance a little by setting qr code detection explicitly
        detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
    }

    public BarcodeData Detect(BarcodeImage img){


        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(img.getVisionImage())
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        for (FirebaseVisionBarcode barcode: barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            int valueType = barcode.getValueType();
                            // See API reference for complete list of supported types
                            switch (valueType) {
                                    case FirebaseVisionBarcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    List<String> values = new ArrayList<>();
                                    values.add(url);
                                    temp = new BarcodeData(null, values);
                                    break;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        temp = new BarcodeData(e, null);
                    }
                });

                return temp;
    }

    public void setFormats(){}
}
