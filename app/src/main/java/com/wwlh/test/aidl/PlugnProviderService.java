package com.wwlh.test.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

public class PlugnProviderService extends Service {

    private static class PlugnBinder extends IMyAidlInterface.Stub {
        public String getPlugnName() {
            return "you are so young";
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String getName() throws RemoteException {
            return null;
        }

        @Override
        public String getOtherName() throws RemoteException {
            return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlugnBinder();
    }

}
