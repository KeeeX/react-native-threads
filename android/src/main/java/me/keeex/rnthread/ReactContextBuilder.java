package me.keeex.rnthread;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.hermes.reactexecutor.HermesExecutorFactory;
import com.facebook.react.NativeModuleRegistryBuilder;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.CatalystInstanceImpl;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.jscexecutor.JSCExecutorFactory;
import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.queue.ReactQueueConfigurationSpec;
import com.facebook.react.devsupport.interfaces.DevSupportManager;
import com.facebook.soloader.SoLoader;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static com.facebook.react.modules.systeminfo.AndroidInfoHelpers.getFriendlyDeviceName;

public class ReactContextBuilder {
    private static final String TAG = "RNThread: ReactContextBuilder";

    private Context parentContext;
    private JSBundleLoader jsBundleLoader;
    private DevSupportManager devSupportManager;
    private ReactInstanceManager instanceManager;
    private ArrayList<ReactPackage> reactPackages;

    public ReactContextBuilder(Context context) {
        Log.d(TAG, "ctor()");
        this.parentContext = context;
        SoLoader.init(context, /* native exopackage */ false);
    }

    public ReactContextBuilder setJSBundleLoader(JSBundleLoader jsBundleLoader) {
        Log.d(TAG, "setJSBundleLoader()");
        this.jsBundleLoader = jsBundleLoader;
        return this;
    }

    public ReactContextBuilder setDevSupportManager(DevSupportManager devSupportManager) {
        Log.d(TAG, "setDevSupportManager()");
        this.devSupportManager = devSupportManager;
        return this;
    }

    public ReactContextBuilder setReactInstanceManager(ReactInstanceManager manager) {
        Log.d(TAG, "setReactInstanceManager()");
        this.instanceManager = manager;
        return this;
    }

    public ReactContextBuilder setReactPackages(ArrayList<ReactPackage> reactPackages) {
        Log.d(TAG, "setReactPackages()");
        this.reactPackages = reactPackages;
        return this;
    }

    private JavaScriptExecutorFactory getJSExecutorFactory() {
        Log.d(TAG, "getJSExecutorFactory()");
        try {
            String appName = Uri.encode(parentContext.getPackageName());
            String deviceName = Uri.encode(getFriendlyDeviceName());
            // If JSC is included, use it as normal
            SoLoader.loadLibrary("jscexecutor");
            JavaScriptExecutorFactory factory = new JSCExecutorFactory(appName, deviceName);
            Log.d(TAG, "Use JSC");
            return factory;
        } catch (UnsatisfiedLinkError jscE) {
            // Otherwise use Hermes
            Log.d(TAG, "Use Hermes");
            return new HermesExecutorFactory();
        }
    }

    public ReactApplicationContext build() throws Exception {
        Log.d(TAG, "build()");
        JavaScriptExecutor jsExecutor = getJSExecutorFactory().create();

        // fresh new react context
        final ReactApplicationContext reactContext = new ReactApplicationContext(parentContext);
        if (devSupportManager != null) {
            reactContext.setNativeModuleCallExceptionHandler(devSupportManager);
        }

        // load native modules
        NativeModuleRegistryBuilder nativeRegistryBuilder = new NativeModuleRegistryBuilder(reactContext, this.instanceManager);
        addNativeModules(nativeRegistryBuilder);

        CatalystInstanceImpl.Builder catalystInstanceBuilder = new CatalystInstanceImpl.Builder()
                .setReactQueueConfigurationSpec(ReactQueueConfigurationSpec.createDefault())
                .setJSExecutor(jsExecutor)
                .setRegistry(nativeRegistryBuilder.build())
                .setJSBundleLoader(jsBundleLoader)
                .setNativeModuleCallExceptionHandler(devSupportManager != null
                        ? devSupportManager
                        : createNativeModuleExceptionHandler()
                );


        final CatalystInstance catalystInstance;
        catalystInstance = catalystInstanceBuilder.build();

        catalystInstance.getReactQueueConfiguration().getJSQueueThread().callOnQueue(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        try {
                            reactContext.initializeWithInstance(catalystInstance);
                            catalystInstance.runJSBundle();
                        } catch (Exception e) {
                            Log.d(TAG, "Exception in callOnQueue");
                            e.printStackTrace();
                            devSupportManager.handleException(e);
                        }

                        return null;
                    }
                }
        ).get();

        catalystInstance.getReactQueueConfiguration().getUIQueueThread().callOnQueue(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    catalystInstance.initialize();
                    reactContext.onHostResume(null);
                } catch (Exception e) {
                    Log.d(TAG, "Exception in callOnQueue");
                    e.printStackTrace();
                    devSupportManager.handleException(e);
                }

                return null;
            }
        }).get();

        return reactContext;
    }

    private NativeModuleCallExceptionHandler createNativeModuleExceptionHandler() {
        Log.d(TAG, "createNativeModuleExceptionHandler()");
        return new NativeModuleCallExceptionHandler() {
            @Override
            public void handleException(Exception e) {
                Log.d(TAG, "handleException()");
                throw new RuntimeException(e);
            }
        };
    }

    private void addNativeModules(NativeModuleRegistryBuilder nativeRegistryBuilder) {
        Log.d(TAG, "addNativeModules()");
        for (int i = 0; i < reactPackages.size(); i++) {
            ReactPackage reactPackage = reactPackages.get(i);
            nativeRegistryBuilder.processPackage(reactPackage);
        }
    }
}
