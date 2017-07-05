package xuansu.com.hookdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by xuansu on 2017/7/3.
 */

public class AmsManager {
    private Class<?> proxyActivity;

    private Context mContetxt;


    public AmsManager(Class<?> proxyActivity, Context mContetxt) {
        this.proxyActivity = proxyActivity;
        this.mContetxt = mContetxt;
    }

    public void hookAms() {
        try {
            Class<?> clazz = Class.forName("android.app.ActivityManagerNative");
            Field gDefault = clazz.getDeclaredField("gDefault");
            gDefault.setAccessible(true);
            Object  defaultValue=gDefault.get(null);
            Class<?> singleClazz = Class.forName("android.util.Singleton");
            Field mInstance =    singleClazz.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            Object IActivityManagerValue=  mInstance.get(defaultValue);
            //  创建新的借口对象
            Class<?> IActivityManagerProxy=Class.forName("android.app.IActivityManager");
            AmsInvocationHandler  handler=new AmsInvocationHandler(IActivityManagerValue);
          Object o=  Proxy.newProxyInstance(mContetxt.getClassLoader(),new Class<?>[]{IActivityManagerProxy},handler);
           mInstance.set(defaultValue,o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class  AmsInvocationHandler implements InvocationHandler{
        Object  IActivityManagerValue;
        public AmsInvocationHandler( Object IActivityManagerValue) {
            this.IActivityManagerValue=IActivityManagerValue;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if("startActivity".equals(method.getName())){
                Intent  intent=null;
                int  index=0;
                for(int i=0;i<args.length;i++){
                    if(args[i] instanceof  Intent){
                        intent  =(Intent)args[i];
                        index=i;
                        break;
                    }
                }
                Intent   proxIntent=new Intent();
                ComponentName  cmp=new ComponentName(mContetxt,proxyActivity);
                Log.i("http_log",proxyActivity.getName());
                proxIntent.setComponent(cmp);
                proxIntent.putExtra("oldIntent",intent);
                Log.i("http_log_new",intent.getComponent().getClassName());
                args[index]=proxIntent;
            }
            return method.invoke(IActivityManagerValue,args);
        }
    }

   ;
    public  void  hookSystemHandler(){
        try {
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Method  sCurrentActivityThread= clazz.getDeclaredMethod("currentActivityThread");
            sCurrentActivityThread.setAccessible(true);
            //获取主线程对象
            Object activityThread= sCurrentActivityThread.invoke(null);


            //获取mH字段
            Field  mH = clazz.getDeclaredField("mH");

            mH.setAccessible(true);
            Handler  handler = (Handler) mH.get(activityThread);

            //获取原始的mCallBack字段
            Field  mCallbackFiled=Handler.class.getDeclaredField("mCallback");
            mCallbackFiled.setAccessible(true);
            mCallbackFiled.set(handler ,new ActivityThreadHandlerCallback(handler ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class  ActivityThreadHandlerCallback  implements Handler.Callback{
//        Handler  handler;

        public ActivityThreadHandlerCallback(Handler handler) {
//            this.handler = handler;
        }

        @Override
        public boolean handleMessage(Message msg) {

            if(msg.what==100){
                handlerLaunchActivity(msg);
            }
//            handler.handleMessage(msg);

            return false;
        }


    }
    private void handlerLaunchActivity(Message msg) {
        try {
            Object  o=msg.obj;
            Field  intentFiled=o.getClass().getDeclaredField("intent");
            intentFiled.setAccessible(true);
            Intent proxyIntent= (Intent) intentFiled.get(o);
            Log.i("http_log_new_2",proxyIntent.getComponent().getClassName());
            Intent  realIntent=  proxyIntent.getParcelableExtra("oldIntent");
            Log.i("http_log_new_1",realIntent.getComponent().getClassName());
            if(realIntent!=null){
                proxyIntent.setComponent(realIntent.getComponent());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
