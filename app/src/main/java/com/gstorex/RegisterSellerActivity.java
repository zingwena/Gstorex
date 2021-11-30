package com.gstorex;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {

    private ImageButton backBtn;
    private ImageButton gpsBtn;
    private ImageView profileIv;
    private EditText fullname;
    private EditText shopname;
    private EditText phone;
    private EditText country;
    private EditText state;
    private EditText city;
    private EditText fulladdress;
    private EditText email;
    private EditText password;
    private EditText confirmpwd;
    private Button register;
    private TextView registerUser;

    // permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;


    //permission  arrays
    private String[] localPermission;
    private String[] cameraPermission;
    private String[] storagePermission;


    private LocationManager locationManager;
    private double longtude, latitude;

    //image picked uri
    private Uri image_uri;
    //  @Nullable Intent intentdata;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);

        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        fullname = findViewById(R.id.fullname);
        shopname = findViewById(R.id.shopname);
        phone = findViewById(R.id.phone);
        country = findViewById(R.id.country);
        state = findViewById(R.id.state);
        city = findViewById(R.id.city);
        fulladdress = findViewById(R.id.fulladdress);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmpwd = findViewById(R.id.confirmpwd);
        register = findViewById(R.id.register);
        registerUser = findViewById(R.id.registerUser);

        //init permission
        localPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                ;
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick location
                if (checkLocalPermission()) {
                    detectLocation();
                } else {
                    requestLocalPermission();
                }
            }
        });

        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick profile image
                showImageDialog();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //register user
                inputData();
            }
        });

        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterSellerActivity.this, RegisterUserActivity.class));
            }
        });


    }

    private String fullName, shopName, phoneNumber, countryRes, stateRes, cityRes, address, emailRes, passwordRes, confirmPassword;

    private void inputData() {
        fullName = fullname.getText().toString().trim();
        shopName = shopname.getText().toString().trim();
        phoneNumber = phone.getText().toString().trim();
        countryRes = country.getText().toString().trim();
        stateRes = state.getText().toString().trim();
        cityRes = city.getText().toString().trim();
        address = fulladdress.getText().toString().trim();
        emailRes = email.getText().toString().trim();
        passwordRes = password.getText().toString().trim();
        confirmPassword = confirmpwd.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Full Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(shopName)) {
            Toast.makeText(this, "Enter Shop Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(countryRes)) {
            Toast.makeText(this, "Enter Country", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(stateRes)) {
            Toast.makeText(this, "Enter State", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(cityRes)) {
            Toast.makeText(this, "Enter City", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Enter Full Address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(emailRes)) {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude == 0.0 || longtude == 0.0) {
            Toast.makeText(this, "Please click GPS button  to detect location....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(passwordRes)) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Confirm Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!passwordRes.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords doesn't match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailRes).matches()) {
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordRes.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 long", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Create Account");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(emailRes, passwordRes)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account create
                        saveFireBaseData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                ;
                Toast.makeText(RegisterSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFireBaseData() {
        progressDialog.setMessage("Saving  Account Info... ");
        final String timestamp = "" + System.currentTimeMillis();
        if (image_uri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("email", "" + emailRes);
            hashMap.put("name", "" + fullName);
            hashMap.put("address", "" + address);
            hashMap.put("shopName", "" + shopName);
            hashMap.put("phone", "" + phoneNumber);
            hashMap.put("country", "" + countryRes);
            hashMap.put("city", "" + cityRes);
            hashMap.put("state", "" + stateRes);
            hashMap.put("longitude", "" + longtude);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("accountType", "Seller");
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("online", "true");
            hashMap.put("shopOpen", "true");
            hashMap.put("profileImage", "");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //ad updated
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSellerActivity.this, MainSellerActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterSellerActivity.this, MainSellerActivity.class));
                            finish();
                        }
                    });

        } else {
            // save info with image

            //name and path of image
            String filePatheAndName = "profile_image/" + "" + firebaseAuth.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePatheAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // get image url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();
                            if (uriTask.isSuccessful()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("email", "" + emailRes);
                                hashMap.put("name", "" + fullName);
                                hashMap.put("address", "" + address);
                                hashMap.put("shopName", "" + shopName);
                                hashMap.put("phone", "" + phoneNumber);
                                hashMap.put("country", "" + countryRes);
                                hashMap.put("city", "" + cityRes);
                                hashMap.put("state", "" + stateRes);
                                hashMap.put("longitude", "" + longtude);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("accountType", "Seller");
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("online", "true");
                                hashMap.put("shopOpen", "true");
                                hashMap.put("profileImage", "" + downloadImageUri);// url of uploaded image

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //ad updated
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerActivity.this, MainSellerActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterSellerActivity.this, MainSellerActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImageDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0) {
                    //camera clicked
                    if (checkCameraPermission()) {
                        pickFromCamera();
                    } else {
                        //not allowed request
                        requestCameraPermission();
                    }

                } else {
                    //gallery
                    if (checkStoragePermission()) {
                        pickFromGallery();
                    } else {
                        requestStoragePermission();
                    }


                }
            }
        }).show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        //someActivityResultLauncher.launch(intent);
        //Intent intent = new Intent(Intent.ACTION_PICK);
        // intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please Wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(RegisterSellerActivity.this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longtude, 1);
            String address = addresses.get(0).getAddressLine(0);// full address
            String cityLoc = addresses.get(0).getLocality();
            String stateLoc = addresses.get(0).getAdminArea();
            String countryLoc = addresses.get(0).getCountryName();

            //set address
            country.setText(countryLoc);
            state.setText(stateLoc);
            city.setText(cityLoc);
            fulladdress.setText(address);

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
        //someActivityResultLauncher.launch(intent);

    }

    /* ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
             new ActivityResultContracts.StartActivityForResult(),
             new ActivityResultCallback<ActivityResult>() {
                 @Override
                 public void onActivityResult(ActivityResult result) {
                     if (result.getResultCode() == IMAGE_PICK_GALLERY_CODE) {
                         image_uri = intentdata.getData();
                         profileIv.setImageURI(image_uri);
                     } else if (result.getResultCode() == IMAGE_PICK_CAMERA_CODE) {
                         profileIv.setImageURI(image_uri);

                     }
                 }
             });*/
    private boolean checkLocalPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocalPermission() {
        ActivityCompat.requestPermissions(this, localPermission, LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, storagePermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
      //detect loaction
        longtude = location.getLongitude();
        latitude = location.getLatitude();
        findAddress();
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {

    }

    @Override
    public void onFlushComplete(int requestCode) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this, "Please turn on Location....", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        detectLocation();
                    } else {
                        Toast.makeText(this, "Location Permission is Required.....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    //  boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera Permission is Required.....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage Permission is Required.....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                profileIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                profileIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
