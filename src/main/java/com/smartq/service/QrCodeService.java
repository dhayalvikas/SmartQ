package com.smartq.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class QrCodeService {

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    // Generate QR code as Base64 string
    public String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    QR_WIDTH,
                    QR_HEIGHT
            );

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    bitMatrix, "PNG", outputStream);

            byte[] qrBytes = outputStream.toByteArray();
            return "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(qrBytes);

        } catch (WriterException | IOException e) {
            throw new RuntimeException(
                    "Failed to generate QR code: " + e.getMessage());
        }
    }

    // Generate QR code as byte array (for download)
    public byte[] generateQrCodeBytes(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    QR_WIDTH,
                    QR_HEIGHT
            );

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException(
                    "Failed to generate QR code: " + e.getMessage());
        }
    }
}