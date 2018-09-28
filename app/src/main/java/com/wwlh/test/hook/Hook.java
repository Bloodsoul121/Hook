package com.wwlh.test.hook;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Hook {

    public void hook() {

        try {

            // 反射 ActivityManager 类
//            Class<?> classActivityManager = Class.forName("android.app.ActivityManager");
            Class<?> classActivityManager = Class.forName("android.app.ActivityManagerNative");

            Field[] fields = classActivityManager.getDeclaredFields();
            for (Field f : fields) {
                Log.i("Hook", "field : " + f.getName());
            }

//            Field iActivityManagerSingleton = classActivityManager.getDeclaredField("IActivityManagerSingleton");
            Field iActivityManagerSingleton = classActivityManager.getDeclaredField("gDefault");
            iActivityManagerSingleton.setAccessible(true);
            Object mIActivityManagerSingleton = iActivityManagerSingleton.get(null);// static 传入 null

            // 反射 Singleton 类
            Class<?> classSingleton = Class.forName("android.util.Singleton");
            Field mInstance = classSingleton.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            // 拿到 IActivityManager
            final Object iActivityManagerObject = mInstance.get(mIActivityManagerSingleton);

            // 对 IActivityManager 进行代理
            Class<?> classIActivityManager = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{classIActivityManager}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("startActivity".equals(method.getName())) {
                        Log.i("Hook", "haha , it is a surprise !");
                    }
                    return method.invoke(iActivityManagerObject, args);
                }
            });

            mInstance.set(mIActivityManagerSingleton, proxy);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
