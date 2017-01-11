package com.example.lauren.mobilephoneguard.activity;

import android.app.Application;
import android.app.DownloadManager;

import org.xutils.x;

/**
 * Created by Lauren on 17/1/11.
 */

public class XutilsDemo extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化 XUtils3,必须要这个
        //而且一定要在 mainifest 中增加  android:name=".activity.XutilsDemo"

        x.Ext.init(this);




    }
}
