package com.company.arminro.viewmodel;

import android.content.Context;

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
