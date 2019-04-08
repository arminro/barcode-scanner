package com.company.arminro.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrowserControl  {

    public BrowserControl(Context c) {
        this.c = c;
    }

    private Context c;
    private StartBrowserEventListener startBrowser;


    public void setstartBrowserEventListener(StartBrowserEventListener eventListener) {
        startBrowser = eventListener;
    }

    public void start(String text){
        if(startBrowser != null){
            startBrowser.startBrowser(text);
        }
    }
}
