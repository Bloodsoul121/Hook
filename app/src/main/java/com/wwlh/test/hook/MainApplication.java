package com.wwlh.test.hook;

import android.app.Application;

/**
 * Created by wwlhac on 2018/9/13.
 */

public class MainApplication extends Application {

    private static MainApplication myApplication = null;

    public static MainApplication getApplication() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        myApplication = this;

        Hook hook = new Hook();
        hook.hook();
    }

}
