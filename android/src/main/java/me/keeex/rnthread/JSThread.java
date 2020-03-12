package me.keeex.rnthread;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Random;

/**
 * Java side of a "thread" from JavaScript
 */
public class JSThread {
    /**
     * Tag used for logging purpose
     */
    private static final String TAG = "RNThread: JSThread"
    /**
     * Identifier used to match with the JavaScript instance of Thread
     */
    private int id;

    /**
     * Next id in sequence
     */
    private static int mNextId = 1;

    /**
     * Name of the loaded script
     */
    private String jsSlugname;

    /**
     * JavaScript VM containing the "thread" code
     */
    private ReactApplicationContext reactContext;

    /**
     * Constructor
     */
    public JSThread(String jsSlugname, ReactApplicationContext context, ReactContextBuilder reactContextBuilder) throws Exception {
        Log.d(TAG, "ctor(" + jsSlugname + ")")
        this.id = JSThread.mNextId;
        ++JSThread.mNextId;
        Log.d(TAG, "ThreadId=" + this.id);
        this.jsSlugname = jsSlugname;
        this.runFromContext(context, reactContextBuilder);
    }

    public int getThreadId() {
        return this.id;
    }

    public String getName() {
        return jsSlugname;
    }

    /**
     * Actually start the JavaScript VM
     */
    private void runFromContext(ReactApplicationContext context, ReactContextBuilder reactContextBuilder) throws Exception {
        Log.d(TAG, "runFromContext()");
        reactContext = reactContextBuilder.build();
        if (reactContext == null) {
            throw new Exception("Couldn't build react context");
        }
        Log.d(TAG, "reactContext built");

        ThreadSelfModule threadSelfModule = reactContext.getNativeModule(ThreadSelfModule.class);
        threadSelfModule.initialize(id, context);
        Log.d(TAG, "self initialized");
    }

    /**
     * Send a message to the JavaScript VM
     */
    public void postMessage(String message) throws Exception {
        Log.d(TAG, "postMessage(\"" + message + "\")");
        if (reactContext == null) {
            throw new Exception("Missing reactContext (should not happen)");
        }

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("ThreadMessage", message);
    }

    /**
     * Called when the host app is resumed. Restart the JavaScript VM
     */
    public void onHostResume() {
        Log.d(TAG, "onHostResume()");
        if (reactContext == null) {
            Log.d(TAG, "No reactContext in onHostResume, expect breakage");
            return;
        }

        reactContext.onHostResume(null);
    }

    /**
     * Called when the host app is paused. Stop the JavaScript VM
     */
    public void onHostPause() {
        Log.d(TAG, "onHostPause()");
        if (reactContext == null) {
            Log.d(TAG, "No reactContext in onHostPause, expect breakage");
            return;
        }

        reactContext.onHostPause();
    }

    /**
     * Kill the JavaScript VM
     */
    public void terminate() {
        Log.d(TAG, "terminate()");
        if (reactContext == null) {
            Log.d(TAG, "No reactContext in terminate, expect breakage");
            return;
        }

        reactContext.onHostPause();
        reactContext.destroy();
        reactContext = null;
    }
}
