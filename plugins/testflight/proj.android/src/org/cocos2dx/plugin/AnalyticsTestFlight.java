/****************************************************************************
Copyright (c) 2012-2013 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package org.cocos2dx.plugin;

import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.testflightapp.lib.TestFlight;

public class AnalyticsTestFlight implements InterfaceAnalytics {

    private Context mContext = null;
    protected static String TAG = "AnalyticsTestFlight";

    protected static void LogE(String msg, Exception e) {
        Log.e(TAG, msg, e);
        e.printStackTrace();
    }

    private static boolean isDebug = false;
    protected static void LogD(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public AnalyticsTestFlight(Context context) {
        mContext = context;
    }
    
    @Override
    public void startSession(String appKey) {
        LogD("startSession invoked!");
        final String curKey = appKey;
        //final String appToken = mContext.getString(R.string.testflightAppToken);
        PluginWrapper.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class.forName("android.os.AsyncTask");
                } catch (ClassNotFoundException e) {
                   e.printStackTrace();
                }
                TestFlight.takeOff((Application)mContext, curKey);
            }
            
        });
    }

    @Override
    public void stopSession() {
        LogD("stopSession NOT used!");
    }

    @Override
    public void setSessionContinueMillis(int millis) {
        LogD("setSessionContinueMillis NOT used!");
    }

    @Override
    public void setCaptureUncaughtException(boolean isEnabled) {
        LogD("setCaptureUncaughtException NOT used!");
    }

    @Override
    public void setDebugMode(boolean isDebugMode) {
        LogD("setDebugMode NOT used!");
    }

    @Override
    public void logError(String errorId, String message) {
        LogD("logError NOT used!");
    }

    @Override
    public void logEvent(String eventId) {
        LogD("logEvent NOT used!");
    }

    @Override
    public void logEvent(String eventId, Hashtable<String, String> paramMap) {
        LogD("logEvent NOT used!");
    }

    @Override
    public void logTimedEventBegin(String eventId) {
        LogD("logTimedEventBegin NOT used!");
    }

    @Override
    public void logTimedEventEnd(String eventId) {
        LogD("logTimedEventBegin NOT used!");
    }

    @Override
    public String getSDKVersion() {
        return "1.3";
    }

    @Override
    public String getPluginVersion() {
        return "0.0.1";
    }
}
