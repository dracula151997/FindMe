package com.project.semicolon.findme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.semicolon.findme.adapter.DividerItemDecoration;
import com.project.semicolon.findme.adapter.PhonesRecyclerAdapter;
import com.project.semicolon.findme.adapter.RecyclerTouchListener;
import com.project.semicolon.findme.broadcast.BatteryService;
import com.project.semicolon.findme.database.AppDatabase;
import com.project.semicolon.findme.database.ContactEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CONTACT_PICK = 100;
    private static final int READ_CONTACTS_PERMISSION = 200;
    private static final int LOCATION_PERMISSION_REQUEST = 300;
    private static final int SEND_SMS_PERMISSION_REQUEST = 400;
    private static final String TAG = MainActivity.class.getSimpleName();
    private PhonesRecyclerAdapter adapter;
    private AppDatabase database;
    private List<ContactEntity> contactEntities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getInstance(this);
        Intent intent = new Intent(this, BatteryService.class);
        startService(intent);
        fetchContactFromDatabase();
        getLastLocation();

        smsPermission();

        initRecyclerView();


        FloatingActionButton addContact = findViewById(R.id.add_contact_fab);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION);
                } else {
                    fetchContacts();

                }
            }
        });


    }

    private void smsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST);
            }

        }
    }

    private void getLastLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST);
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
            }

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                double latitude = lastLocation.getLatitude();
                double longitude = lastLocation.getLongitude();
                getGeocoderAddress(latitude, longitude);
                Log.d(TAG, "getLastLocation: latitude & longitude = " + latitude + ", " + longitude);
            }
        }
    }

    private void getGeocoderAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);
            Log.d(TAG, "getGeocoderAddress: addressLine: " + addressLine);
            //save addressLine;
            SharedHelper.saveLocation(this, "address", addressLine);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchContactFromDatabase() {
        database.dao().getAll().observe(this, new Observer<List<ContactEntity>>() {
            @Override
            public void onChanged(List<ContactEntity> entity) {
                contactEntities.clear();
                contactEntities.addAll(entity);
                adapter.setItems(contactEntities);

            }
        });

    }

    private void initRecyclerView() {
        RecyclerView phoneRecycler = findViewById(R.id.phones_recycler);
        phoneRecycler.setLayoutManager(new LinearLayoutManager(this));
        phoneRecycler.setHasFixedSize(true);
        adapter = new PhonesRecyclerAdapter();
        phoneRecycler.setAdapter(adapter);
        phoneRecycler.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST, 16));

        phoneRecycler.addOnItemTouchListener(new RecyclerTouchListener(this, phoneRecycler, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View v, int position) {

            }

            @Override
            public void onLongClick(View v, int position) {
                showDeleteDialog(position);

            }
        }));
    }

    private void showDeleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Phone number");
        builder.setMessage("Are you sure to delete this phone number?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteFromDatabase(position);

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    private void deleteFromDatabase(final int position) {
        AppExactors.getInstance().getDiskIo().execute(new Runnable() {
            @Override
            public void run() {
                database.dao().delete(contactEntities.get(position));
                adapter.delete(position);

            }
        });

    }

    private void fetchContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT_PICK);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CONTACT_PICK:
                if (data != null) {
                    Uri contactUri = data.getData();
                    convertUriToPhoneNumber(contactUri);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void convertUriToPhoneNumber(Uri contactUri) {
        String number;
        Cursor cursor = getContentResolver()
                .query(contactUri, null, null, null, null);
        String hasPhone = null;
        String contactId = null;

        if (cursor != null) {
            cursor.moveToFirst();
            hasPhone = cursor.
                    getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            contactId = cursor
                    .getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
        }


        if (hasPhone != null) {
            if (hasPhone.equals("1")) {
                Cursor phones = getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                                null,
                                null);

                while (phones.moveToNext()) {
                    number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replaceAll("[-()]", "");
                    final String displayName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    Toast.makeText(this, displayName + " is Added.", Toast.LENGTH_SHORT).show();
                    final String finalNumber = number;

                    AppExactors.getInstance().getDiskIo().execute(new Runnable() {
                        @Override
                        public void run() {
                            ContactEntity entity = new ContactEntity();
                            entity.setPhoneNumber(finalNumber);
                            entity.setContactName(displayName);
                            database.dao().insert(entity);
                        }
                    });

                }

                phones.close();

            } else {
                Toast.makeText(this, "The contact has no phone number",
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchContacts();
                } else {
                    Toast.makeText(this, "Until you grant the permission, we cannot display the names",
                            Toast.LENGTH_SHORT).show();
                }
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();

                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
