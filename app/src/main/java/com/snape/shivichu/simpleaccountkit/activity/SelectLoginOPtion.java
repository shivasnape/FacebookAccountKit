package com.snape.shivichu.simpleaccountkit.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.appevents.AppEventsLogger;
import com.snape.shivichu.simpleaccountkit.R;


/**
 * Created by Shivichu on 19-11-2017.
 */

public class SelectLoginOPtion extends AppCompatActivity {

    Button bSMS,bEMAIL;
    public static final int APP_REQUEST_CODE = 121;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS};
    private TextView txtPermissions;
    private Button btnCheckPermissions;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_select_option);
        
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkandgrantpermission();
        }

        AccessToken accessToken = AccountKit.getCurrentAccessToken();

        if (accessToken != null) {
            //Handle Returning User
            goToMyLoggedInActivity();
        }


        bSMS = (Button)findViewById(R.id.btn_sms);
        bEMAIL = (Button)findViewById(R.id.btn_email);
    }



    private void checkandgrantpermission() {

        if (ActivityCompat.checkSelfPermission(SelectLoginOPtion.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(SelectLoginOPtion.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED ){
            if (ActivityCompat.shouldShowRequestPermissionRationale(SelectLoginOPtion.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SelectLoginOPtion.this, permissionsRequired[1])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectLoginOPtion.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Multiple Permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SelectLoginOPtion.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectLoginOPtion.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Multiple Permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Permissions", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(SelectLoginOPtion.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

//            txtPermissions.setText("Permissions Required");

            //saving the permission in to a Shared Preference
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();
        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }


    }



    public void phoneLogin(final View view) {

        final Intent intent = new Intent(getApplicationContext(), AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.CODE); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    public void emailLogin(final View view) {

        final Intent intent = new Intent(getApplicationContext(), AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.EMAIL,
                        AccountKitActivity.ResponseType.CODE); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                showErrorActivity(loginResult.getError());
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
            } else {
                if (loginResult.getAccessToken() != null) {
                    toastMessage = "Success:" + loginResult.getAccessToken().getAccountId();
                } else {
                    toastMessage = String.format(
                            "Success:%s...",
                            loginResult.getAuthorizationCode().substring(0,10));
                }

                // If you have an authorization code, retrieve it from
                // loginResult.getAuthorizationCode()
                // and pass it to your server and exchange it for an access token.

                // Success! Start your next activity...
                goToMyLoggedInActivity();
            }

            // Surface the result to your user in an appropriate way.
            Toast.makeText(
                    this, toastMessage, Toast.LENGTH_LONG).show();
        }

        else if(requestCode == REQUEST_PERMISSION_SETTING)
        {

            if (ActivityCompat.checkSelfPermission(SelectLoginOPtion.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }

    }

    private void proceedAfterPermission() {
        Log.d("*********","Got All Permissions");
//        txtPermissions.setText("We've got all permissions");
//        Toast.makeText(getBaseContext(), "We got All Permissions", Toast.LENGTH_LONG).show();
    }


    private void goToMyLoggedInActivity() {

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void showErrorActivity(AccountKitError error) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SelectLoginOPtion.this);
        alertDialog.setTitle("Error");
        alertDialog.setMessage(error.toString());
        alertDialog.setCancelable(true);
        alertDialog.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(SelectLoginOPtion.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SelectLoginOPtion.this, permissionsRequired[1])) {
                txtPermissions.setText("Permissions Required");
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectLoginOPtion.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Multiple Permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SelectLoginOPtion.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
