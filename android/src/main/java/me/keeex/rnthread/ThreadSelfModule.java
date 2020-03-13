package me.keeex.rnthread;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

@ReactModule(name = ThreadSelfModule.REACT_MODULE_NAME)
public class ThreadSelfModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNThread: ThreadSelfModule";
    public static final String REACT_MODULE_NAME = "ThreadSelfManager";

    private int threadId;
    private ReactApplicationContext parentContext;

    public ThreadSelfModule(ReactApplicationContext context) {
        super(context);
        Log.d(TAG, "ctor()");
    }

    public void initialize(int threadId, ReactApplicationContext parentContext) {
        Log.d(TAG, "initialize(" + threadId + ")");
        this.parentContext = parentContext;
        this.threadId = threadId;
    }

    @Override
    public String getName() {
        return REACT_MODULE_NAME;
    }

    @ReactMethod
    public void postMessage(String data, final Promise promise) {
        Log.d(TAG, "postMessage(\"" + data + "\")");
        if (parentContext == null) {
            Log.d(TAG, "No parentContext(), expect breakage");
            promise.reject(null);
            return;
        }

        parentContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("Thread" + String.valueOf(threadId), data);
        promise.resolve(null);
    }
}
