package com.snape.shivichu.simpleaccountkit.app;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by Shivichu on 19-11-2017.
 */

public class AccountKitApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Kitkat and lower has a bug that can cause in correct strict mode
            // warnings about expected activity counts
            enableStrictMode();
        }
    }

    public void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
}
