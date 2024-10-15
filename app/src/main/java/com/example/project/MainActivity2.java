package com.example.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.project.databinding.ActivityMain2Binding;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ProductAdapter.OnRemoveClickListener, ProductAdapter.SendDataToArduinoListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 2;
    private static final String ARDUINO_DEVICE_ADDRESS = "98:D3:61:F7:32:07"; // Replace with your Arduino's Bluetooth MAC address
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard UUID for SPP (Serial Port Profile)

    private FragmentManager fragmentManager;
    private ActivityMain2Binding binding;
    private DBHelper DB;
    private List<String> productList;
    private ProductAdapter productAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice arduinoDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        productList = new ArrayList<>();

        // Initialize DBHelper
        DB = new DBHelper(this);

        // Check and request Bluetooth permissions
        checkBluetoothPermission();

        // Initialize RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList, this, this);
        binding.recyclerView.setAdapter(productAdapter);

        loadProductsFromDatabase(); // Load products from the database

        // Initialize SearchView
        setupSearchView();

        // Set up the ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.nav_open,
                R.string.nav_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navigationDrawer.setNavigationItemSelectedListener(this);
        binding.navigationDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.log_Out:
                        Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                        startActivity(intent);
                        return true; // Indicate that the item selection is handled
                    case R.id.about_us:
                        Intent intent1 = new Intent(MainActivity2.this, AboutUsActivity.class);
                        startActivity(intent1);// Indicate that the item selection is handled
                        return true;
                    case R.id.rate_us:
                        Intent intent2 = new Intent(MainActivity2.this, RateUsActivity.class);
                        startActivity(intent2);// Indicate that the item selection is handled
                        return true;
                    default:
                        return false; // For other items, indicate they are not handled
                }
            }
        });
        binding.bottomNavigation.setBackground(null);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    openFragment(new HomeFragment());
                    break;
                case R.id.Menu:
                    openFragment(new MenuFragment());
                    break;
            }
            return true;
        });
        fragmentManager = getSupportFragmentManager();
        openFragment(new HomeFragment());

        binding.fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity2.this, QRCodeScannerActivity.class);
            startActivity(intent);
        });
    }

    private void checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {
            // Bluetooth permission granted, initialize Bluetooth
            setupBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Bluetooth permission granted, initialize Bluetooth
                setupBluetooth();
            } else {
                showToastOnMainThread("Bluetooth permission denied");
            }
        }
    }

    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.filter(newText != null ? newText : "");
                return true;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToastOnMainThread("Bluetooth not supported");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            arduinoDevice = bluetoothAdapter.getRemoteDevice(ARDUINO_DEVICE_ADDRESS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                arduinoDevice = bluetoothAdapter.getRemoteDevice(ARDUINO_DEVICE_ADDRESS); // Bluetooth is enabled, proceed with getting the device
            } else {
                showToastOnMainThread("Bluetooth enabling cancelled or failed");
            }
        }
    }

    private void showToastOnMainThread(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity2.this, message, Toast.LENGTH_LONG).show());
    }

    @SuppressLint("MissingPermission")
    private boolean isConnectedToArduino() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            showToastOnMainThread("Connected to Bluetooth HC05");
            return true;
        } else {
            showToastOnMainThread("Not connected to Bluetooth HC05");
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToArduinoAndSendData(String data) {
        new Thread(() -> {
            try {
                // Ensure Bluetooth is enabled
                if (!bluetoothAdapter.isEnabled()) {
                    showToastOnMainThread("Bluetooth is disabled. Please enable Bluetooth and try again.");
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
                        showToastOnMainThread("Failed to connect to Arduino. Retrying...");
                    }
                }

                if (!connected) {
                    showToastOnMainThread("Failed to connect to Arduino after multiple attempts");
                    return;
                }

                // Connection successful
                showToastOnMainThread("Connected to Arduino");

                // Get the output stream
                outputStream = bluetoothSocket.getOutputStream();

                // Send data to Arduino
                outputStream.write(data.getBytes());
                showToastOnMainThread("Data sent to Arduino: " + data);
            } catch (IOException e) {
                e.printStackTrace();
                showToastOnMainThread("Failed to connect to Arduino or send data");
            } finally {
                // Close the connection
                closeBluetoothConnection();
            }
        }).start();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent); // This closes all activities and exits the app
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @SuppressLint("Range")
    private void loadProductsFromDatabase() {
        productList.clear();
        Cursor cursor = DB.getAllQRCodes(); // Check if cursor is null
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String data = cursor.getString(cursor.getColumnIndex("data"));
                productList.add(data);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        productAdapter.filter(""); // Load all products initially
    }

    private void removeProduct(String dataToRemove) {
        boolean removed = DB.removeQRCode(dataToRemove);
        if (removed) {
            productList.remove(dataToRemove);
            productAdapter.filter(""); // Refresh the adapter to show the updated list
            Toast.makeText(MainActivity2.this, "Data removed successfully", Toast.LENGTH_SHORT).show();
            connectToArduinoAndSendData(dataToRemove + "2");
        } else {
            Toast.makeText(MainActivity2.this, "Failed to remove data", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void closeBluetoothConnection() {
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

    @Override
    public void onRemoveClick(String product) {
        removeProduct(product);
    }

    @Override
    public void sendDataToArduino(String data) {
        if (isConnectedToArduino()) {
            connectToArduinoAndSendData(data);
        }
    }
}
