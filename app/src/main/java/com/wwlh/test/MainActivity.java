package com.wwlh.test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wwlh.test.customclassloader.DiskClassLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private Button mSkinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSkinBtn = findViewById(R.id.skinbtn);
    }

    public void clickBtn1(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }

    public void clickBtn2(View view) {
        DiskClassLoader diskClassLoader = new DiskClassLoader("E:\\test");//1 java程序, 不是android
        try {
            Class c = diskClassLoader.loadClass("com.example.Jobs");//2
            if (c != null) {
                try {
                    Object obj = c.newInstance();
                    System.out.println(obj.getClass().getClassLoader());
                    Method method = c.getDeclaredMethod("say");
                    method.invoke(obj);//3
                } catch (InstantiationException | IllegalAccessException
                        | NoSuchMethodException
                        | SecurityException |
                        IllegalArgumentException |
                        InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void clickBtn3(View view) {
        copy2file("plugn-debug.apk");

        //插件APK路径
        //  /data/user/0/com.nan.dynalmic/files/plugin-debug.apk
        String dexPath = getFileStreamPath("plugn-debug.apk").getAbsolutePath();

        Log.i("<plugn>", "dexpath : " + dexPath);

        //DexClassLoader加载的时候Dex文件释放的路径
        //  /data/user/0/com.nan.dynalmic/app_dex
        String fileReleasePath = getDir("dex", Context.MODE_PRIVATE).getAbsolutePath();
        Log.i("<plugn>", "fileReleasePath : " + fileReleasePath);

        //通过DexClassLoader加载插件APK
        DexClassLoader mPluginClassLoader = new DexClassLoader(dexPath, fileReleasePath, null, getClassLoader());

        try {
            Class<?> beanClass = mPluginClassLoader.loadClass("com.wwlh.apk.Bean");
            Object beanObject = beanClass.newInstance();

            Method setNameMethod = beanClass.getMethod("setName", String.class);
            setNameMethod.setAccessible(true);
            Method getNameMethod = beanClass.getMethod("getName");
            getNameMethod.setAccessible(true);

            setNameMethod.invoke(beanObject, "blood");
            String name = (String) getNameMethod.invoke(beanObject);

            Log.i("<plugn>", "Bean - name : " + name);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void copy2file(String fileName) {
        OutputStream out = null;
        InputStream in = null;
        try {
            File file = new File(getFilesDir(), fileName);
            out = new FileOutputStream(file);
            in = getAssets().open(fileName);
            byte[] arr = new byte[1024];
            int len;
            while ((len = in.read(arr)) != -1) {
                out.write(arr, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private  boolean mChange = false;

    public void clickBtn4(View view) {
        copy2file("Plugn-skin-A-debug.apk");
        copy2file("Plugn-skin-B-debug.apk");

        String skinType;
        if(!mChange){
            skinType= "Plugn-skin-A-debug.apk";
            mChange=true;
        }else {
            skinType= "Plugn-skin-B-debug.apk";
            mChange=false;
        }
        final String path = getFilesDir() + File.separator + skinType;
        final String pkgName = getUninstallApkPkgName(this, path);
        dynamicLoadApk(path,pkgName);
    }

    private void dynamicLoadApk(String pApkFilePath,String pApkPacketName){
        File file = getDir("dex", Context.MODE_PRIVATE);
        //第一个参数：是dex压缩文件的路径
        //第二个参数：是dex解压缩后存放的目录
        //第三个参数：是C/C++依赖的本地库文件目录,可以为null
        //第四个参数：是上一级的类加载器
        DexClassLoader classLoader = new DexClassLoader(pApkFilePath, file.getAbsolutePath(),null, getClassLoader());
        try {
            final Class<?> loadClazz = classLoader.loadClass(pApkPacketName + ".R$drawable");
            //插件中皮肤的名称是skin_one
            final Field skinOneField = loadClazz.getDeclaredField("skin_one");
            skinOneField.setAccessible(true);
            //反射获取skin_one的resousreId
            final int resousreId = (int) skinOneField.get(R.id.class);
            //可以加载插件资源的Resources
            final Resources resources = createResources(pApkFilePath);
            if (resources != null) {
                final Drawable drawable = resources.getDrawable(resousreId);
                mSkinBtn.setBackground(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取未安装apk的信息
     * @param context
     * @param pApkFilePath apk文件的path
     * @return
     */
    private String getUninstallApkPkgName(Context context, String pApkFilePath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(pApkFilePath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            return appInfo.packageName;
        }
        return "";
    }

    /**
     * 获取AssetManager   用来加载插件资源
     * @param pFilePath  插件的路径
     * @return
     */
    private AssetManager createAssetManager(String pFilePath) {
        try {
            final AssetManager assetManager = AssetManager.class.newInstance();
            final Class<?> assetManagerClazz = Class.forName("android.content.res.AssetManager");
            final Method addAssetPathMethod = assetManagerClazz.getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, pFilePath);
            return assetManager;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //这个Resources就可以加载非宿主apk中的资源
    private Resources createResources(String pFilePath){
        final AssetManager assetManager = createAssetManager(pFilePath);
        Resources superRes = this.getResources();
        return new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
    }


}
