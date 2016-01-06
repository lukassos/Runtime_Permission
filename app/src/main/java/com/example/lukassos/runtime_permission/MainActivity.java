package com.example.lukassos.runtime_permission;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity implements OnClickListener, RuntimePermissionsUtils.Callback {
    private final static int COARSE_LOCATION_RESULT = 100;
    private final static int FINE_LOCATION_RESULT = 101;
    private final static int CALL_PHONE_RESULT = 102;
    private final static int CAMERA_RESULT = 103;
    private final static int READ_CONTACTS_RESULT = 104;
    private final static int WRITE_EXTERNAL_RESULT = 105;
    private final static int RECORD_AUDIO_RESULT = 106;
    private final static int ALL_PERMISSIONS_RESULT = 107;


    private SharedPreferences sharedPreferences;
    private Button btnLocationFine, btnLocationCoarse,
            btnCamera, btnContacts, btnMicrophone,
            btnPhone, btnStorageWrite, btnRequestAll;
    private FrameLayout permissionSuccess;


    private View coordinatorLayoutView;
    private Handler mSuccessMessageHandler;
    private Runnable mSuccessMessageRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //assign views
        coordinatorLayoutView = findViewById(R.id.snackbarPosition);
        permissionSuccess = (FrameLayout)findViewById(R.id.permissionSuccess);
        btnLocationFine = (Button)findViewById(R.id.btnLocationFine);
        btnLocationCoarse = (Button)findViewById(R.id.btnLocationCoarse);
        btnCamera = (Button)findViewById(R.id.btnCamera);
        btnContacts = (Button)findViewById(R.id.btnContacts);
        btnMicrophone = (Button)findViewById(R.id.btnMicrophone);
        btnPhone = (Button)findViewById(R.id.btnPhone);
        btnStorageWrite = (Button)findViewById(R.id.btnStorageWrite);
        btnRequestAll = (Button)findViewById(R.id.btnRequestAll);

        //set click listeners
        btnLocationFine.setOnClickListener(this);
        btnLocationCoarse.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnContacts.setOnClickListener(this);
        btnMicrophone.setOnClickListener(this);
        btnPhone.setOnClickListener(this);
        btnStorageWrite.setOnClickListener(this);
        btnRequestAll.setOnClickListener(this);

        // only for beauty : handler for timed show/gone of permissionSuccess
        mSuccessMessageHandler = new Handler();
        mSuccessMessageRunnable = new Runnable() {
            public void run() {
                //after some time shut the granted  message
                permissionSuccess.setVisibility(View.GONE);
            }
        };
    }


    @Override
    public void onClick(View v) {
        permissionSuccess.setVisibility(View.GONE);
        ArrayList<String> permissions = new ArrayList<>();
        switch(v.getId()){
            case R.id.btnLocationFine:
                permissions.add(ACCESS_FINE_LOCATION);
                break;
            case R.id.btnLocationCoarse:
                permissions.add(ACCESS_COARSE_LOCATION);
                break;
            case R.id.btnCamera:
                permissions.add(CAMERA);
                break;
            case R.id.btnContacts:
                permissions.add(READ_CONTACTS);
                break;
            case R.id.btnMicrophone:
                permissions.add(RECORD_AUDIO);
                break;
            case R.id.btnPhone:
                permissions.add(CALL_PHONE);
                break;
            case R.id.btnStorageWrite:
                permissions.add(WRITE_EXTERNAL_STORAGE);
                break;
            case R.id.btnRequestAll:
                permissions.add(ACCESS_FINE_LOCATION);
                permissions.add(ACCESS_COARSE_LOCATION);
                permissions.add(CAMERA);
                permissions.add(READ_CONTACTS);
                permissions.add(RECORD_AUDIO);
                permissions.add(CALL_PHONE);
                permissions.add(WRITE_EXTERNAL_STORAGE);
                break;
        }


        RuntimePermissionsUtils.request(this, permissions, this);
    }


    /**
     * This is the method that is hit after the user accepts/declines the
     * permission you requested. For the purpose of this example I am showing a "success" header
     * when the user accepts the permission and a snackbar when the user declines it.  In your application
     * you will want to handle the accept/decline in a way that makes sense.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        RuntimePermissionsUtils.verify(this, requestCode, this);

    }

    @Override
    public void onPermissionGranted(RuntimePermissionsUtils.PermissionStatus status) {
        // show success message
        permissionSuccess.setVisibility(View.VISIBLE);
        // hide after time period pass
        int SLEEP_INTERVAL_MS = 1500;
        mSuccessMessageHandler.removeCallbacks(mSuccessMessageRunnable);
        mSuccessMessageHandler.postDelayed(mSuccessMessageRunnable, SLEEP_INTERVAL_MS);
    }

    @Override
    public void onPermissionDenied(RuntimePermissionsUtils.PermissionStatus status) {
        makePostRequestSnackFromDenied(this, status.denied);
    }

    /**
     * a method that will centralize the showing of a Snackbar
     */
    private void makePostRequestSnackFromDenied(final Activity context, final ArrayList<String> permissionsRejected) {
        Snackbar
                .make(coordinatorLayoutView, String.valueOf(permissionsRejected.size()) + " permission(s) were rejected", Snackbar.LENGTH_LONG)
                .setAction("Allow to Ask Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (String perm : permissionsRejected) {
                            RuntimePermissionsUtils.clearMarkAsAsked(context, perm);
                        }
                    }
                })
                .show();
    }
}
