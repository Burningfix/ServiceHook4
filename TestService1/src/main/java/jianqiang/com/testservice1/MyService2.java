package jianqiang.com.testservice1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.jianqiang.mypluginlibrary.IMyInterface;

public class MyService2 extends Service {
    private String TAG = "sanbo.plugin.s2";
    private int count;

    private MyBinder binder = new MyBinder();

    public class MyBinder extends Binder implements IMyInterface {
        public int getCount() {
            return count;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "Service is binded");
        count = count + 1;
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        count = count + 1;
        Log.e(TAG, "Service is created");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        count = count + 1;
        Log.e(TAG, "Service is Unbind");
        return true;
    }

    @Override
    public void onDestroy() {
        count = count + 1;
        super.onDestroy();
        Log.e(TAG, "Service is Destroy");
    }
}