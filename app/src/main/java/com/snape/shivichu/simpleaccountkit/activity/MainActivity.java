package com.snape.shivichu.simpleaccountkit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.PhoneNumber;
import com.snape.shivichu.simpleaccountkit.R;

/** Created by Shivichu 0n 19/11/2017
 *
 */

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button bLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.txtView);
        bLogout = (Button)findViewById(R.id.btn_logout);

        getAccountDetails();

        bLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AccountKit.logOut();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent i = new Intent(getApplicationContext(),SelectLoginOPtion.class);
                        startActivity(i);
                        finish();
                    }
                },300);

            }
        });

    }

    private void getAccountDetails() {

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {

                // Get Account Kit ID
                String accountKitId = account.getId();

                // Get phone number
                PhoneNumber phoneNumber = account.getPhoneNumber();
                String phoneNumberString = phoneNumber.toString();

                if(phoneNumberString!=null)
                {
                    Toast.makeText(getApplicationContext(),"Ph:"+phoneNumberString,Toast.LENGTH_LONG).show();
                    textView.setText(phoneNumberString);
                    Log.d("***PH NUMBER ***",phoneNumberString);
                }

                // Get email
                String email = account.getEmail();

                if(email!=null)
                {
                    Toast.makeText(getApplicationContext(),"Email:"+email,Toast.LENGTH_LONG).show();
                    textView.setText(email);
                    Log.d("***EMAIL ***",email);
                }
            }

            @Override
            public void onError(AccountKitError accountKitError) {

                Toast.makeText(getApplicationContext(),String.valueOf(accountKitError.getUserFacingMessage()),Toast.LENGTH_LONG).show();

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
