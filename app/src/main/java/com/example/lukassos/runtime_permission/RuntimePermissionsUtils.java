package com.example.lukassos.runtime_permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.v4.app.ActivityCompat.requestPermissions;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * Created by lukassos on 1/6/2016. All rights reserved.
 */
public class RuntimePermissionsUtils {
    private static final String TAG = "RuntimePermissionsUtils";
    private static final String REQUESTED_PERMISSIONS = "requested_permissions";

    private final static int COARSE_LOCATION_RESULT = 101;
    private final static int FINE_LOCATION_RESULT = 201;
    private final static int CALL_PHONE_RESULT = 301;
    private final static int CAMERA_RESULT = 401;
    private final static int READ_CONTACTS_RESULT = 501;
    private final static int WRITE_EXTERNAL_RESULT = 601;
    private final static int RECORD_AUDIO_RESULT = 701;
    // TODO : add other result codes - increment by 100

    /**
     * Model Class for storing info of requesting permissions process
     */
    public static class PermissionStatus {
        ArrayList<String> requested;
        ArrayList<String> granted;
        ArrayList<String> denied;

        public PermissionStatus(
                ArrayList<String> granted,
                ArrayList<String> requested,
                ArrayList<String> denied) {
            this.granted = granted;
            this.requested = requested;
            this.denied = denied;
        }

        public PermissionStatus() {
            this.granted = new ArrayList<>();
            this.requested = new ArrayList<>();
            this.denied = new ArrayList<>();
        }


        public void addGranted(String perm) {
            if (!granted.contains(perm))
                this.granted.add(perm);
            if (!requested.contains(perm))
                this.requested.add(perm);
            if (denied.contains(perm))
                this.denied.remove(perm);
        }

        public void addGranted(List<String> perms) {
            for (String perm : perms) {
                this.addGranted(perm);
            }
        }

        public void addDenied(String perm) {
            if (!denied.contains(perm))
                this.denied.add(perm);
            if (!requested.contains(perm))
                this.requested.add(perm);
            if (granted.contains(perm))
                this.granted.remove(perm);
        }

        public void addDenied(List<String> perms) {
            for (String perm : perms) {
                this.addDenied(perm);
            }
        }

        public void removeFromAll(String perm) {
            if (denied.contains(perm))
                this.denied.remove(perm);
            if (requested.contains(perm))
                this.requested.remove(perm);
            if (granted.contains(perm))
                this.granted.remove(perm);
        }
    }

    public interface Callback {
        void onPermissionGranted(PermissionStatus status);

        void onPermissionDenied(PermissionStatus status);

    }


    /**
     * This method allows developer to easily request permission/s
     * anywhere within some Activity Context
     *
     * @param activityContext
     * @param permissions
     */
    @SuppressLint("NewApi") // we can suppress it this time : canMakeSmores() tests for api level
    public static void request(Activity activityContext, ArrayList<String> permissions, Callback callback) {
        PermissionStatus status = new PermissionStatus();

        //but have we already asked for them?
        //filter out the permissions we have already accepted
        ArrayList<String> permissionsToRequest = findUnAskedPermissions(activityContext, permissions);
        //get the permissions we have asked for before but were not granted..
        //we will store this in a global list to access later.
        ArrayList<String> permissionsRejected = findRejectedPermissions(activityContext, permissions);

        //ask for those unasked first, then verify if we asked for some previously rejected
        if (permissionsToRequest.size() > 0) {//we need to ask for permissions
            // ask only if this dev is running on Marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // save those we last asked for
                savePermissionsRequested(activityContext, permissionsToRequest);

                activityContext.requestPermissions(
                        permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                        requestCode(permissionsToRequest)
                );
            } else {
                // fallback on older APIs to let developer know of missing something in Manifesto
                Log.e(TAG, "request: Permissions requested are missing in manifest! Add following : " + permissionsToRequest, new SecurityException());
            }
            //mark all these as asked..
            for (String perm : permissionsToRequest) {
                markAsAsked(activityContext, perm);
            }
        } else {
            // if we add all permissions ...
            status.addGranted(permissions);
            // ... then add also rejected ones,
            // addDenied will remove all permission in rejected ones from status.granted
            // leaving only granted ones in status.granted

            status.addDenied(permissionsRejected);
            //show the success
            if (permissionsRejected.size() < permissions.size()) {
                //this means we can show success because some were already accepted.
                callback.onPermissionGranted(status);
            }

            if (permissionsRejected.size() > 0) {
                //we have none to request but some previously rejected..tell the user.
                //It may be better to show a dialog here in a prod application
                callback.onPermissionDenied(status);
            }
        }
    }


    /**
     * This is the method that is hit after the user accepts/declines the
     * permission you requested. It have to be called inside of activity in onRequestPermissionsResult
     *
     * @param context
     * @param requestCode
     * @param callback
     */
    public static void verify(Context context, int requestCode, Callback callback) {
        PermissionStatus status = new PermissionStatus();
        switch (requestCode) {
            case FINE_LOCATION_RESULT:
                if (hasPermission(context, ACCESS_FINE_LOCATION)) {
                    status.addGranted(ACCESS_FINE_LOCATION);
                    callback.onPermissionGranted(status);

                } else {

                    status.addDenied(ACCESS_FINE_LOCATION);
                    callback.onPermissionDenied(status);
                }
                break;
            case COARSE_LOCATION_RESULT:
                if (hasPermission(context, ACCESS_COARSE_LOCATION)) {
                    status.addGranted(ACCESS_COARSE_LOCATION);
                    callback.onPermissionGranted(status);

                } else {
                    status.addDenied(ACCESS_COARSE_LOCATION);
                    callback.onPermissionDenied(status);
                }
                break;
            case CALL_PHONE_RESULT:
                if (hasPermission(context, CALL_PHONE)) {
                    status.addGranted(CALL_PHONE);
                    callback.onPermissionGranted(status);
                } else {
                    status.addDenied(CALL_PHONE);
                    callback.onPermissionDenied(status);
                }
                break;
            case CAMERA_RESULT:
                if (hasPermission(context, CAMERA)) {
                    status.addGranted(CAMERA);
                    callback.onPermissionGranted(status);
                } else {
                    status.addDenied(CAMERA);
                    callback.onPermissionDenied(status);
                }
                break;
            case READ_CONTACTS_RESULT:
                if (hasPermission(context, READ_CONTACTS)) {
                    status.addGranted(READ_CONTACTS);
                    callback.onPermissionGranted(status);
                } else {
                    status.addDenied(READ_CONTACTS);
                    callback.onPermissionDenied(status);
                }
                break;
            case WRITE_EXTERNAL_RESULT:
                if (hasPermission(context, WRITE_EXTERNAL_STORAGE)) {
                    status.addGranted(WRITE_EXTERNAL_STORAGE);
                    callback.onPermissionGranted(status);
                } else {
                    status.addDenied(WRITE_EXTERNAL_STORAGE);
                    callback.onPermissionDenied(status);
                }
                break;
            case RECORD_AUDIO_RESULT:
                if (hasPermission(context, RECORD_AUDIO)) {
                    status.addGranted(RECORD_AUDIO);
                    callback.onPermissionGranted(status);
                } else {
                    status.addDenied(RECORD_AUDIO);
                    callback.onPermissionDenied(status);
                }
                break;
            default:
                if (isCombinedRequestCode(requestCode)) {
                    boolean someAccepted = false;
                    boolean someRejected = false;
                    for (String perm : permissionsToRequest(context)) {
                        if (hasPermission(context, perm)) {
                            someAccepted = true;
                            status.addGranted(perm);
                        } else {
                            someRejected = true;
                            status.addDenied(perm);
                        }
                    }

                    if (status.denied.size() > 0) {
                        someRejected = true;
                    }

                    if (someAccepted) {
                        callback.onPermissionGranted(status);
                    }
                    if (someRejected) {
                        callback.onPermissionDenied(status);
                    }
                }
                break;
        }

    }

    /**
     * Restores previously saved requested permissions from Shared Prefs
     * @param context
     */
    private static ArrayList<String> permissionsToRequest(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ArrayList<>(PreferenceManager.getDefaultSharedPreferences(context).getStringSet(REQUESTED_PERMISSIONS, new ArraySet<String>()));
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     * Saves currently requested permissions to Shared Prefs
     * @param context
     * @param perms
     */

    private static void savePermissionsRequested(Context context, ArrayList<String> perms) {
        ArraySet<String> set = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            set = new ArraySet<String>();
            for (String perm : perms) {
                set.add(perm);
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(REQUESTED_PERMISSIONS, set);
        }
    }

    /**
     * method that will return whether the permission is accepted.
     *
     * @param context
     * @param permission
     * @return
     */
    private static boolean hasPermission(Context context, String permission) {
        return (checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * method to determine whether we have asked
     * for this permission before.. if we have, we do not want to ask again.
     * They either rejected us or later removed the permission.
     *
     * @param context
     * @param permission
     * @return
     */
    private static boolean shouldWeAsk(Context context, String permission) {
        return (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(permission, true));
    }

    /**
     * we will save that we have already asked the user
     *
     * @param context
     * @param permission
     */
    private static void markAsAsked(Context context, String permission) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(permission, false).apply();
    }

    /**
     * We may want to ask the user again at their request.. Let's clear the
     * marked as seen preference for that permission.
     *
     * @param context
     * @param permission
     */
    public static void clearMarkAsAsked(Context context, String permission) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(permission, true).apply();
    }


    /**
     * This method is used to determine the permissions we do not have accepted yet and ones that we have not already
     * bugged the user about.  This comes in handle when you are asking for multiple permissions at once.
     *
     * @param context
     * @param wanted
     * @return
     */
    private static ArrayList<String> findUnAskedPermissions(Context context, ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(context, perm) && shouldWeAsk(context, perm)) {
                result.add(perm);
            }
        }

        return result;
    }


    /**
     * this will return us all the permissions we have previously asked for but
     * currently do not have permission to use. This may be because they declined us
     * or later revoked our permission. This becomes useful when you want to tell the user
     * what permissions they declined and why they cannot use a feature.
     *
     * @param context
     * @param wanted
     * @return
     */
    private static ArrayList<String> findRejectedPermissions(Context context, ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(context, perm) && !shouldWeAsk(context, perm)) {
                result.add(perm);
            }
        }

        return result;
    }

// sorry Smores .. Android Studio precompiler does not jump through methods just reads the condition
//    /**
//     * Just a check to see if we have marshmallows (version 23)
//     *
//     * @return
//     */
//    private static boolean canMakeSmores() {
//        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
//    }

    /**
     * This method verifies if more than one request were made
     *
     * @param reqCode
     * @return
     */
    private static boolean isCombinedRequestCode(int reqCode) {
        return (reqCode % 100 > 1);
    }

    /**
     * This method combines all the permissions requested into one int request code
     *
     * @param permissions
     * @return
     */
    private static int requestCode(ArrayList<String> permissions) {
        int resultCode = 0;
        for (String permission : permissions) {
            if (permission.contains(ACCESS_COARSE_LOCATION)) {
                resultCode += COARSE_LOCATION_RESULT;
            } else if (permission.contains(ACCESS_FINE_LOCATION)) {
                resultCode += FINE_LOCATION_RESULT;
            } else if (permission.contains(CALL_PHONE)) {
                resultCode += CALL_PHONE_RESULT;
            } else if (permission.contains(CAMERA)) {
                resultCode += CAMERA_RESULT;
            } else if (permission.contains(READ_CONTACTS)) {
                resultCode += READ_CONTACTS_RESULT;
            } else if (permission.contains(RECORD_AUDIO)) {
                resultCode += RECORD_AUDIO_RESULT;
            } else if (permission.contains(WRITE_EXTERNAL_STORAGE)) {
                resultCode += WRITE_EXTERNAL_RESULT;
            }
            // TODO : add other result codes
        }
        return resultCode;
    }

}
