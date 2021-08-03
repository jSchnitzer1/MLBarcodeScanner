package com.leamra.barcode_scanner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.leamra.barcode_scanner.barcode.core.QRCodeFoundListener;
import com.leamra.barcode_scanner.barcode.core.QRCodeImageAnalyzer;
import com.leamra.barcode_scanner.ui.CameraViewOverlay;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 49;
    public String[] permissions = new String[4];

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    TextView value_barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraExecutor = Executors.newSingleThreadExecutor();

        assignPermissions();

        if (!checkPermissions(getApplicationContext()))
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        else {
            startCamera();
        }

        CameraViewOverlay overlay = findViewById(R.id.overlay);
        value_barcode = findViewById(R.id.value_barcode);
        overlay.post(() -> {
            overlay.setViewFinder();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            startCamera();
        } else {
            Toast.makeText(getApplicationContext(), "No permissions assigned!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        cameraExecutor.shutdown();
        super.onDestroy();
    }

    private void startCamera() {
        PreviewView previewView = findViewById(R.id.fragment_scan_barcode_preview_view);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(previewView.getWidth(), previewView.getHeight()))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);

                imageAnalysis.setAnalyzer(cameraExecutor, new QRCodeImageAnalyzer(new QRCodeFoundListener() {
                    @Override
                    public void onQRCodeFound(String qrCode) {
                        value_barcode.post(() -> {
                            value_barcode.setText(qrCode);
                        });
                    }

                    @Override
                    public void onCodeNotFound() {
                        System.out.println("not found");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }));


            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get()
                // shouldn't block since the listener is being called, so no need to
                // handle InterruptedException.
            }

        }, ContextCompat.getMainExecutor(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermissions(Context context) {
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void assignPermissions() {
        permissions[0] = Manifest.permission.CAMERA;
        permissions[1] = Manifest.permission.INTERNET;
        permissions[2] = Manifest.permission.READ_EXTERNAL_STORAGE;
        permissions[3] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }
}