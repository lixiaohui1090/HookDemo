package xuansu.com.hookdemo;

import android.app.Application;

/**
 * Created by xuansu on 2017/7/4.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AmsManager  ams=new AmsManager(ProxyActivity.class,this);
        ams.hookSystemHandler();
        ams.hookAms();
    }
}
