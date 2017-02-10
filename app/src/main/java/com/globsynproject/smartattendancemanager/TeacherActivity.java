package com.globsynproject.smartattendancemanager;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class TeacherActivity extends AppCompatActivity {

    WifiController controller;
    //WifiInfo info;
    Timer timer;
    TimerTask timerTask;
    Context context;
    static FileController fileController;
    String ssid[], pwd[];

    int timeOut = 0, position =0, n;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        ssid = b.getStringArray("SSID");
        pwd = b.getStringArray("KEYS");
        n=ssid.length;
        context = this;
        controller = new WifiController(this);
        Message.toastMessage(this, "Before pressing the button, please switch on WiFi and remain disconnected from ALL networks.", "long");
    }
    public void startAttendance(View view){
        if(!controller.checkWifiOn()||(controller.checkWifiOn()&&controller.wifiManager.getConnectionInfo().getSupplicantState().equals(SupplicantState.COMPLETED))){
            Message.toastMessage(this, "Please switch on WiFi and remain disconnected from ALL networks to proceed.", "long");
            return;
        }
        startNewConnection(position);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message.logMessages("CHECK: ", "Checking status...");
                if(timeOut>=20){
                    Message.logMessages("WIFI: ","TIMED OUT");
                    timeOut=0; position++;

                    startNewConnection(position);
                    return;
                }

                if(controller.getConnectionStatus()){
                    Message.logMessages("WIFI: ","CONNECTED");
                    writeToFile(controller.wifiManager.getConnectionInfo());
                    controller.disbandConnection();
                    timeOut=0; position++;
                    startNewConnection(position);
                    return;
                }
                timeOut++;
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public void startNewConnection(int pos){
        if(pos>=n){
            TeacherActivity.this.timer.cancel();
            Message.logMessages("DONE", "COMPLETE");
            controller.turnWifiOff();
            position=0; timeOut=0;
            return;
        }
        Message.logMessages("WIFI", "Setting up connection: "+ssid[pos]+", "+pwd[pos]);
        Message.logMessages("CHECK: ", Boolean.toString(controller.establishConnection(ssid[pos], pwd[pos])));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fileController.delete_File();//deleting the temporary file.
    }

    public void writeToFile(WifiInfo info){
        fileController = new FileController(context);
        fileController.appendData_File(info.getBSSID());
    }
}