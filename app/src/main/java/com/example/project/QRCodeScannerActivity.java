package com.example.project;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class QRCodeScannerActivity extends AppCompatActivity {
    DBHelper DB;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 201;
    private static final String ARDUINO_DEVICE_ADDRESS = "98:D3:61:F7:32:07"; // Replace with your Arduino's Bluetooth MAC address
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard UUID for SPP (Serial Port Profile)
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice arduinoDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB= new DBHelper(this);
        setContentView(R.layout.activity_qrcode_scanner);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Check Bluetooth permission
            checkBluetoothPermission();
        }

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get Arduino Bluetooth device
        arduinoDevice = bluetoothAdapter.getRemoteDevice(ARDUINO_DEVICE_ADDRESS);
    }

    private void checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {
            // Bluetooth permission granted, start scanner
            startScanner();
        }
    }

    private void startScanner() {
        new IntentIntegrator(this)
                .setPrompt("Scan a QR Code")
                .setOrientationLocked(false)
                .initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check Bluetooth permission
                checkBluetoothPermission();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                // Finish the activity
                finish();
            }
        } else if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Bluetooth permission granted, start scanner
                startScanner();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Bluetooth permission is required to connect to devices", Toast.LENGTH_SHORT).show();
                // Finish the activity
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if the request code matches the QR code scanner
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    // Scanning canceled
                    Toast.makeText(this, "Scanning canceled", Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(QRCodeScannerActivity.this,MainActivity2.class);
                    startActivity(intent);
                } else {
                    // QR code scanned successfully
                    String scannedData = result.getContents();
                    Toast.makeText(this, "Scanned: " + scannedData, Toast.LENGTH_LONG).show();

                    if (DB.isQRCodeExist(scannedData)) {
                        Intent intent = new Intent(this, MainActivity2.class);
                        startActivity(intent);
                        Toast.makeText(this, "The scanned code already exists in the database", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(this, MainActivity2.class);
                        startActivity(intent);
                        Toast.makeText(this, "Scanned: " + scannedData, Toast.LENGTH_LONG).show();
                        connectToArduinoAndSendData(scannedData);
                        DB.insertQRCode(scannedData);
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToArduinoAndSendData(String data) {
        try {
            // Ensure Bluetooth is enabled
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth is disabled. Please enable Bluetooth and try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retry mechanism for connection
            boolean connected = false;
            for (int i = 0; i < 1; i++) { // Try to connect up to 3 times
                try {
                    bluetoothSocket = arduinoDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect();
                    connected = true;
                    break; // Break the loop if connected
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to connect to Arduino. Retrying...", Toast.LENGTH_SHORT).show();
                }
            }

            if (!connected) {
                Toast.makeText(this, "Failed to connect to Arduino after multiple attempts", Toast.LENGTH_SHORT).show();
                return;
            }

            // Connection successful
            Toast.makeText(this, "Connected to Arduino", Toast.LENGTH_SHORT).show();

            // Get the output stream
            outputStream = bluetoothSocket.getOutputStream();

            // Send data to Arduino
            outputStream.write(data.getBytes());
            Toast.makeText(this, "Data sent to Arduino: " + data, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to connect to Arduino or send data", Toast.LENGTH_SHORT).show();
        } finally {
            // Close the connection
            closeBluetoothConnection();
        }
    }

    private void closeBluetoothConnection() {
        // Close the connection
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close Bluetooth socket and streams
        closeBluetoothConnection();
    }
}
