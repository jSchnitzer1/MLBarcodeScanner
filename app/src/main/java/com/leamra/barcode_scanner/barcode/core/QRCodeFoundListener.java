package com.leamra.barcode_scanner.barcode.core;

public interface QRCodeFoundListener {
    void onQRCodeFound(String qrCode);
    void onCodeNotFound();
    void onFailure(Exception e);
}
