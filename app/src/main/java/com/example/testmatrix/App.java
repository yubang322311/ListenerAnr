package com.example.testmatrix;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;

import com.example.testmatrix.config.DynamicConfigImplDemo;
import com.tencent.matrix.Matrix;
import com.tencent.matrix.lifecycle.MatrixLifecycleLogger;
import com.tencent.matrix.trace.TracePlugin;
import com.tencent.matrix.trace.config.TraceConfig;
import com.tencent.matrix.util.MatrixLog;

import java.io.File;

public class App extends Application {
    private static final String TAG = "Matrix.Application";

    private static Context sContext;

    public static boolean is64BitRuntime() {
        final String currRuntimeABI = Build.CPU_ABI;
        return "arm64-v8a".equalsIgnoreCase(currRuntimeABI)
                || "x86_64".equalsIgnoreCase(currRuntimeABI)
                || "mips64".equalsIgnoreCase(currRuntimeABI);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.loadLibrary("c++_shared");

//        if (!is64BitRuntime()) {
//            try {
//                final PthreadHook.ThreadStackShrinkConfig config = new PthreadHook.ThreadStackShrinkConfig()
//                        .setEnabled(true)
//                        .addIgnoreCreatorSoPatterns(".*/app_tbs/.*")
//                        .addIgnoreCreatorSoPatterns(".*/libany\\.so$");
//                HookManager.INSTANCE.addHook(PthreadHook.INSTANCE.setThreadStackShrinkConfig(config)).commitHooks();
//            } catch (HookManager.HookFailedException e) {
//                e.printStackTrace();
//            }
//        }

        try {
            ServiceInfo[] services = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SERVICES).services;
            for (ServiceInfo service : services) {
                MatrixLog.d(TAG, "name = %s, processName = %s", service.name, service.processName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        DynamicConfigImplDemo dynamicConfig = new DynamicConfigImplDemo();
        Matrix.Builder builder = new Matrix.Builder(this);

        builder.pluginListener(new TestPluginListener(this));

        TracePlugin tracePlugin = configureTracePlugin(dynamicConfig);
        builder.plugin(tracePlugin);

        Matrix.init(builder.build());

        Matrix.with().startAllPlugins();


    }

    private TracePlugin configureTracePlugin(DynamicConfigImplDemo dynamicConfig) {

        boolean fpsEnable = dynamicConfig.isFPSEnable();
        boolean traceEnable = dynamicConfig.isTraceEnable();
        boolean signalAnrTraceEnable = dynamicConfig.isSignalAnrTraceEnable();

        File traceFileDir = new File(getApplicationContext().getFilesDir(), "matrix_trace");
        if (!traceFileDir.exists()) {
            if (traceFileDir.mkdirs()) {
                MatrixLog.e(TAG, "failed to create traceFileDir");
            }
        }

        File anrTraceFile = new File(traceFileDir, "anr_trace");    // path : /data/user/0/sample.tencent.matrix/files/matrix_trace/anr_trace
        File printTraceFile = new File(traceFileDir, "print_trace");    // path : /data/user/0/sample.tencent.matrix/files/matrix_trace/print_trace

        TraceConfig traceConfig = new TraceConfig.Builder()
                .dynamicConfig(dynamicConfig)
                .enableFPS(fpsEnable)
                .enableEvilMethodTrace(traceEnable)
                .enableAnrTrace(traceEnable)
                .enableStartup(traceEnable)
                .enableIdleHandlerTrace(traceEnable)                    // Introduced in Matrix 2.0
                .enableSignalAnrTrace(signalAnrTraceEnable)             // Introduced in Matrix 2.0
                .anrTracePath(anrTraceFile.getAbsolutePath())
                .printTracePath(printTraceFile.getAbsolutePath())
                .splashActivities("sample.tencent.matrix.SplashActivity;")
                .isDebug(true)
                .isDevEnv(false)
                .build();

        //Another way to use SignalAnrTracer separately
        //useSignalAnrTraceAlone(anrTraceFile.getAbsolutePath(), printTraceFile.getAbsolutePath());

        return new TracePlugin(traceConfig);
    }
}
