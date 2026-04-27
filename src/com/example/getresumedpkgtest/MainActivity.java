package com.example.getresumedpkgtest;

import android.app.Activity;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "GetResumedPkgTest";
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultText = findViewById(R.id.result_text);
        Button btnQuery = findViewById(R.id.btn_query);
        Button btnQueryAll = findViewById(R.id.btn_query_all);

        btnQuery.setOnClickListener(v -> queryResumedPackage(Display.DEFAULT_DISPLAY));
        btnQueryAll.setOnClickListener(v -> queryAllDisplays());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto-query all displays when activity is resumed for testing
        getWindow().getDecorView().postDelayed(this::queryAllDisplays, 1000);
    }

    private void queryResumedPackage(int displayId) {
        try {
            android.os.IBinder binder = ServiceManager.getService("activity_task");
            android.app.IActivityTaskManager atm =
                    android.app.IActivityTaskManager.Stub.asInterface(binder);

            String pkg = atm.getResumedPackageNameOnDisplay(displayId);

            String result = "Display " + displayId + " resumed pkg:\n" + pkg;
            Log.d(TAG, result);
            mResultText.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();

        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
            mResultText.setText("Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void queryAllDisplays() {
        StringBuilder sb = new StringBuilder();
        DisplayManager dm = getSystemService(DisplayManager.class);
        for (Display display : dm.getDisplays()) {
            try {
                android.os.IBinder binder = ServiceManager.getService("activity_task");
                android.app.IActivityTaskManager atm =
                        android.app.IActivityTaskManager.Stub.asInterface(binder);
                String pkg = atm.getResumedPackageNameOnDisplay(display.getDisplayId());
                sb.append("Display ").append(display.getDisplayId())
                  .append(" (").append(display.getName()).append(")")
                  .append(": ").append(pkg != null ? pkg : "null").append("\n");
            } catch (RemoteException e) {
                sb.append("Display ").append(display.getDisplayId())
                  .append(": ERROR ").append(e.getMessage()).append("\n");
            }
        }
        Log.d(TAG, sb.toString().trim());
        mResultText.setText(sb.toString().trim());
    }
}
