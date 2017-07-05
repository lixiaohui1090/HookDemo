package xuansu.com.hookdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by xuansu on 2017/7/4.
 */

public class SecondActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Log.i("http_activity","SecondActivity");
    }
}
