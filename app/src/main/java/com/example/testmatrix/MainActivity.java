package com.example.testmatrix;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import com.tencent.matrix.AppActiveMatrixDelegate;
import com.tencent.matrix.Matrix;
import com.tencent.matrix.plugin.Plugin;
import com.tencent.matrix.trace.TracePlugin;
import com.tencent.matrix.util.MatrixLog;

public class MainActivity extends AppCompatActivity {
    boolean sleep = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Plugin plugin = Matrix.with().getPluginByClass(TracePlugin.class);
        if (!plugin.isPluginStarted()) {
            MatrixLog.i("MainActivity", "plugin-trace start");
            plugin.start();
        }

        findViewById(R.id.testanr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testANR(v);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Plugin plugin = Matrix.with().getPluginByClass(TracePlugin.class);
        if (plugin.isPluginStarted()) {
            MatrixLog.i("MainActivity", "plugin-trace stop");
            plugin.stop();
        }
    }

    public void testANR(final View view) {
        if(!sleep){
            sleep = true;
            SystemClock.sleep(8000);
            sleep = false;
        }
    }
}